const express = require("express");

const app = express();
const PORT = 3003;

app.use(express.json());

const facturas = [];
let siguienteFacturaId = 1;

function responderError(res, status, mensaje) {
  console.error(`Error de facturación: ${mensaje}`);
  return res.status(status).json({ error: true, mensaje });
}

function generarFactura(pedido) {
  const facturaExistente = facturas.find((factura) => factura.idPedido === pedido.id);

  if (facturaExistente) {
    return facturaExistente;
  }

  const factura = {
    id: siguienteFacturaId++,
    idPedido: pedido.id,
    cliente: pedido.cliente,
    subtotal: pedido.subtotal,
    descuentoAplicado: pedido.descuentoAplicado,
    total: pedido.total,
    estado: "GENERADA",
    fecha: new Date().toISOString()
  };

  facturas.push(factura);
  console.log("Factura generada");
  return factura;
}

app.get("/health", (req, res) => {
  res.json({ estado: "OK", servicio: "Facturacion Service" });
});

app.get("/facturas", (req, res) => {
  res.json(facturas);
});

app.post("/facturas", (req, res) => {
  const { pedido } = req.body;

  if (!pedido || !pedido.id) {
    return responderError(res, 400, "Pedido invalido para generar factura");
  }

  const factura = generarFactura(pedido);
  res.status(201).json(factura);
});

app.post("/facturas/reintento", (req, res) => {
  const idPedido = Number(req.body.idPedido);
  const facturaExistente = facturas.find((factura) => factura.idPedido === idPedido);

  if (facturaExistente) {
    return res.json({
      mensaje: "Factura existente devuelta sin duplicar",
      factura: facturaExistente
    });
  }

  responderError(res, 404, "No existe factura para el pedido indicado");
});

app.get("/metricas", (req, res) => {
  res.json({ totalFacturas: facturas.length });
});

app.listen(PORT, () => {
  console.log(`Facturacion Service ejecutandose en http://localhost:${PORT}`);
});
