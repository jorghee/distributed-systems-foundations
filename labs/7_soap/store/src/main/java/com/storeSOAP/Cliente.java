package com.storeSOAP;

import java.net.URL;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

public class Cliente {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://localhost:8082/ws/ventas?wsdl");
        QName qname = new QName("http://storeSOAP.com/", "VentasSOAPImplService");

        Service servicio = Service.create(url, qname);
        VentasSOAP tienda = servicio.getPort(VentasSOAP.class);

        System.out.println("\n=================================");
        System.out.println("   PROBANDO TIENDA SOAP (UNSA)   ");
        System.out.println("=================================");
        
        Producto p = tienda.buscarProducto(101);
        System.out.println("-> Producto Encontrado: " + p.getNombre() + " - $" + p.getPrecio());
        
        String recibo = tienda.procesarVenta(101, 2);
        System.out.println("-> " + recibo);
        System.out.println("=================================\n");
    }
}