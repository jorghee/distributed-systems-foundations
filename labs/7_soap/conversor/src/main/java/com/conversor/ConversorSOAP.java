package com.conversor;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;

@WebService
public interface ConversorSOAP {
    
    @WebMethod
    double cToF(@WebParam(name = "celsius") double c);

    @WebMethod
    double fToC(@WebParam(name = "fahrenheit") double f);
}