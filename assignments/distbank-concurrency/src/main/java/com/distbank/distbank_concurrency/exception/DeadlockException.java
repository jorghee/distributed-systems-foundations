package com.distbank.distbank_concurrency.exception;

/** Excepción lanzada cuando PostgreSQL detecta un deadlock y aborta una de las transacciones. */
public class DeadlockException extends RuntimeException {
  private final String operationDescription;

  public DeadlockException(String operationDescription, Throwable cause) {
    super("Deadlock detectado durante: " + operationDescription, cause);
    this.operationDescription = operationDescription;
  }

  public String getOperationDescription() {
    return operationDescription;
  }
}
