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
