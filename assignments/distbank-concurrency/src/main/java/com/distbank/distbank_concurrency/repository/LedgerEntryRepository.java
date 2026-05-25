package com.distbank.distbank_concurrency.repository;

import com.distbank.distbank_concurrency.model.LedgerEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio para el registro histórico de transacciones (Ledger). */
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

  @Query("SELECT e FROM LedgerEntry e WHERE e.account.id = :accountId ORDER BY e.createdAt DESC")
  List<LedgerEntry> findByAccountId(@Param("accountId") Long accountId);

  @Query(
      "SELECT e FROM LedgerEntry e WHERE e.account.accountNumber = :accountNumber ORDER BY"
          + " e.createdAt DESC")
  List<LedgerEntry> findByAccountNumber(@Param("accountNumber") String accountNumber);

  @Query("SELECT e FROM LedgerEntry e WHERE e.referenceId = :referenceId")
  List<LedgerEntry> findByReferenceId(@Param("referenceId") String referenceId);
}
