package com.storeSOAP;

import jakarta.xml.ws.Endpoint;

public class Publicador {
    public static void main(String[] args) {
        String url = "http://localhost:8082/ws/ventas";
        System.out.println("Iniciando el Servicio SOAP de Ventas...");
        
        Endpoint.publish(url, new VentasSOAPImpl());
        
        System.out.println("Servidor de Ventas activo en: " + url);
        System.out.println("WSDL disponible en: " + url + "?wsdl");
    }
}