package com.storeSOAP;

import jakarta.jws.WebService;

@WebService(endpointInterface = "com.storeSOAP.VentasSOAP")
public class VentasSOAPImpl implements VentasSOAP {

    @Override
    public Producto buscarProducto(int id) {
        if (id == 101) return new Producto(101, "Laptop Gamer", 1200.00);
        if (id == 102) return new Producto(102, "Teclado Mecanico", 85.50);
        return null;
    }

    @Override
    public String procesarVenta(int id, int cantidad) {
        Producto prod = buscarProducto(id);
        if (prod == null) return "Error: Producto no encontrado.";
        
        double subtotal = prod.getPrecio() * cantidad;
        double total = subtotal * 1.18; // 18% de impuestos
        
        return String.format("Venta Exitosa: %d x %s. Total a pagar: $%.2f", 
                             cantidad, prod.getNombre(), total);
    }
}