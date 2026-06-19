const express = require("express");

const app = express();
const PORT = 3002;

app.use(express.json());

const productos = [
  { id: "P001", nombre: "Yogurt refrigerado", stock: 100000, precio: 10 },
  { id: "P002", nombre: "Queso fresco", stock: 50, precio: 15 },
  { id: "P003", nombre: "Leche refrigerada", stock: 0, precio: 8 }
];

function buscarProducto(productoId) {
  return productos.find((producto) => producto.id === productoId);
}

function responderError(res, status, mensaje) {
  console.error(`Error de inventario: ${mensaje}`);
  return res.status(status).json({ error: true, mensaje });
}

app.get("/health", (req, res) => {
  res.json({ estado: "OK", servicio: "Inventario Service" });
});

app.get("/productos", (req, res) => {
  res.json(productos);
});

app.post("/inventario/validar", (req, res) => {
  const { productoId, cantidad } = req.body;
  const producto = buscarProducto(productoId);

  if (!producto) {
    return responderError(res, 404, "Producto inexistente");
  }

  if (typeof cantidad !== "number" || cantidad <= 0) {
    return responderError(res, 400, "La cantidad debe ser mayor a cero");
  }

  if (producto.stock < cantidad) {
    return responderError(res, 400, "Inventario insuficiente");
  }

  console.log("Inventario validado");
  res.json({ mensaje: "Inventario validado", producto });
});

app.post("/inventario/descontar", (req, res) => {
  const { productoId, cantidad } = req.body;
  const producto = buscarProducto(productoId);

  if (!producto) {
    return responderError(res, 404, "Producto inexistente");
  }

  if (typeof cantidad !== "number" || cantidad <= 0) {
    return responderError(res, 400, "La cantidad debe ser mayor a cero");
  }

  if (producto.stock < cantidad) {
    return responderError(res, 400, "Inventario insuficiente");
  }

  producto.stock -= cantidad;
  console.log("Stock descontado");
  res.json({ mensaje: "Stock descontado", producto });
});

app.post("/inventario/devolver", (req, res) => {
  const { productoId, cantidad } = req.body;
  const producto = buscarProducto(productoId);

  if (!producto) {
    return responderError(res, 404, "Producto inexistente");
  }

  producto.stock += cantidad;
  res.json({ mensaje: "Stock devuelto", producto });
});

app.get("/metricas", (req, res) => {
  const productoP001 = buscarProducto("P001");
  res.json({ stockActualP001: productoP001 ? productoP001.stock : 0 });
});

app.listen(PORT, () => {
  console.log(`Inventario Service ejecutandose en http://localhost:${PORT}`);
});
