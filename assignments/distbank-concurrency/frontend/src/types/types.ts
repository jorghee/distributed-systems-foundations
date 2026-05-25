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

export interface WsEventPayload {
    eventType: 'TX_STARTED' | 'LOCK_ACQUIRED' | 'TX_COMMITTED' | 'TX_FAILED' | 'DEADLOCK' | 'LOAD_STARTED' | 'LOAD_COMPLETED';
    referenceId?: string;
    transactionType?: string;
    accountNumber?: string;
    amount?: number;
    thread?: string;
    timestamp: string;
    // Campos específicos de algunos eventos
    lockWaitMillis?: number;
    hadContention?: boolean;
    durationMillis?: number;
    errorMessage?: string;
    errorType?: string;
    operation?: string;
}

export interface TransactionLiveState {
    referenceId: string;
    thread: string;
    type: string;
    amount: number;
    status: 'WAITING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
    timestamp: Date;
    lockWaitMillis?: number;
    durationMillis?: number;
    errorMessage?: string;
    hadContention?: boolean;
}
