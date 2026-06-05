package com.conversor;

import jakarta.jws.WebService;

// Le indicamos cuál es la interfaz del servicio web
@WebService(endpointInterface = "com.conversor.ConversorSOAP")
public class ConversorSOAPImpl implements ConversorSOAP {

    @Override
    public double cToF(double c) {
        return (c * 9 / 5) + 32;
    }

    @Override
    public double fToC(double f) {
        return (f - 32) * 5 / 9;
    }
}