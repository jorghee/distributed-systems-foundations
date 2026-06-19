# LogiFresh Lab 9 Microservicios

Implementacion simple de LogiFresh S.A. usando microservicios reales con Node.js y Express. Cada modulo se ejecuta como un proceso independiente y se comunica por HTTP.

No usa Docker, Docker Compose, base de datos, frontend, TypeScript, autenticacion, RabbitMQ ni Kafka.

## Arquitectura

```text
logifresh-lab9-microservicios/
  pedidos-service/           Puerto 3001
  inventario-service/        Puerto 3002
  facturacion-service/       Puerto 3003
  transporte-service/        Puerto 3004
  notificaciones-service/    Puerto 3005
```

El servicio principal para las pruebas es `pedidos-service`. Este servicio recibe `POST /pedidos` y llama por HTTP a los demas microservicios:

1. `inventario-service`: valida y descuenta stock.
2. `facturacion-service`: genera la factura.
3. `transporte-service`: asigna transporte.
4. `notificaciones-service`: envia la notificacion.

## Requisitos

- Node.js 18 o superior.
- npm.
- k6 para la prueba de rendimiento.
- Visual Studio Code con REST Client para ejecutar `pruebas.http`.

## Instalacion

```bash
npm install
```

## Ejecucion

Abrir cinco terminales en esta carpeta y ejecutar un servicio por terminal:

```bash
npm run start:inventario
```

```bash
npm run start:facturacion
```

```bash
npm run start:transporte
```

```bash
npm run start:notificaciones
```

```bash
npm run start:pedidos
```

El servicio que se prueba desde REST Client y k6 es:

```text
http://localhost:3001
```

## Endpoints principales

Los endpoints se consumen desde `pedidos-service` en el puerto `3001`.

| Metodo | Endpoint | Descripcion |
| --- | --- | --- |
| GET | `/health` | Verifica el servicio de pedidos y muestra los microservicios usados. |
| GET | `/productos` | Consulta productos desde inventario-service. |
| GET | `/pedidos` | Lista pedidos registrados en pedidos-service. |
| GET | `/facturas` | Consulta facturas desde facturacion-service. |
| GET | `/transportes` | Consulta transportes desde transporte-service. |
| GET | `/notificaciones` | Consulta notificaciones desde notificaciones-service. |
| GET | `/metricas` | Agrega metricas de varios microservicios. |
| POST | `/pedidos` | Registra un pedido completo usando varios microservicios. |
| POST | `/pedidos/:id/cancelar` | Cancela un pedido, devuelve stock y notifica. |
| POST | `/facturas/reintento` | Valida idempotencia de facturacion. |
| POST | `/integracion/test` | Ejecuta una prueba de integracion entre microservicios. |

## Pruebas funcionales

1. Iniciar los cinco microservicios.
2. Abrir `pruebas.http`.
3. Ejecutar cada caso con REST Client.

Casos incluidos:

- CP-01 Registro correcto de pedido.
- CP-02 Inventario insuficiente.
- CP-03 Cantidad cero.
- CP-04 Cantidad negativa.
- CP-05 Descuento por volumen.
- CP-06 Descuento VIP y volumen.
- CP-07 Generacion automatica de factura.
- CP-08 Reintento de factura duplicada.
- CP-09 Cancelacion de pedido.
- CP-10 Envio de notificacion.
- Verificacion de productos.
- Verificacion de metricas agregadas.
- Pruebas de integracion con flujo correcto, falla de transporte y falla de notificaciones.

## Prueba de rendimiento con k6

Con los cinco microservicios activos:

```bash
k6 run k6_test.js
```

La prueba usa:

- 100 usuarios concurrentes.
- Duracion de 5 minutos.
- Endpoint `POST http://localhost:3001/pedidos`.
- Cantidad 1 por pedido para no agotar el inventario de `P001`.
- Respuesta esperada 200 o 201.
- Tiempo de respuesta menor a 8000 ms.

## Metricas para la Actividad 4

En la salida final de k6 usar:

- `http_req_duration avg`: tiempo promedio.
- `http_req_duration max`: tiempo maximo.
- `http_req_failed`: porcentaje de errores.
- `http_reqs / 300`: throughput aproximado, porque la prueba dura 300 segundos.

El archivo `k6_test.js` tambien imprime:

```text
Resumen para Actividad 4
Tiempo promedio: ...
Tiempo maximo: ...
Errores: ...
Throughput aproximado: ... req/s
```

## Capturas recomendadas para el informe

- Cinco terminales con los microservicios ejecutandose.
- `GET /health` en `http://localhost:3001`.
- CP-01 registro exitoso.
- CP-02 inventario insuficiente.
- CP-05 descuento por volumen.
- CP-06 descuento VIP y volumen.
- CP-08 reintento de factura sin duplicar.
- CP-09 cancelacion de pedido.
- `GET /metricas`.
- `/integracion/test` exitoso.
- `/integracion/test` con falla de transporte.
- `/integracion/test` con falla de notificaciones.
- Salida final de `k6 run k6_test.js`.

## Relacion con el laboratorio

Esta version si representa microservicios de forma estricta porque cada modulo se ejecuta como servicio independiente en su propio puerto. La integracion ocurre mediante llamadas HTTP entre servicios, lo que permite explicar pruebas funcionales, pruebas de integracion entre componentes y pruebas de rendimiento sobre el servicio orquestador.
