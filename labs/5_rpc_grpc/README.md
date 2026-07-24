## Proyecto `rpc-json` (RPC sobre TCP + JSON)

Se necesita **2 terminales**.

**Terminal 1 — Servidor:**
```bash
cd rpc-json
mvn compile
mvn exec:java -Dexec.mainClass="com.rpc.server.RpcServer"
```

**Terminal 2 — Cliente (con métricas):**
```bash
cd rpc-json
mvn exec:java -Dexec.mainClass="com.rpc.client.RpcClient"
```

Se verá algo así en la Terminal 2:
```
Conectado al servidor RPC.

[Client] Solicitud enviada: {"method":"multiply","params":[4.0,5.0],...}
[Client] Resultado: 20.0
[METRICS][multiply] Tiempo: 1.243 ms | ΔMemoria: +12.50 KB

[Client] Solicitud enviada: {"method":"divide","params":[10.0,2.0],...}
[Client] Resultado: 5.0
[METRICS][divide] Tiempo: 0.871 ms | ΔMemoria: +8.00 KB
...
```


## Proyecto `converter` (gRPC)

Se necesita **2 terminales**.

**Terminal 1 — Servidor:**
```bash
cd converter
mvn compile
mvn exec:java -Dexec.mainClass="converter.ServerMain"
```

**Terminal 2 — Cliente (con métricas):**
```bash
cd converter
mvn exec:java -Dexec.mainClass="converter.ClientMain"
```

Se verá el menú interactivo. Ejemplo de sesión:
```
=== SISTEMA DE CONVERSION ===
1. Celsius -> Fahrenheit
...
Seleccione opción: 1
Ingrese grados Celsius: 100

Resultado: 212.0
Mensaje: Conversión exitosa
[METRICS][c_f] Tiempo: 3.871 ms | ΔMemoria: +45.00 KB
```


## Notas importantes

**Orden obligatorio:** siempre levanta el servidor primero y espera a que imprima su mensaje de inicio antes de lanzar el cliente.

**`mvn compile` solo la primera vez** (o tras cambios en el código). Para ejecuciones posteriores basta con `mvn exec:java ...`.


## Ejecución de Pruebas y Monitoreo

Para ejecutar la comparativa de carga entre gRPC y JSON-RPC y monitorizar los resultados con Prometheus y Grafana, sigue estos pasos:

### Prerrequisitos
- **Java 17+** y **Maven** instalados.
- **K6** instalado y compilado con la extensión TCP (`k6/x/tcp`). *(En instalaciones recientes o usando `xk6`, la extensión TCP se puede proveer dinámicamente).*
    Usa el siguiente comando para traer el archivo ejecutable K6 con la extensión disponible:
    ```sh
    go install go.k6.io/xk6/cmd/xk6@latest && ~/go/bin/xk6 build --with github.com/grafana/xk6-tcp@latest
    ```
- **Docker y Docker Compose** instalados (para desplegar la pila de observabilidad).

### Paso 1: Iniciar la Observabilidad
En la raíz del proyecto se ha configurado un entorno automatizado con Docker Compose. Este entorno levanta **Prometheus** (puerto 9090) y **Grafana** (puerto 3000), con Prometheus ya configurado para ingerir métricas de ambos servidores automáticamente.

Para levantar la infraestructura, ejecuta en tu terminal:
```bash
docker-compose up -d
```
*(Nota: Grafana estará disponible en `http://localhost:3000` con usuario y contraseña `admin`)*.

### Paso 2: Iniciar los Servidores
Necesitas dos terminales para levantar ambos servidores simultáneamente.

**Terminal A (Servidor gRPC):**
```bash
cd converter
mvn clean compile
mvn exec:java -Dexec.mainClass="converter.ServerMain"
```

**Terminal B (Servidor JSON-RPC):**
```bash
cd rpc-json
mvn clean compile
mvn exec:java -Dexec.mainClass="com.rpc.server.RpcServer"
```

### Paso 3: Ejecutar las Pruebas
Con ambos servidores corriendo, abre una nueva terminal y lanza el script de carga.

**Carga contra gRPC:**
```bash
k6 run script.js -e SCENARIO=grpc_load_test
```
*(Nota: por defecto el script corre todos los escenarios, puedes usar el comando estándar `k6 run script.js` para correr la prueba base conjunta)*.

**Carga contra JSON-RPC (TCP Puro):**
Asegúrate de usar un binario de K6 que contenga la extensión `k6/x/tcp`:
```bash
./k6 run script.js -e SCENARIO=json_rpc_load_test
```

### Paso 4: Visualizar Resultados
1. Abre tu navegador y accede a **Grafana** (`http://localhost:3000`).
2. Configura el **Data Source** de Prometheus (apuntando a la IP de Prometheus, `http://localhost:9090` o `http://prometheus:9090` si estás en linux).
3. Importa el Dashboard en Grafana ubicado en [`./prometheus/grafana_dashboard.json`](./prometheus/grafana_dashboard.json). Este dashboard muestra:
   - `rpc_requests_latency_seconds` (para ver los tiempos de respuesta y percentiles).
   - `rpc_requests_total` (para calcular el Throughput / Peticiones por segundo).
   - Métricas de la JVM como `jvm_memory_used_bytes` y `jvm_threads_live_threads`.

> [!IMPORTANT]
> Las métricas solo estarán disponibles una vez ejecutadas la pruebas de rendimiento con k6
