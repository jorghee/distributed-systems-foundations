const express = require("express");

const app = express();
const PORT = 3004;

app.use(express.json());

const transportes = [];
let siguienteTransporteId = 1;

function responderError(res, status, mensaje) {
  console.error(`Error de transporte: ${mensaje}`);
  return res.status(status).json({ error: true, mensaje });
}

app.get("/health", (req, res) => {
  res.json({ estado: "OK", servicio: "Transporte Service" });
});

app.get("/transportes", (req, res) => {
  res.json(transportes);
});

app.post("/transportes", (req, res) => {
  const { pedido, simularFallaTransporte } = req.body;

  if (simularFallaTransporte === true) {
    return responderError(res, 500, "Falla simulada de transporte");
  }

  if (!pedido || !pedido.id) {
    return responderError(res, 400, "Pedido invalido para transporte");
  }

  const transporte = {
    id: siguienteTransporteId++,
    idPedido: pedido.id,
    cliente: pedido.cliente,
    estado: "ASIGNADO",
    fecha: new Date().toISOString()
  };

  transportes.push(transporte);
  console.log("Transporte asignado");
  res.status(201).json(transporte);
});

app.get("/metricas", (req, res) => {
  res.json({ totalTransportes: transportes.length });
});

app.listen(PORT, () => {
  console.log(`Transporte Service ejecutandose en http://localhost:${PORT}`);
});
