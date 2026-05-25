package com.distbank.distbank_concurrency.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Libro mayor (Ledger) que actúa como registro inmutable (Append-Only) de todas las operaciones de
 * débito y crédito. Nos permite verificar la consistencia del sistema al final de la prueba de
 * carga.
 */
@Entity
@Table(name = "ledger_entry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  /** Monto de la operación. Puede ser positivo (crédito) o negativo (débito). */
  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  /** Tipo de transacción (e.g., "DEPOSIT", "WITHDRAWAL", "TRANSFER_IN", "TRANSFER_OUT"). */
  @Column(name = "transaction_type", nullable = false, length = 20)
  private String transactionType;

  /** Identificador único que correlaciona múltiples movimientos */
  @Column(name = "reference_id", nullable = false, length = 50)
  private String referenceId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
