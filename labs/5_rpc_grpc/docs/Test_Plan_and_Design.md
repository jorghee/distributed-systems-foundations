# Plan de Diseño y Pruebas (ISO/IEEE 29119-3)

## 1. Introducción
El propósito de este plan de pruebas es establecer la estrategia, alcance y metodología para evaluar y comparar de manera cuantitativa el rendimiento de dos arquitecturas de Comunicación de Procedimientos Remotos (RPC): **gRPC** (sobre HTTP/2) y **JSON-RPC** (sobre Sockets TCP). Este documento rige las pruebas de carga y estrés para identificar las diferencias en latencia, capacidad de procesamiento y eficiencia de recursos bajo condiciones controladas.

## 2. Elementos de Prueba (Test Items)
Se evaluarán dos servidores implementados en Java que exponen operaciones matemáticas y de conversión.

| Elemento de Prueba | Protocolo Base | Tecnología de Serialización | Puerto Escucha | Endpoint de Métricas |
| :--- | :--- | :--- | :--- | :--- |
| **Servidor gRPC** | HTTP/2 | Protocol Buffers (Protobuf) | `50051` | `http://localhost:8081/metrics` |
| **Servidor JSON-RPC** | Sockets TCP | JSON (Jackson Databind) | `8080` | `http://localhost:8082/metrics` |

## 3. Características a Probar
Se medirán y analizarán las siguientes características de calidad de software (basadas en la norma ISO/IEC 25010 para Eficiencia de Desempeño):

| Categoría | Métrica Específica | Descripción |
| :--- | :--- | :--- |
| **Latencia de Respuesta** | p90, p95, p99 (ms) | Tiempo de respuesta del servidor en los percentiles más altos, descartando anomalías extremas. |
| **Capacidad (Throughput)** | RPS (Requests per Second) | Tasa de peticiones procesadas exitosamente por segundo bajo carga constante. |
| **Consumo de Memoria** | JVM Heap & Non-Heap (MB) | Monitoreo de memoria dinámica utilizada y actividad del Garbage Collector (GC). |
| **Consumo de CPU** | JVM Processor Usage (%) | Porcentaje de tiempo de procesador asignado a los hilos de los servidores. |
| **Tasa de Errores** | % Error Rate | Porcentaje de respuestas fallidas, timeouts o conexiones rechazadas durante las pruebas. |

## 4. Enfoque de Prueba (Test Approach)
La comparativa utilizará **K6** como inyector de tráfico de alto rendimiento. Las métricas internas de la JVM y de la latencia de las peticiones serán capturadas por **Micrometer**, expuestas en formato **Prometheus** (vía un HttpServer embebido), y visualizadas en un tablero de **Grafana**.

### Tipos de Prueba a Ejecutar
| Tipo de Prueba | Objetivo |
| :--- | :--- |
| **Load Testing (Carga Esperada)** | Simular un volumen de tráfico concurrente realista para evaluar si los servicios mantienen una latencia estable (sin degradación significativa de performance). |
| **Stress Testing (Prueba de Estrés)** | Escalar drásticamente los Virtual Users (VUs) para saturar los servidores, provocando encolamiento y forzando la recolección de basura intensiva con la finalidad de encontrar el punto de quiebre (bottleneck). |

## 5. Diseño y Escenarios de Prueba
Se han definido los siguientes escenarios automatizados en el script `script.js`, ejecutados de forma aislada para cada servidor:

| Escenario | Concurrencia (VUs) | Duración | Payload / Operación | Objetivo Específico |
| :--- | :--- | :--- | :--- | :--- |
| **1. Carga Base (Warm-up)** | 10 VUs | 30 segundos | `Convert (c_f: 25.0)` / `multiply (4,5)` | Calentar la JVM y forzar compilación JIT (Just-In-Time) estabilizando latencias iniciales. |
| **2. Carga Sostenida** | 50 VUs | 2 minutos | Operaciones matemáticas ligeras | Medir el Throughput estable (RPS) y percentiles p95 de latencia bajo una carga típica de red. |
| **3. Prueba de Estrés** | 300 VUs | 1 minuto | Operaciones continuas concurrentes | Evaluar el consumo máximo de Heap RAM, saturación de CPU y tasa de rechazo de conexiones TCP. |

## 6. Entorno de Prueba
Para asegurar la reproducibilidad de los resultados, el entorno de ejecución debe cumplir los siguientes requisitos mínimos:

| Componente | Requisito / Versión Mínima |
| :--- | :--- |
| **Sistema Operativo** | Linux / Windows / macOS |
| **Entorno de Ejecución** | OpenJDK 17 o superior |
| **Gestor de Paquetes** | Apache Maven 3.8+ |
| **Herramienta de Carga** | Grafana K6 (con la extensión `k6/x/tcp` instalada para TCP puro) |
| **Observabilidad** | Docker Engine (para levantar los contenedores de Prometheus y Grafana) |
