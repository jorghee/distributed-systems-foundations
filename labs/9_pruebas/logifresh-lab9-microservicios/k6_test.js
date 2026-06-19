import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Counter, Rate } from "k6/metrics";

// ─── Métricas personalizadas ───────────────────────────────────────────────────
const pedidoDuration = new Trend("pedido_duration", true);
const pedidoErrors = new Counter("pedido_errors");
const pedidoSuccessRate = new Rate("pedido_success_rate");

const healthDuration = new Trend("health_duration", true);
const productosDuration = new Trend("productos_duration", true);
const pedidosGetDuration = new Trend("pedidos_get_duration", true);

// ─── Configuración de la prueba ────────────────────────────────────────────────
export const options = {
  scenarios: {
    // Escenario principal: Crear pedidos (flujo crítico del sistema)
    crear_pedidos: {
      executor: "constant-vus",
      vus: 80,
      duration: "5m",
      exec: "crearPedido",
      tags: { scenario: "crear_pedidos" },
    },
    // Escenario secundario: Consultas GET (lectura concurrente)
    consultas: {
      executor: "constant-vus",
      vus: 20,
      duration: "5m",
      exec: "consultarEndpoints",
      tags: { scenario: "consultas" },
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.05"],               // Menos del 5% de errores
    http_req_duration: ["avg<8000", "p(95)<15000"], // Promedio < 8s, p95 < 15s
    pedido_duration: ["avg<10000"],                // POST /pedidos promedio < 10s
    pedido_success_rate: ["rate>0.95"],            // >= 95% de pedidos exitosos
  },
};

// ─── Constantes ────────────────────────────────────────────────────────────────
const BASE_URL = "http://localhost:3001";
const HEADERS = { headers: { "Content-Type": "application/json" } };

const clientes = [
  "Supermercado Sur", "Supermercado Norte", "Supermercado Este",
  "Supermercado Oeste", "Supermercado Centro", "Distribuidora La Paz",
  "Almacén Don Pedro", "Tienda Express", "MiniMarket Plus", "Bodega Central"
];

const tiposCliente = ["Normal", "VIP"];

// ─── Escenario 1: Crear pedidos (POST /pedidos) ────────────────────────────────
export function crearPedido() {
  const cliente = clientes[Math.floor(Math.random() * clientes.length)];
  const tipoCliente = tiposCliente[Math.floor(Math.random() * tiposCliente.length)];

  const payload = JSON.stringify({
    cliente: cliente,
    tipoCliente: tipoCliente,
    productoId: "P001",
    cantidad: 1,
  });

  const respuesta = http.post(`${BASE_URL}/pedidos`, payload, HEADERS);

  const exitoso = check(respuesta, {
    "POST /pedidos - status 200 o 201": (r) => r.status === 200 || r.status === 201,
    "POST /pedidos - tiempo < 8000ms": (r) => r.timings.duration < 8000,
    "POST /pedidos - respuesta contiene pedido": (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.pedido && body.pedido.id;
      } catch {
        return false;
      }
    },
  });

  pedidoDuration.add(respuesta.timings.duration);
  pedidoSuccessRate.add(exitoso);

  if (!exitoso) {
    pedidoErrors.add(1);
  }

  sleep(Math.random() * 0.5 + 0.1); // Pausa entre 100ms y 600ms para simular usuario real
}

// ─── Escenario 2: Consultas GET ────────────────────────────────────────────────
export function consultarEndpoints() {
  // GET /health
  const resHealth = http.get(`${BASE_URL}/health`);
  check(resHealth, {
    "GET /health - status 200": (r) => r.status === 200,
  });
  healthDuration.add(resHealth.timings.duration);

  sleep(0.5);

  // GET /productos
  const resProductos = http.get(`${BASE_URL}/productos`);
  check(resProductos, {
    "GET /productos - status 200": (r) => r.status === 200,
  });
  productosDuration.add(resProductos.timings.duration);

  sleep(0.5);

  // GET /pedidos
  const resPedidos = http.get(`${BASE_URL}/pedidos`);
  check(resPedidos, {
    "GET /pedidos - status 200": (r) => r.status === 200,
  });
  pedidosGetDuration.add(resPedidos.timings.duration);

  sleep(Math.random() * 1 + 0.5);
}

