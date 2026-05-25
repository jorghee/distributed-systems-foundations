package com.distbank.distbank_concurrency.websocket;

import com.distbank.distbank_concurrency.dto.BankingDtos.*;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica eventos de concurrencia por WebSocket (STOMP) hacia la UI. Cada evento incluye el thread
 * name para que la UI pueda distinguir visualmente qué hilo está en qué estado.
 */
@Component
public class ConcurrencyEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(ConcurrencyEventPublisher.class);
  private static final String TOPIC = "/topic/concurrency";
  private static final String TOPIC_ACCOUNTS = "/topic/accounts";

  private final SimpMessagingTemplate messaging;

  public ConcurrencyEventPublisher(SimpMessagingTemplate messaging) {
    this.messaging = messaging;
  }

  public void publishTransactionStarted(
      String referenceId, String type, String accountNumber, java.math.BigDecimal amount) {
    publish(
        Map.of(
            "eventType", "TX_STARTED",
            "referenceId", referenceId,
            "transactionType", type,
            "accountNumber", accountNumber,
            "amount", amount,
            "thread", Thread.currentThread().getName(),
            "timestamp", LocalDateTime.now().toString()));
  }

  public void publishLockAcquired(
      String referenceId, String accountNumber, long lockWaitNanos, boolean hadContention) {
    publish(
        Map.of(
            "eventType",
            "LOCK_ACQUIRED",
            "referenceId",
            referenceId,
            "accountNumber",
            accountNumber,
            "lockWaitNanos",
            lockWaitNanos,
            "lockWaitMillis",
            lockWaitNanos / 1_000_000.0,
            "hadContention",
            hadContention,
            "thread",
            Thread.currentThread().getName(),
            "timestamp",
            LocalDateTime.now().toString()));
  }

  public void publishTransactionCommitted(OperationResult result) {
    var payload = new java.util.HashMap<String, Object>();
    payload.put("eventType", "TX_COMMITTED");
    payload.put("referenceId", result.referenceId());
    payload.put("transactionType", result.operationType());
    payload.put("accountNumber", result.accountNumber());
    payload.put("balanceBefore", result.balanceBefore());
    payload.put("balanceAfter", result.balanceAfter());
    payload.put("lockWaitMillis", result.lockWaitMillis());
    payload.put("hadContention", result.hadContention());
    payload.put("durationMillis", result.transactionDurationMillis());
    payload.put("thread", result.threadName());
    payload.put("timestamp", result.timestamp().toString());
    publish(payload);
    // También notificamos el snapshot de cuenta actualizado
    messaging.convertAndSend(
        TOPIC_ACCOUNTS,
        (Object)
            Map.of(
                "accountNumber", result.accountNumber(),
                "newBalance", result.balanceAfter()));
  }

  public void publishTransactionFailed(
      String referenceId,
      String type,
      String accountNumber,
      String errorMessage,
      String errorType) {
    publish(
        Map.of(
            "eventType",
            "TX_FAILED",
            "referenceId",
            referenceId != null ? referenceId : "unknown",
            "transactionType",
            type,
            "accountNumber",
            accountNumber,
            "errorMessage",
            errorMessage,
            "errorType",
            errorType,
            "thread",
            Thread.currentThread().getName(),
            "timestamp",
            LocalDateTime.now().toString()));
  }

  public void publishDeadlockDetected(String referenceId, String operation, String accounts) {
    publish(
        Map.of(
            "eventType",
            "DEADLOCK",
            "referenceId",
            referenceId,
            "operation",
            operation,
            "accounts",
            accounts,
            "thread",
            Thread.currentThread().getName(),
            "timestamp",
            LocalDateTime.now().toString()));
    log.warn("[WS] Deadlock publicado → ref={} cuentas={}", referenceId, accounts);
  }

  public void publishLoadSimulationStarted(ConcurrentLoadRequest request) {
    publish(
        Map.of(
            "eventType", "LOAD_STARTED",
            "accountNumber", request.accountNumber(),
            "threadCount", request.threadCount(),
            "amountPerThread", request.amountPerThread(),
            "operationType", request.operationType(),
            "timestamp", LocalDateTime.now().toString()));
  }

  public void publishLoadSimulationCompleted(ConcurrentLoadResult result) {
    publish(
        Map.of(
            "eventType", "LOAD_COMPLETED",
            "totalRequested", result.totalRequested(),
            "successCount", result.successCount(),
            "failureCount", result.failureCount(),
            "contentionCount", result.contentionCount(),
            "avgLockWaitMillis", result.avgLockWaitMillis(),
            "maxLockWaitMillis", result.maxLockWaitMillis(),
            "totalDurationMillis", result.totalDurationMillis(),
            "finalBalance", result.finalBalance(),
            "timestamp", LocalDateTime.now().toString()));
  }

  private void publish(Object payload) {
    try {
      messaging.convertAndSend(TOPIC, payload);
    } catch (Exception e) {
      log.warn("[WS] No se pudo publicar evento WebSocket: {}", e.getMessage());
    }
  }
}
