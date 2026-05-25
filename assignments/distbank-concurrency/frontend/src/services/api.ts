import type {
  AccountSnapshot,
  LedgerEntryDto,
  DepositRequest,
  WithdrawalRequest,
  ConcurrentLoadRequest,
  OperationResult,
  ConcurrentLoadResult,
} from '../types/types';

const API_BASE_URL = 'http://localhost:8080/api';

async function fetchApi<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, options);
  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || 'An unknown error occurred');
  }
  return response.json();
}

export const getAccounts = (): Promise<AccountSnapshot[]> => {
  return fetchApi<AccountSnapshot[]>(`${API_BASE_URL}/accounts`);
};

export const getAccount = (accountNumber: string): Promise<AccountSnapshot> => {
  return fetchApi<AccountSnapshot>(`${API_BASE_URL}/accounts/${accountNumber}`);
};

export const getLedger = (accountNumber: string): Promise<LedgerEntryDto[]> => {
  return fetchApi<LedgerEntryDto[]>(`${API_BASE_URL}/accounts/${accountNumber}/ledger`);
};

export const deposit = (accountNumber: string, amount: number): Promise<OperationResult> => {
  const request: DepositRequest = { accountNumber, amount };
  return fetchApi<OperationResult>(`${API_BASE_URL}/accounts/${accountNumber}/deposit`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
};

export const withdrawal = (accountNumber: string, amount: number): Promise<OperationResult> => {
  const request: WithdrawalRequest = { accountNumber, amount };
  return fetchApi<OperationResult>(`${API_BASE_URL}/accounts/${accountNumber}/withdrawal`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
};

export const simulateLoad = (request: ConcurrentLoadRequest): Promise<ConcurrentLoadResult> => {
  return fetchApi<ConcurrentLoadResult>(`${API_BASE_URL}/simulation/load`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });
};

