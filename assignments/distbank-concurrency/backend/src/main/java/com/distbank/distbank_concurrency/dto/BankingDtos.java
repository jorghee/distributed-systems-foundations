package com.distbank.distbank_concurrency.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class BankingDtos {

  private BankingDtos() {}

  // Requests

  /** Solicitud de depósito o retiro en una cuenta. */
  public record DepositRequest(String accountNumber, BigDecimal amount) {}

  public record WithdrawalRequest(String accountNumber, BigDecimal amount) {}

  /**
   * Solicitud de simulación de carga concurrente. La UI envía este comando y el backend lanza N
   * hilos simultáneos.
   */
  public record ConcurrentLoadRequest(
      String accountNumber, int threadCount, BigDecimal amountPerThread, String operationType) {}

  // Responses

  /**
   * Resultado de una operación bancaria individual. Contiene tanto el resultado de negocio como las
   * métricas de concurrencia.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record OperationResult(
      boolean success,
      String operationType,
      String referenceId,
      String accountNumber,
      BigDecimal amountProcessed,
      BigDecimal balanceBefore,
      BigDecimal balanceAfter,
      long lockWaitNanos,
      double lockWaitMillis,
      boolean hadContention,
      long transactionDurationNanos,
      double transactionDurationMillis,
      String threadName,
      LocalDateTime timestamp,
      String errorMessage,
      String errorType) {

    /** Factory para operación exitosa. */
    public static OperationResult success(
        String operationType,
        String referenceId,
        String accountNumber,
        BigDecimal amountProcessed,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        long lockWaitNanos,
        long txDurationNanos) {
      return new OperationResult(
          true,
          operationType,
          referenceId,
          accountNumber,
          amountProcessed,
          balanceBefore,
          balanceAfter,
          lockWaitNanos,
          lockWaitNanos / 1_000_000.0,
          lockWaitNanos > 1_000_000L,
          txDurationNanos,
          txDurationNanos / 1_000_000.0,
          Thread.currentThread().getName(),
          LocalDateTime.now(),
          null,
          null);
    }

    /** Factory para operación fallida. */
    public static OperationResult failure(
        String operationType,
        String accountNumber,
        BigDecimal amountAttempted,
        String errorMessage,
        String errorType,
        long txDurationNanos) {
      return new OperationResult(
          false,
          operationType,
          null,
          accountNumber,
          amountAttempted,
          null,
          null,
          0,
          0.0,
          false,
          txDurationNanos,
          txDurationNanos / 1_000_000.0,
          Thread.currentThread().getName(),
          LocalDateTime.now(),
          errorMessage,
          errorType);
    }
  }

  /** Snapshot del saldo actual de una cuenta. */
  public record AccountSnapshot(
      Long id, String accountNumber, BigDecimal balance, LocalDateTime createdAt) {}

  /** Entrada individual del ledger contable. */
  public record LedgerEntryDto(
      Long id,
      String accountNumber,
      BigDecimal amount,
      String transactionType,
      String referenceId,
      LocalDateTime createdAt) {}

  /**
   * Resultado de una simulación de carga concurrente. Agrega los resultados de todos los hilos para
   * análisis.
   */
  public record ConcurrentLoadResult(
      int totalRequested,
      int successCount,
      int failureCount,
      int contentionCount,
      double avgLockWaitMillis,
      double maxLockWaitMillis,
      double totalDurationMillis,
      BigDecimal finalBalance,
      java.util.List<OperationResult> results) {}

  /** Respuesta de error estandarizada para la API. */
  public record ApiError(
      int status, String error, String message, String path, LocalDateTime timestamp) {}
}
