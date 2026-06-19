const express = require("express");

const app = express();
const PORT = 3001;

app.use(express.json());

const INVENTARIO_URL = "http://localhost:3002";
const FACTURACION_URL = "http://localhost:3003";
const TRANSPORTE_URL = "http://localhost:3004";
const NOTIFICACIONES_URL = "http://localhost:3005";

const pedidos = [];
let siguientePedidoId = 1;
let totalErrores = 0;

async function llamarServicio(url, opciones = {}) {
  const respuesta = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...opciones
  });

  const datos = await respuesta.json();

  if (!respuesta.ok) {
    throw new Error(datos.mensaje || "Error al llamar microservicio");
  }

  return datos;
}

function registrarError(modulo, mensaje) {
  totalErrores += 1;
  console.error(`Error de ${modulo}: ${mensaje}`);
}

function responderError(res, status, modulo, mensaje) {
  registrarError(modulo, mensaje);
  return res.status(status).json({ error: true, mensaje });
}

function calcularDescuento(cantidad, tipoCliente) {
  let descuento = 0;

  if (cantidad > 50) {
    descuento += 0.10;
  }

  if (tipoCliente === "VIP") {
    descuento += 0.05;
  }

  console.log("Descuento aplicado");
  return descuento;
}

function crearPedido(datos, producto, descuento) {
  const subtotal = producto.precio * datos.cantidad;
  const total = Number((subtotal * (1 - descuento)).toFixed(2));

  const pedido = {
    id: siguientePedidoId++,
    cliente: datos.cliente,
    tipoCliente: datos.tipoCliente || "Normal",
    productoId: producto.id,
    producto: producto.nombre,
    cantidad: datos.cantidad,
    precioUnitario: producto.precio,
    subtotal,
    descuentoAplicado: Number((descuento * 100).toFixed(2)),
    total,
    estado: "REGISTRADO",
    fecha: new Date().toISOString()
  };

  pedidos.push(pedido);
  return pedido;
}

function buscarPedido(id) {
  return pedidos.find((pedido) => pedido.id === Number(id));
}

async function registrarPedidoCompleto(datos) {
  console.log("Pedido recibido");

  const validacion = await llamarServicio(`${INVENTARIO_URL}/inventario/validar`, {
    method: "POST",
    body: JSON.stringify({
      productoId: datos.productoId,
      cantidad: datos.cantidad
    })
  });

  const producto = validacion.producto;

  await llamarServicio(`${INVENTARIO_URL}/inventario/descontar`, {
    method: "POST",
    body: JSON.stringify({
      productoId: datos.productoId,
      cantidad: datos.cantidad
    })
  });

  const descuento = calcularDescuento(datos.cantidad, datos.tipoCliente);
  const pedido = crearPedido(datos, producto, descuento);

  const factura = await llamarServicio(`${FACTURACION_URL}/facturas`, {
    method: "POST",
    body: JSON.stringify({ pedido })
  });

  const transporte = await llamarServicio(`${TRANSPORTE_URL}/transportes`, {
    method: "POST",
    body: JSON.stringify({
      pedido,
      simularFallaTransporte: datos.simularFallaTransporte === true
    })
  });

  const notificacion = await llamarServicio(`${NOTIFICACIONES_URL}/notificaciones`, {
    method: "POST",
    body: JSON.stringify({
      pedido,
      tipo: "REGISTRO",
      simularFallaNotificacion: datos.simularFallaNotificacion === true
    })
  });

  return { pedido, factura, transporte, notificacion };
}

async function obtenerMetricas() {
  const [inventario, facturacion, transporte, notificaciones] = await Promise.all([
    llamarServicio(`${INVENTARIO_URL}/metricas`),
    llamarServicio(`${FACTURACION_URL}/metricas`),
    llamarServicio(`${TRANSPORTE_URL}/metricas`),
    llamarServicio(`${NOTIFICACIONES_URL}/metricas`)
  ]);

  return {
    totalPedidos: pedidos.length,
    totalFacturas: facturacion.totalFacturas,
    totalTransportes: transporte.totalTransportes,
    totalNotificaciones: notificaciones.totalNotificaciones,
    totalErrores,
    stockActualP001: inventario.stockActualP001
  };
}

app.get("/health", (req, res) => {
  res.json({
    estado: "OK",
    servicio: "Pedidos Service",
    microservicios: {
      inventario: INVENTARIO_URL,
      facturacion: FACTURACION_URL,
      transporte: TRANSPORTE_URL,
      notificaciones: NOTIFICACIONES_URL
    }
  });
});

