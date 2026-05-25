package com.distbank.distbank_concurrency.service;

import com.distbank.distbank_concurrency.dto.BankingDtos.*;
import com.distbank.distbank_concurrency.exception.*;
import com.distbank.distbank_concurrency.model.Account;
import com.distbank.distbank_concurrency.model.LedgerEntry;
import com.distbank.distbank_concurrency.repository.AccountRepository;
import com.distbank.distbank_concurrency.repository.LedgerEntryRepository;
import com.distbank.distbank_concurrency.websocket.ConcurrencyEventPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Servicio central del sistema bancario distribuido. */
@Service
public class AccountService {

  private static final Logger log = LoggerFactory.getLogger(AccountService.class);

  private final AccountRepository accountRepository;
  private final LedgerEntryRepository ledgerEntryRepository;
  private final TransactionTemplate transactionTemplate;
  private final ConcurrencyEventPublisher eventPublisher;

  public AccountService(
      AccountRepository accountRepository,
      LedgerEntryRepository ledgerEntryRepository,
      PlatformTransactionManager transactionManager,
      ConcurrencyEventPublisher eventPublisher) {
    this.accountRepository = accountRepository;
    this.ledgerEntryRepository = ledgerEntryRepository;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.eventPublisher = eventPublisher;
  }

  // CONSULTAS (sin transacción, sin lock)

  public AccountSnapshot getAccount(String accountNumber) {
    return accountRepository
        .findByAccountNumber(accountNumber)
        .map(this::toSnapshot)
        .orElseThrow(() -> new AccountNotFoundException(accountNumber));
  }

  public List<AccountSnapshot> getAllAccounts() {
    return accountRepository.findAll().stream().map(this::toSnapshot).toList();
  }

  public List<LedgerEntryDto> getLedger(String accountNumber) {
    return ledgerEntryRepository.findByAccountNumber(accountNumber).stream()
        .map(this::toLedgerDto)
        .toList();
  }

  // DEPÓSITO: transacción con lock y métricas

  /** Realiza un depósito con control explícito de transacción y lock. */
  public OperationResult deposit(String accountNumber, BigDecimal amount) {
    validatePositiveAmount(amount);
    String referenceId = generateReferenceId("DEP");
    long txStart = System.nanoTime();

    log.info(
        "[TX] BEGIN depósito: account={} amount={} ref={} thread={}",
        accountNumber,
        amount,
        referenceId,
        Thread.currentThread().getName());

    eventPublisher.publishTransactionStarted(referenceId, "DEPOSIT", accountNumber, amount);

    try {
      OperationResult result =
          transactionTemplate.execute(
              status -> {
                // Adquirir lock exclusivo (puede bloquear aquí si hay contención)
                var lockResult = accountRepository.findByAccountNumberForUpdate(accountNumber);

                log.info(
                    "[TX] LOCK ACQUIRED: account={} wait={}ms contention={}",
                    accountNumber,
                    String.format("%.2f", lockResult.lockWaitMillis()),
                    lockResult.hadContention());

                eventPublisher.publishLockAcquired(
                    referenceId,
                    accountNumber,
                    lockResult.lockWaitNanos(),
                    lockResult.hadContention());

                Account account =
                    lockResult
                        .account()
                        .orElseThrow(() -> new AccountNotFoundException(accountNumber));

                BigDecimal balanceBefore = account.getBalance();
                BigDecimal balanceAfter = balanceBefore.add(amount);

                // Actualizar saldo
                accountRepository.updateBalance(account.getId(), balanceAfter);
                log.info(
                    "[TX] BALANCE UPDATE: id={} {}: {}",
                    account.getId(),
                    balanceBefore,
                    balanceAfter);

                // Registrar en ledger
                LedgerEntry entry =
                    LedgerEntry.builder()
                        .account(account)
                        .amount(amount)
                        .transactionType("DEPOSIT")
                        .referenceId(referenceId)
                        .createdAt(LocalDateTime.now())
                        .build();
                ledgerEntryRepository.save(entry);
                log.info("[TX] LEDGER INSERT: ref={} amount=+{}", referenceId, amount);

                long txDuration = System.nanoTime() - txStart;
                return OperationResult.success(
                    "DEPOSIT",
                    referenceId,
                    accountNumber,
                    amount,
                    balanceBefore,
                    balanceAfter,
                    lockResult.lockWaitNanos(),
                    txDuration);
                // TransactionTemplate emite COMMIT aquí al retornar sin excepción
              });

      log.info(
          "[TX] COMMIT: ref={} duration={}ms",
          referenceId,
          String.format("%.2f", result.transactionDurationMillis()));
      eventPublisher.publishTransactionCommitted(result);
      return result;

    } catch (AccountNotFoundException e) {
      log.warn("[TX] ROLLBACK (cuenta no encontrada): ref={}", referenceId);
      eventPublisher.publishTransactionFailed(
          referenceId, "DEPOSIT", accountNumber, e.getMessage(), "NOT_FOUND");
      throw e;
    } catch (DataAccessException e) {
      handleDataAccessException(e, referenceId, "DEPOSIT", accountNumber);
      throw new RuntimeException("Error de base de datos inesperado", e);
    }
  }

