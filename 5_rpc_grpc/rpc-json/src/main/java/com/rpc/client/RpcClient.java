package com.rpc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpc.model.RpcRequest;
import com.rpc.model.RpcResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.UUID;

public class RpcClient {
  private static final String HOST = "127.0.0.1";
  private static final int PORT = 8080;
  private static final ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args) {
    try (Socket socket = new Socket(HOST, PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      socket.setSoTimeout(5000); // Timeout robusto
      System.out.println("Connected to RPC Server.");

      // Stub / Llamadas de ejemplo
      callRemoteProcedure(out, in, "multiply", 4.0, 5.0);
      callRemoteProcedure(out, in, "divide", 10.0, 2.0);
      callRemoteProcedure(out, in, "power", 2.0, 8.0);

      // Pruebas de robustez
      callRemoteProcedure(out, in, "divide", 10.0, 0.0); // Error División
      callRemoteProcedure(out, in, "subtract", 5.0, 2.0); // Error No Existe

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void callRemoteProcedure(
      PrintWriter out, BufferedReader in, String method, Double... params) throws Exception {
    // Construir Request
    String requestId = UUID.randomUUID().toString();
    RpcRequest request = new RpcRequest(method, Arrays.asList(params), requestId);

    // Serializar a JSON
    String jsonRequest = mapper.writeValueAsString(request);

    // Enviar por TCP (agregando salto de línea para el framing)
    out.println(jsonRequest);
    System.out.println("\n[Client] Request Sent: " + jsonRequest);

    // Leer respuesta TCP
    String jsonResponse = in.readLine();

    // Deserializar
    RpcResponse response = mapper.readValue(jsonResponse, RpcResponse.class);

    // Validar y mostrar
    if (response.getError() != null) {
      System.err.println("[Client] Error from Server: " + response.getError().getMessage());
    } else {
      System.out.println("[Client] Result: " + response.getResult());
    }
  }
}
