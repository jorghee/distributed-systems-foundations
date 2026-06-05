package com.conversor;

import java.net.URL;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

public class Cliente {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://localhost:8081/ws/conversor?wsdl");
        QName qname = new QName("http://conversor.com/", "ConversorSOAPImplService");

        Service servicio = Service.create(url, qname);
        
        // Ahora sí, JAX-WS creará el Proxy sin quejarse porque pasamos una Interfaz
        ConversorSOAP calc = servicio.getPort(ConversorSOAP.class);

        System.out.println("\n=================================");
        System.out.println("   PROBANDO CLIENTE SOAP (UNSA)   ");
        System.out.println("=================================");
        
        double resultadoF = calc.cToF(30);
        System.out.println("-> 30° Celsius a Fahrenheit: " + resultadoF);
        
        double resultadoC = calc.fToC(86);
        System.out.println("-> 86° Fahrenheit a Celsius: " + resultadoC);
        System.out.println("=================================\n");
    }
}