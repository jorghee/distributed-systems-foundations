import http from "k6/http";
import { check } from "k6";

export const options = {
  vus: 100,
  duration: "5m",
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["avg<8000"]
  }
};

export default function () {
  const payload = JSON.stringify({
    cliente: "Cliente K6 Microservicios",
    tipoCliente: "Normal",
    productoId: "P001",
    cantidad: 1
  });

  const params = {
    headers: {
      "Content-Type": "application/json"
    }
  };

  const respuesta = http.post("http://localhost:3001/pedidos", payload, params);

  check(respuesta, {
    "respuesta 200 o 201": (r) => r.status === 200 || r.status === 201,
    "tiempo menor a 8000 ms": (r) => r.timings.duration < 8000
  });
}

export function handleSummary(data) {
  const totalRequests = data.metrics.http_reqs.values.count;
  const throughput = totalRequests / 300;

  console.log("Resumen para Actividad 4");
  console.log(`Tiempo promedio: ${data.metrics.http_req_duration.values.avg.toFixed(2)} ms`);
  console.log(`Tiempo maximo: ${data.metrics.http_req_duration.values.max.toFixed(2)} ms`);
  console.log(`Errores: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);
  console.log(`Throughput aproximado: ${throughput.toFixed(2)} req/s`);

  return {};
}