// ─── Resumen personalizado ─────────────────────────────────────────────────────
export function handleSummary(data) {
  const duracionPrueba = 300; // 5 minutos en segundos

  const httpDuration = data.metrics.http_req_duration;
  const httpReqs = data.metrics.http_reqs;
  const httpFailed = data.metrics.http_req_failed;
  const checks = data.metrics.checks;

  const totalRequests = httpReqs ? httpReqs.values.count : 0;
  const avgDuration = httpDuration ? httpDuration.values.avg : 0;
  const maxDuration = httpDuration ? httpDuration.values.max : 0;
  const minDuration = httpDuration ? httpDuration.values.min : 0;
  const medDuration = httpDuration ? httpDuration.values.med : 0;
  const p90Duration = httpDuration ? httpDuration.values["p(90)"] : 0;
  const p95Duration = httpDuration ? httpDuration.values["p(95)"] : 0;
  const errorRate = httpFailed ? httpFailed.values.rate : 0;
  const errorCount = httpFailed ? Math.round(httpFailed.values.rate * totalRequests) : 0;
  const throughput = totalRequests / duracionPrueba;
  const checksPassRate = checks ? checks.values.rate : 0;

  // Métricas del escenario POST /pedidos
  const pedidoMetrics = data.metrics.pedido_duration;
  const pedidoAvg = pedidoMetrics ? pedidoMetrics.values.avg : 0;
  const pedidoMax = pedidoMetrics ? pedidoMetrics.values.max : 0;
  const pedidoP95 = pedidoMetrics ? pedidoMetrics.values["p(95)"] : 0;
  const pedidoP90 = pedidoMetrics ? pedidoMetrics.values["p(90)"] : 0;

  // Métricas de los endpoints GET
  const healthMetrics = data.metrics.health_duration;
  const productosMetrics = data.metrics.productos_duration;
  const pedidosGetMetrics = data.metrics.pedidos_get_duration;

  console.log("\n╔══════════════════════════════════════════════════════════════════╗");
  console.log("║        REPORTE DE PRUEBA DE RENDIMIENTO - LogiFresh S.A.       ║");
  console.log("╠══════════════════════════════════════════════════════════════════╣");
  console.log("║  Herramienta: k6 v0.56.0                                       ║");
  console.log("║  Usuarios virtuales: 100 (80 POST + 20 GET)                    ║");
  console.log("║  Duración: 5 minutos                                           ║");
  console.log("╠══════════════════════════════════════════════════════════════════╣");
  console.log("║                    MÉTRICAS GENERALES                           ║");
  console.log("╠══════════════════════════════════════════════════════════════════╣");
  console.log(`║  Total de solicitudes:     ${totalRequests.toString().padEnd(36)}║`);
  console.log(`║  Requests por segundo:     ${throughput.toFixed(2).padEnd(36)}║`);
  console.log(`║  Tiempo promedio:          ${avgDuration.toFixed(2).padEnd(30)} ms ║`);
  console.log(`║  Tiempo mínimo:            ${minDuration.toFixed(2).padEnd(30)} ms ║`);
  console.log(`║  Mediana:                  ${medDuration.toFixed(2).padEnd(30)} ms ║`);
  console.log(`║  Tiempo máximo:            ${maxDuration.toFixed(2).padEnd(30)} ms ║`);
  console.log(`║  Percentil 90 (p90):       ${p90Duration.toFixed(2).padEnd(30)} ms ║`);
  console.log(`║  Percentil 95 (p95):       ${p95Duration.toFixed(2).padEnd(30)} ms ║`);
  console.log(`║  Tasa de errores:          ${(errorRate * 100).toFixed(2)}%  (${errorCount} solicitudes fallidas)`);
  console.log(`║  Tasa de checks exitosos:  ${(checksPassRate * 100).toFixed(2)}%`);
  console.log("╠══════════════════════════════════════════════════════════════════╣");
  console.log("║              MÉTRICAS POR ESCENARIO                             ║");
  console.log("╠══════════════════════════════════════════════════════════════════╣");
  console.log("║  >> POST /pedidos (Flujo crítico)                               ║");
  console.log(`║     Promedio:   ${pedidoAvg.toFixed(2)} ms`);
  console.log(`║     Máximo:     ${pedidoMax.toFixed(2)} ms`);
  console.log(`║     p90:        ${pedidoP90.toFixed(2)} ms`);
  console.log(`║     p95:        ${pedidoP95.toFixed(2)} ms`);
  console.log("║                                                                 ║");
  console.log("║  >> GET /health                                                 ║");
  console.log(`║     Promedio:   ${healthMetrics ? healthMetrics.values.avg.toFixed(2) : "N/A"} ms`);
  console.log(`║     p95:        ${healthMetrics ? healthMetrics.values["p(95)"].toFixed(2) : "N/A"} ms`);
  console.log("║                                                                 ║");
  console.log("║  >> GET /productos                                              ║");
  console.log(`║     Promedio:   ${productosMetrics ? productosMetrics.values.avg.toFixed(2) : "N/A"} ms`);
  console.log(`║     p95:        ${productosMetrics ? productosMetrics.values["p(95)"].toFixed(2) : "N/A"} ms`);
  console.log("║                                                                 ║");
  console.log("║  >> GET /pedidos                                                ║");
  console.log(`║     Promedio:   ${pedidosGetMetrics ? pedidosGetMetrics.values.avg.toFixed(2) : "N/A"} ms`);
  console.log(`║     p95:        ${pedidosGetMetrics ? pedidosGetMetrics.values["p(95)"].toFixed(2) : "N/A"} ms`);
  console.log("╚══════════════════════════════════════════════════════════════════╝\n");

  // Exportar JSON con todas las métricas para el análisis posterior
  const summaryJson = {
    timestamp: new Date().toISOString(),
    configuracion: {
      herramienta: "k6 v0.56.0",
      vus: 100,
      duracion: "5 minutos",
      escenarios: {
        crear_pedidos: { vus: 80, executor: "constant-vus" },
        consultas: { vus: 20, executor: "constant-vus" },
      },
    },
    metricas_generales: {
      total_requests: totalRequests,
      requests_por_segundo: Number(throughput.toFixed(2)),
      tiempo_promedio_ms: Number(avgDuration.toFixed(2)),
      tiempo_minimo_ms: Number(minDuration.toFixed(2)),
      mediana_ms: Number(medDuration.toFixed(2)),
      tiempo_maximo_ms: Number(maxDuration.toFixed(2)),
      p90_ms: Number(p90Duration.toFixed(2)),
      p95_ms: Number(p95Duration.toFixed(2)),
      tasa_errores_porcentaje: Number((errorRate * 100).toFixed(2)),
      errores_totales: errorCount,
      checks_exitosos_porcentaje: Number((checksPassRate * 100).toFixed(2)),
    },
    metricas_post_pedidos: {
      promedio_ms: Number(pedidoAvg.toFixed(2)),
      maximo_ms: Number(pedidoMax.toFixed(2)),
      p90_ms: Number(pedidoP90.toFixed(2)),
      p95_ms: Number(pedidoP95.toFixed(2)),
    },
    metricas_get_health: healthMetrics ? {
      promedio_ms: Number(healthMetrics.values.avg.toFixed(2)),
      maximo_ms: Number(healthMetrics.values.max.toFixed(2)),
      p95_ms: Number(healthMetrics.values["p(95)"].toFixed(2)),
    } : null,
    metricas_get_productos: productosMetrics ? {
      promedio_ms: Number(productosMetrics.values.avg.toFixed(2)),
      maximo_ms: Number(productosMetrics.values.max.toFixed(2)),
      p95_ms: Number(productosMetrics.values["p(95)"].toFixed(2)),
    } : null,
    metricas_get_pedidos: pedidosGetMetrics ? {
      promedio_ms: Number(pedidosGetMetrics.values.avg.toFixed(2)),
      maximo_ms: Number(pedidosGetMetrics.values.max.toFixed(2)),
      p95_ms: Number(pedidosGetMetrics.values["p(95)"].toFixed(2)),
    } : null,
    thresholds: data.root_group ? data.root_group : {},
  };

  return {
    "k6_results.json": JSON.stringify(summaryJson, null, 2),
    stdout: "",  // Suprime el resumen por defecto, ya lo imprimimos arriba
  };
}
