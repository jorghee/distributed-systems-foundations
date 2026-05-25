package com.distbank.distbank_concurrency.controller;

import com.distbank.distbank_concurrency.dto.BankingDtos.*;
import com.distbank.distbank_concurrency.exception.*;
import com.distbank.distbank_concurrency.service.AccountService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controlador REST para operaciones bancarias. */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BankingController {

  private final AccountService accountService;

  public BankingController(AccountService accountService) {
    this.accountService = accountService;
  }

  // Consultas

  @GetMapping("/accounts")
  public ResponseEntity<List<AccountSnapshot>> getAllAccounts() {
    return ResponseEntity.ok(accountService.getAllAccounts());
  }

  @GetMapping("/accounts/{accountNumber}")
  public ResponseEntity<AccountSnapshot> getAccount(@PathVariable String accountNumber) {
    return ResponseEntity.ok(accountService.getAccount(accountNumber));
  }

  @GetMapping("/accounts/{accountNumber}/ledger")
  public ResponseEntity<List<LedgerEntryDto>> getLedger(@PathVariable String accountNumber) {
    return ResponseEntity.ok(accountService.getLedger(accountNumber));
  }

  // Operaciones transaccionales

  @PostMapping("/accounts/{accountNumber}/deposit")
  public ResponseEntity<OperationResult> deposit(
      @PathVariable String accountNumber, @RequestBody DepositRequest request) {
    OperationResult result = accountService.deposit(accountNumber, request.amount());
    return ResponseEntity.ok(result);
  }

  @PostMapping("/accounts/{accountNumber}/withdrawal")
  public ResponseEntity<OperationResult> withdrawal(
      @PathVariable String accountNumber, @RequestBody WithdrawalRequest request) {
    OperationResult result = accountService.withdrawal(accountNumber, request.amount());
    return ResponseEntity.ok(result);
  }

  // Simulación de carga concurrente

  @PostMapping("/simulation/load")
  public ResponseEntity<ConcurrentLoadResult> simulateLoad(
      @RequestBody ConcurrentLoadRequest request) {
    ConcurrentLoadResult result = accountService.simulateConcurrentLoad(request);
    return ResponseEntity.ok(result);
  }

  // Manejo global de excepciones de dominio

  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      AccountNotFoundException e, jakarta.servlet.http.HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new ApiError(
                404, "NOT_FOUND", e.getMessage(), req.getRequestURI(), LocalDateTime.now()));
  }

  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<ApiError> handleInsufficientFunds(
      InsufficientFundsException e, jakarta.servlet.http.HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(
            new ApiError(
                422,
                "INSUFFICIENT_FUNDS",
                e.getMessage(),
                req.getRequestURI(),
                LocalDateTime.now()));
  }

  @ExceptionHandler(DeadlockException.class)
  public ResponseEntity<ApiError> handleDeadlock(
      DeadlockException e, jakarta.servlet.http.HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            new ApiError(
                409, "DEADLOCK", e.getMessage(), req.getRequestURI(), LocalDateTime.now()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleBadRequest(
      IllegalArgumentException e, jakarta.servlet.http.HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ApiError(
                400, "BAD_REQUEST", e.getMessage(), req.getRequestURI(), LocalDateTime.now()));
  }
}
