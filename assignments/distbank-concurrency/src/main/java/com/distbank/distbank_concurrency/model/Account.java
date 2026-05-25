package com.distbank.distbank_concurrency.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una cuenta bancaria en el sistema. Es la entidad principal sobre la cual aplicaremos
 * el Pessimistic Locking para evitar que múltiples canales alteren el saldo simultáneamente.
 */
@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "account_number", unique = true, nullable = false, length = 20)
  private String accountNumber;

  /**
   * El saldo de la cuenta. Las transacciones concurrentes competirán para modificar este valor de
   * forma segura.
   */
  @Column(name = "balance", nullable = false, precision = 15, scale = 2)
  private BigDecimal balance;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