  // RETIRO: igual que depósito pero con validación de saldo (puede hacer rollback)

  public OperationResult withdrawal(String accountNumber, BigDecimal amount) {
    validatePositiveAmount(amount);
    String referenceId = generateReferenceId("WDR");
    long txStart = System.nanoTime();

    log.info(
        "[TX] BEGIN retiro: account={} amount={} ref={} thread={}",
        accountNumber,
        amount,
        referenceId,
        Thread.currentThread().getName());

    eventPublisher.publishTransactionStarted(referenceId, "WITHDRAWAL", accountNumber, amount);

    try {
      OperationResult result =
          transactionTemplate.execute(
              status -> {
                var lockResult = accountRepository.findByAccountNumberForUpdate(accountNumber);

                log.info(
                    "[TX] LOCK ACQUIRED: account={} wait={}ms contention={}",
                    accountNumber,
                    String.format("%.2f", lockResult.lockWaitMillis()),
                    lockResult.hadContention());

                eventPublisher.publishLockAcquired(
                    referenceId,
                    accountNumber,
                    lockResult.lockWaitNanos(),
                    lockResult.hadContention());

                Account account =
                    lockResult
                        .account()
                        .orElseThrow(() -> new AccountNotFoundException(accountNumber));

                BigDecimal balanceBefore = account.getBalance();

                // Validación de negocio: si falla, haremos rollback explícito
                if (balanceBefore.compareTo(amount) < 0) {
                  log.warn(
                      "[TX] ROLLBACK (saldo insuficiente): ref={} balance={} requested={}",
                      referenceId,
                      balanceBefore,
                      amount);
                  // Marcar la transacción para rollback explícitamente
                  status.setRollbackOnly();
                  eventPublisher.publishTransactionFailed(
                      referenceId,
                      "WITHDRAWAL",
                      accountNumber,
                      "Saldo insuficiente",
                      "INSUFFICIENT_FUNDS");
                  throw new InsufficientFundsException(balanceBefore, amount);
                }

                BigDecimal balanceAfter = balanceBefore.subtract(amount);
                accountRepository.updateBalance(account.getId(), balanceAfter);
                log.info(
                    "[TX] BALANCE UPDATE: id={} {}: {}",
                    account.getId(),
                    balanceBefore,
                    balanceAfter);

                LedgerEntry entry =
                    LedgerEntry.builder()
                        .account(account)
                        .amount(amount.negate())
                        .transactionType("WITHDRAWAL")
                        .referenceId(referenceId)
                        .createdAt(LocalDateTime.now())
                        .build();
                ledgerEntryRepository.save(entry);
                log.info("[TX] LEDGER INSERT: ref={} amount=-{}", referenceId, amount);

                long txDuration = System.nanoTime() - txStart;
                return OperationResult.success(
                    "WITHDRAWAL",
                    referenceId,
                    accountNumber,
                    amount,
                    balanceBefore,
                    balanceAfter,
                    lockResult.lockWaitNanos(),
                    txDuration);
              });

      log.info(
          "[TX] COMMIT: ref={} duration={}ms",
          referenceId,
          String.format("%.2f", result.transactionDurationMillis()));
      eventPublisher.publishTransactionCommitted(result);
      return result;

    } catch (InsufficientFundsException e) {
      throw e;
    } catch (AccountNotFoundException e) {
      eventPublisher.publishTransactionFailed(
          referenceId, "WITHDRAWAL", accountNumber, e.getMessage(), "NOT_FOUND");
      throw e;
    } catch (DataAccessException e) {
      handleDataAccessException(e, referenceId, "WITHDRAWAL", accountNumber);
      throw new RuntimeException("Error de base de datos inesperado", e);
    }
  }

  // SIMULACIÓN DE CARGA CONCURRENTE

