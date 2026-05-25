package com.distbank.distbank_concurrency.repository;

import com.distbank.distbank_concurrency.model.Account;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

  private static final Logger log = LoggerFactory.getLogger(AccountRepository.class);

  /** Lectura sin lock. Para consultas informativas y validaciones previas. */
  private static final String SQL_FIND_BY_NUMBER =
      "SELECT id, account_number, balance, created_at "
          + "FROM account "
          + "WHERE account_number = ?";

  /** Lectura con lock exclusivo a nivel de fila. */
  private static final String SQL_FIND_BY_NUMBER_FOR_UPDATE =
      "SELECT id, account_number, balance, created_at "
          + "FROM account "
          + "WHERE account_number = ? "
          + "FOR UPDATE";

  /** UPDATE del saldo. Siempre ejecutado dentro de la misma transacción que el FOR UPDATE. */
  private static final String SQL_UPDATE_BALANCE = "UPDATE account SET balance = ? WHERE id = ?";

  /** Listado completo de cuentas sin lock. Para el endpoint de consulta de saldos. */
  private static final String SQL_FIND_ALL =
      "SELECT id, account_number, balance, created_at FROM account ORDER BY id";

  private static final RowMapper<Account> ACCOUNT_MAPPER =
      (rs, rowNum) ->
          Account.builder()
              .id(rs.getLong("id"))
              .accountNumber(rs.getString("account_number"))
              .balance(rs.getBigDecimal("balance"))
              .createdAt(rs.getObject("created_at", LocalDateTime.class))
              .build();

  private final JdbcTemplate jdbc;

  public AccountRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  // Lecturas SIN lock

  public Optional<Account> findByAccountNumber(String accountNumber) {
    var rows = jdbc.query(SQL_FIND_BY_NUMBER, ACCOUNT_MAPPER, accountNumber);
    return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
  }

  public java.util.List<Account> findAll() {
    return jdbc.query(SQL_FIND_ALL, ACCOUNT_MAPPER);
  }

  // Lectura CON lock

  /**
   * Resultado de la adquisición del lock. Contiene la cuenta bloqueada y el tiempo que el hilo
   * esperó al lock.
   */
  public record LockAcquisitionResult(Optional<Account> account, long lockWaitNanos) {
    public double lockWaitMillis() {
      return lockWaitNanos / 1_000_000.0;
    }

    /** Contención real: esperas > 1ms indican que otro hilo tenía el lock. */
    public boolean hadContention() {
      return lockWaitNanos > 1_000_000L;
    }
  }

  /** Busca una cuenta con bloqueo exclusivo y mide el tiempo de contención. */
  public LockAcquisitionResult findByAccountNumberForUpdate(String accountNumber) {
    log.debug(
        "[REPO] Solicitando FOR UPDATE: account={} thread={}",
        accountNumber,
        Thread.currentThread().getName());

    long t0 = System.nanoTime();
    var rows = jdbc.query(SQL_FIND_BY_NUMBER_FOR_UPDATE, ACCOUNT_MAPPER, accountNumber);
    long waitNanos = System.nanoTime() - t0;

    Optional<Account> account = rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    var result = new LockAcquisitionResult(account, waitNanos);

    if (result.hadContention()) {
      log.warn(
          "[REPO] CONTENCIÓN: account={} thread={} wait={}ms",
          accountNumber,
          Thread.currentThread().getName(),
          String.format("%.2f", result.lockWaitMillis()));
    } else {
      log.debug(
          "[REPO] Lock sin contención: account={} wait={}ms",
          accountNumber,
          String.format("%.2f", result.lockWaitMillis()));
    }

    return result;
  }

  // Escritura

  /**
   * Actualiza el saldo. DEBE ejecutarse dentro de la misma transacción que adquirió el lock con
   * findByAccountNumberForUpdate.
   */
  public int updateBalance(Long accountId, BigDecimal newBalance) {
    log.debug("[REPO] UPDATE balance: id={} newBalance={}", accountId, newBalance);
    return jdbc.update(SQL_UPDATE_BALANCE, newBalance, accountId);
  }
}
