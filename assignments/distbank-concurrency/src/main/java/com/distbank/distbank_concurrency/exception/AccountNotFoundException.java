package com.distbank.distbank_concurrency.exception;

/** Excepción lanzada cuando no se encuentra una cuenta bancaria. */
public class AccountNotFoundException extends RuntimeException {
  public AccountNotFoundException(String accountNumber) {
    super("Cuenta no encontrada: " + accountNumber);
  }
}
