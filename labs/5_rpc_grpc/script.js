import grpc from 'k6/net/grpc';
import { Socket } from 'k6/x/tcp';
import { check, sleep, fail } from 'k6';

// -----------------------------------------------------------------------------------------
// NOTA ARQUITECTÓNICA SOBRE JSON-RPC y HTTP
// -----------------------------------------------------------------------------------------
// El servidor JSON-RPC (RpcServer.java) está implementado utilizando Sockets TCP puros,
// escuchando por conexiones y leyendo JSON delimitado por saltos de línea (\n).
// NO ES UN SERVIDOR HTTP. 
// Por esta razón, NO se puede probar usando el módulo `k6/http` estándar, ya que este
// envía cabeceras HTTP que el servidor Java no sabe parsear, causando excepciones.
// 
// Para probar el servidor TCP nativamente en k6, se debe usar la extensión oficial
// `k6/x/tcp`. Puedes correr este script usando un binario de k6 compilado con la 
// extensión TCP (xk6 build --with github.com/grafana/xk6-tcp) o, en sistemas
// recientes, k6 la descargará automáticamente si está soportada.
// -----------------------------------------------------------------------------------------

// Inicializa el cliente gRPC y carga el proto
const grpcClient = new grpc.Client();
grpcClient.load(['converter/src/main/proto'], 'converter.proto');

export const options = {
  scenarios: {
    // Escenario de carga para gRPC
    grpc_load_test: {
      executor: 'constant-vus',
      vus: 10, // 10 usuarios virtuales concurrentes
      duration: '30s', // duración del test
      exec: 'test_grpc',
    },
    // Escenario de carga para JSON-RPC (TCP)
    json_rpc_load_test: {
      executor: 'constant-vus',
      vus: 10,
      duration: '30s',
      exec: 'test_json_rpc',
    },
  },
};

// Función para testear el servidor gRPC (Puerto 50051)
export function test_grpc() {
  grpcClient.connect('localhost:50051', { plaintext: true });

  const payload = {
    type: 'c_f',
    value: 25.0,
  };

  const response = grpcClient.invoke('Converter/Convert', payload);

  check(response, {
    'gRPC status is OK': (r) => r && r.status === grpc.StatusOK,
    'gRPC message exists': (r) => r && r.message,
  });

  grpcClient.close();
  sleep(0.1);
}

// Función para testear el servidor JSON-RPC sobre TCP (Puerto 8080)
export async function test_json_rpc() {
  const socket = new Socket();
  let receivedData = "";

  // Construir el Request JSON-RPC (exactamente como lo envía RpcClient.java)
  const requestId = "k6-test-" + __ITER; // __ITER es la iteración actual en k6
  const request = {
    method: "multiply",
    params: [4.0, 5.0],
    id: requestId
  };
  
  // Serializamos a string y añadimos el salto de línea obligatorio para el servidor
  const jsonPayload = JSON.stringify(request) + "\n";

  // Manejador de respuesta
  socket.on("data", (data) => {
    // La data recibida viene en bytes, la convertimos a string
    const stringData = String.fromCharCode.apply(null, new Uint8Array(data));
    receivedData += stringData;
    
    // Si la respuesta incluye un salto de línea (fin del request)
    if (stringData.includes('\n')) {
      try {
        const responseJson = JSON.parse(receivedData.trim());
        check(responseJson, {
          'JSON-RPC no error': (res) => !res.error,
          'JSON-RPC result correct': (res) => res.result === 20.0
        });
      } catch (e) {
        fail('Fallo al parsear JSON-RPC: ' + e.message);
      }
      if (socket && typeof socket.close === 'function') {
        try {
          socket.close();
        } catch(err) {
          // Ignora errores si el canal nativo ya se cerró
        }
      }
    }
  });

  socket.on("error", (err) => {
    fail('TCP Socket Error: ' + err.message);
  });

  // Conectar y enviar datos
  try {
    await socket.connect(8080, "localhost");
    await socket.write(jsonPayload);
  } catch (e) {
    fail('Error al conectar o escribir: ' + e.message);
  }
}
