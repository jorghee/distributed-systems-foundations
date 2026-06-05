package com.conversor;

import jakarta.xml.ws.Endpoint;

public class Publicador {
    public static void main(String[] args) {
        String url = "http://localhost:8081/ws/conversor";
        System.out.println("Iniciando el Servicio SOAP de Temperatura...");
        
        // Instanciamos ConversorSOAPImpl
        Endpoint.publish(url, new ConversorSOAPImpl());
        
        System.out.println("Servidor corriendo exitosamente en: " + url);
        System.out.println("WSDL disponible en: " + url + "?wsdl");
    }
}