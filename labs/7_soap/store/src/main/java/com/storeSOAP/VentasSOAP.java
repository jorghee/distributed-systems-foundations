package com.storeSOAP;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;

@WebService
public interface VentasSOAP {
    @WebMethod
    Producto buscarProducto(@WebParam(name = "idProducto") int id);

    @WebMethod
    String procesarVenta(@WebParam(name = "idProducto") int id, @WebParam(name = "cantidad") int cantidad);
}