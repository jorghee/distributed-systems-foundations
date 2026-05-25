package com.distbank.distbank_concurrency.exception;

import java.math.BigDecimal;

/**
 * Excepción lanzada cuando el saldo de la cuenta es insuficiente para completar la operación
 * solicitada.
 */
public class InsufficientFundsException extends RuntimeException {
  private final BigDecimal currentBalance;
  private final BigDecimal requestedAmount;

  public InsufficientFundsException(BigDecimal currentBalance, BigDecimal requestedAmount) {
    super(
        String.format(
            "Saldo insuficiente: disponible=%.2f solicitado=%.2f",
            currentBalance, requestedAmount));
    this.currentBalance = currentBalance;
    this.requestedAmount = requestedAmount;
  }

  public BigDecimal getCurrentBalance() {
    return currentBalance;
  }

  public BigDecimal getRequestedAmount() {
    return requestedAmount;
  }
}