  /** Lanza N hilos simultáneos sobre la misma cuenta para demostrar contención. */
  public ConcurrentLoadResult simulateConcurrentLoad(ConcurrentLoadRequest request) {
    int n = request.threadCount();
    log.info(
        "[LOAD] Iniciando simulación: {} hilos sobre {} operación={} monto={}",
        n,
        request.accountNumber(),
        request.operationType(),
        request.amountPerThread());

    eventPublisher.publishLoadSimulationStarted(request);

    ExecutorService executor = Executors.newFixedThreadPool(n);
    CountDownLatch startLatch =
        new CountDownLatch(1); // Para lanzar todos los hilos al mismo tiempo
    List<Future<OperationResult>> futures = new ArrayList<>();

    long simStart = System.nanoTime();

    for (int i = 0; i < n; i++) {
      final int threadIndex = i;
      futures.add(
          executor.submit(
              () -> {
                // Todos los hilos esperan aquí hasta que se libera el latch
                // Esto maximiza la contención simultánea
                startLatch.await();

                String op = resolveOperation(request.operationType(), threadIndex);
                try {
                  return op.equals("DEPOSIT")
                      ? deposit(request.accountNumber(), request.amountPerThread())
                      : withdrawal(request.accountNumber(), request.amountPerThread());
                } catch (InsufficientFundsException e) {
                  return OperationResult.failure(
                      op,
                      request.accountNumber(),
                      request.amountPerThread(),
                      e.getMessage(),
                      "INSUFFICIENT_FUNDS",
                      0);
                } catch (Exception e) {
                  return OperationResult.failure(
                      op,
                      request.accountNumber(),
                      request.amountPerThread(),
                      e.getMessage(),
                      "ERROR",
                      0);
                }
              }));
    }

    // Liberar todos los hilos al mismo tiempo: máxima contención
    startLatch.countDown();
    executor.shutdown();

    List<OperationResult> results = new ArrayList<>();
    for (var future : futures) {
      try {
        results.add(future.get(30, TimeUnit.SECONDS));
      } catch (TimeoutException e) {
        results.add(
            OperationResult.failure(
                "UNKNOWN",
                request.accountNumber(),
                request.amountPerThread(),
                "Timeout",
                "TIMEOUT",
                0));
      } catch (Exception e) {
        results.add(
            OperationResult.failure(
                "UNKNOWN",
                request.accountNumber(),
                request.amountPerThread(),
                e.getMessage(),
                "ERROR",
                0));
      }
    }

    double totalMs = (System.nanoTime() - simStart) / 1_000_000.0;
    long successCount = results.stream().filter(OperationResult::success).count();
    long failureCount = results.size() - successCount;
    long contentionCount = results.stream().filter(r -> r.success() && r.hadContention()).count();

    double avgWait =
        results.stream()
            .filter(OperationResult::success)
            .mapToDouble(OperationResult::lockWaitMillis)
            .average()
            .orElse(0);
    double maxWait =
        results.stream()
            .filter(OperationResult::success)
            .mapToDouble(OperationResult::lockWaitMillis)
            .max()
            .orElse(0);

    BigDecimal finalBalance =
        accountRepository
            .findByAccountNumber(request.accountNumber())
            .map(Account::getBalance)
            .orElse(BigDecimal.ZERO);

    var loadResult =
        new ConcurrentLoadResult(
            n,
            (int) successCount,
            (int) failureCount,
            (int) contentionCount,
            avgWait,
            maxWait,
            totalMs,
            finalBalance,
            results);

    log.info(
        "[LOAD] Simulación completada: success={} fail={} contention={} avgWait={}ms total={}ms",
        successCount,
        failureCount,
        contentionCount,
        String.format("%.2f", avgWait),
        String.format("%.2f", totalMs));

    eventPublisher.publishLoadSimulationCompleted(loadResult);
    return loadResult;
  }

  // Helpers privados

  private void validatePositiveAmount(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("El monto debe ser positivo: " + amount);
    }
  }

  private String generateReferenceId(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private String resolveOperation(String operationType, int threadIndex) {
    return switch (operationType.toUpperCase()) {
      case "DEPOSIT" -> "DEPOSIT";
      case "WITHDRAWAL" -> "WITHDRAWAL";
      default -> threadIndex % 2 == 0 ? "DEPOSIT" : "WITHDRAWAL";
    };
  }

  /**
   * Maneja excepciones de JDBC, diferenciando deadlock (40P01) de otros errores. Un deadlock es un
   * evento de concurrencia esperado y documentado; no es un bug.
   */
  private void handleDataAccessException(
      DataAccessException e, String referenceId, String operation, String accounts) {
    Throwable cause = e.getCause();
    if (cause instanceof java.sql.SQLException sqlEx && "40P01".equals(sqlEx.getSQLState())) {
      log.error(
          "[TX] DEADLOCK DETECTADO: ref={} op={} cuentas={}", referenceId, operation, accounts);
      eventPublisher.publishDeadlockDetected(referenceId, operation, accounts);
      throw new DeadlockException(operation + " en " + accounts, e);
    }
    log.error(
        "[TX] ERROR DB inesperado: ref={} op={} error={}", referenceId, operation, e.getMessage());
    eventPublisher.publishTransactionFailed(
        referenceId, operation, accounts, e.getMessage(), "DB_ERROR");
  }

  private AccountSnapshot toSnapshot(Account a) {
    return new AccountSnapshot(a.getId(), a.getAccountNumber(), a.getBalance(), a.getCreatedAt());
  }

  private LedgerEntryDto toLedgerDto(LedgerEntry e) {
    return new LedgerEntryDto(
        e.getId(),
        e.getAccount().getAccountNumber(),
        e.getAmount(),
        e.getTransactionType(),
        e.getReferenceId(),
        e.getCreatedAt());
  }
}
