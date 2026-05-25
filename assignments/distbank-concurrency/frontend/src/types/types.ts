export interface AccountSnapshot {
  id: number;
  accountNumber: string;
  balance: number;
  createdAt: string;
}

export interface LedgerEntryDto {
  id: number;
  accountNumber: string;
  amount: number;
  transactionType: string;
  referenceId: string;
  createdAt: string;
}

export interface DepositRequest {
  accountNumber: string;
  amount: number;
}

export interface WithdrawalRequest {
  accountNumber: string;
  amount: number;
}

export interface ConcurrentLoadRequest {
  accountNumber: string;
  threadCount: number;
  amountPerThread: number;
  operationType: string;
}

export interface OperationResult {
  success: boolean;
  operationType: string;
  referenceId: string;
  accountNumber: string;
  amountProcessed: number;
  balanceBefore: number;
  balanceAfter: number;
  lockWaitNanos: number;
  lockWaitMillis: number;
  hadContention: boolean;
  transactionDurationNanos: number;
  transactionDurationMillis: number;
  threadName: string;
  timestamp: string;
  errorMessage?: string;
  errorType?: string;
}

export interface ConcurrentLoadResult {
  totalRequested: number;
  successCount: number;
  failureCount: number;
  contentionCount: number;
  avgLockWaitMillis: number;
  maxLockWaitMillis: number;
  totalDurationMillis: number;
  finalBalance: number;
  results: OperationResult[];
}

export interface ApiError {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
}