app.get("/productos", async (req, res) => {
  try {
    res.json(await llamarServicio(`${INVENTARIO_URL}/productos`));
  } catch (error) {
    responderError(res, 500, "inventario", error.message);
  }
});

app.get("/pedidos", (req, res) => {
  res.json(pedidos);
});

app.get("/facturas", async (req, res) => {
  try {
    res.json(await llamarServicio(`${FACTURACION_URL}/facturas`));
  } catch (error) {
    responderError(res, 500, "facturación", error.message);
  }
});

app.get("/transportes", async (req, res) => {
  try {
    res.json(await llamarServicio(`${TRANSPORTE_URL}/transportes`));
  } catch (error) {
    responderError(res, 500, "transporte", error.message);
  }
});

app.get("/notificaciones", async (req, res) => {
  try {
    res.json(await llamarServicio(`${NOTIFICACIONES_URL}/notificaciones`));
  } catch (error) {
    responderError(res, 500, "notificación", error.message);
  }
});

app.get("/metricas", async (req, res) => {
  try {
    res.json(await obtenerMetricas());
  } catch (error) {
    responderError(res, 500, "integración", error.message);
  }
});

app.post("/pedidos", async (req, res) => {
  try {
    const resultado = await registrarPedidoCompleto(req.body);

    res.status(201).json({
      mensaje: "Pedido registrado correctamente",
      pedido: resultado.pedido,
      factura: resultado.factura,
      transporte: resultado.transporte,
      notificacion: resultado.notificacion
    });
  } catch (error) {
    const mensaje = error.message;
    let modulo = "inventario";

    if (mensaje.includes("factura")) {
      modulo = "facturación";
    } else if (mensaje.includes("transporte")) {
      modulo = "transporte";
    } else if (mensaje.includes("notificacion")) {
      modulo = "notificación";
    }

    responderError(res, 400, modulo, mensaje);
  }
});

app.post("/pedidos/:id/cancelar", async (req, res) => {
  const pedido = buscarPedido(req.params.id);

  if (!pedido) {
    return responderError(res, 404, "pedidos", "Pedido inexistente");
  }

  if (pedido.estado === "CANCELADO") {
    return responderError(res, 400, "pedidos", "El pedido ya esta cancelado");
  }

  try {
    pedido.estado = "CANCELADO";

    await llamarServicio(`${INVENTARIO_URL}/inventario/devolver`, {
      method: "POST",
      body: JSON.stringify({
        productoId: pedido.productoId,
        cantidad: pedido.cantidad
      })
    });

    const notificacion = await llamarServicio(`${NOTIFICACIONES_URL}/notificaciones`, {
      method: "POST",
      body: JSON.stringify({
        pedido,
        tipo: "CANCELACION"
      })
    });

    res.json({
      mensaje: "Pedido cancelado correctamente",
      pedido,
      notificacion
    });
  } catch (error) {
    responderError(res, 400, "notificación", error.message);
  }
});

app.post("/facturas/reintento", async (req, res) => {
  try {
    const resultado = await llamarServicio(`${FACTURACION_URL}/facturas/reintento`, {
      method: "POST",
      body: JSON.stringify({ idPedido: req.body.idPedido })
    });

    res.json(resultado);
  } catch (error) {
    responderError(res, 404, "facturación", error.message);
  }
});

app.post("/integracion/test", async (req, res) => {
  const estado = {
    pedido: "OK",
    inventario: "OK",
    facturacion: "OK",
    transporte: "OK",
    notificaciones: "OK"
  };

  try {
    await registrarPedidoCompleto({
      cliente: "Cliente Integracion",
      tipoCliente: "Normal",
      productoId: "P001",
      cantidad: 1,
      simularFallaTransporte: req.body.simularFallaTransporte === true,
      simularFallaNotificacion: req.body.simularFallaNotificacion === true
    });
  } catch (error) {
    if (error.message.includes("transporte")) {
      estado.transporte = "ERROR";
      registrarError("transporte", error.message);
    } else if (error.message.includes("notificacion")) {
      estado.notificaciones = "ERROR";
      registrarError("notificación", error.message);
    } else if (error.message.includes("Producto") || error.message.includes("cantidad") || error.message.includes("Inventario")) {
      estado.inventario = "ERROR";
      registrarError("inventario", error.message);
    } else {
      estado.facturacion = "ERROR";
      registrarError("facturación", error.message);
    }
  }

  res.json(estado);
});

app.listen(PORT, () => {
  console.log(`Pedidos Service ejecutandose en http://localhost:${PORT}`);
});
