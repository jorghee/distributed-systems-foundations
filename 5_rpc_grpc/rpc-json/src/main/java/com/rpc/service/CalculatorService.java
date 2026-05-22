package com.rpc.service;

public class CalculatorService {

  public Double multiply(Double a, Double b) {
    return a * b;
  }

  public Double divide(Double a, Double b) {
    if (b == 0) {
      throw new IllegalArgumentException("Division por cero no esta permitida.");
    }
    return a / b;
  }

  public Double power(Double base, Double exponent) {
    return Math.pow(base, exponent);
  }
}
