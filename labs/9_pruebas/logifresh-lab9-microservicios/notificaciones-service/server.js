const express = require("express");

const app = express();
const PORT = 3005;

app.use(express.json());

const notificaciones = [];
let siguienteNotificacionId = 1;

function responderError(res, status, mensaje) {
  console.error(`Error de notificación: ${mensaje}`);
  return res.status(status).json({ error: true, mensaje });
}

app.get("/health", (req, res) => {
  res.json({ estado: "OK", servicio: "Notificaciones Service" });
});

app.get("/notificaciones", (req, res) => {
  res.json(notificaciones);
});

app.post("/notificaciones", (req, res) => {
  const { pedido, tipo, simularFallaNotificacion } = req.body;

  if (simularFallaNotificacion === true) {
    return responderError(res, 500, "Falla simulada de notificacion");
  }

  if (!pedido || !pedido.id) {
    return responderError(res, 400, "Pedido invalido para notificacion");
  }

  const notificacion = {
    id: siguienteNotificacionId++,
    idPedido: pedido.id,
    cliente: pedido.cliente,
    tipo: tipo || "REGISTRO",
    mensaje: tipo === "CANCELACION"
      ? `Pedido ${pedido.id} cancelado correctamente`
      : `Pedido ${pedido.id} registrado correctamente`,
    estado: "ENVIADO",
    fecha: new Date().toISOString()
  };

  notificaciones.push(notificacion);
  console.log("Notificación enviada");
  res.status(201).json(notificacion);
});

app.get("/metricas", (req, res) => {
  res.json({ totalNotificaciones: notificaciones.length });
});

app.listen(PORT, () => {
  console.log(`Notificaciones Service ejecutandose en http://localhost:${PORT}`);
});
