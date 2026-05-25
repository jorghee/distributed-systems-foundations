import React, { useState, useEffect } from 'react';
import type { AccountSnapshot, LedgerEntryDto, ConcurrentLoadResult } from './types/types';
import * as api from './services/api';
import Card from './components/Card';
import AccountList from './components/AccountList';
import LedgerView from './components/LedgerView';
import SimulationControl from './components/SimulationControl';
import SimulationResults from './components/SimulationResults';


function App() {
    const [accounts, setAccounts] = useState<AccountSnapshot[]>([]);
    const [selectedAccount, setSelectedAccount] = useState<string | null>(null);
    const [ledger, setLedger] = useState<LedgerEntryDto[]>([]);
    const [simulationResult, setSimulationResult] = useState<ConcurrentLoadResult | null>(null);
    const [error, setError] = useState<string | null>(null);

    const loadAccounts = async () => {
        try {
            const data = await api.getAccounts();
            setAccounts(data);
             if(data.length > 0 && !selectedAccount) {
                handleAccountSelect(data[0].accountNumber);
            }
        } catch (err: any) {
            setError(err.message);
        }
    };

    useEffect(() => {
        loadAccounts();
    }, []);

    const handleAccountSelect = async (accountNumber: string) => {
        try {
            setSelectedAccount(accountNumber);
            const ledgerData = await api.getLedger(accountNumber);
            setLedger(ledgerData.sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()));
        } catch (err: any) {
             setError(err.message);
        }
    };

    const handleSimulate = async (threadCount: number, amountPerThread: number, operationType: 'DEPOSIT' | 'WITHDRAWAL') => {
        if (!selectedAccount) return;
        try {
            setError(null);
            setSimulationResult(null);
            const result = await api.simulateLoad({ accountNumber: selectedAccount, threadCount, amountPerThread, operationType });
            setSimulationResult(result);
            // Refresh data after simulation
            loadAccounts();
            handleAccountSelect(selectedAccount);
        } catch (err: any) {
             setError(err.message);
        }
    };

    return (
        <div className="bg-gray-900 text-white min-h-screen font-sans">
            <header className="bg-gray-800 p-4 shadow-md">
                <h1 className="text-3xl font-bold text-purple-400 text-center">Panel de Concurrencia DistBank</h1>
            </header>

            {error && <div className="bg-red-500 text-white p-4 m-4 rounded-md text-center">{error}</div>}

            <main className="p-8">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

                    <div className="lg:col-span-3">
                        <Card title="Cuentas">
                           <AccountList accounts={accounts} onAccountSelect={handleAccountSelect} />
                        </Card>
                    </div>

                    <div className="lg:col-span-2">
                         <Card title={`Libro Mayor de la cuenta ${selectedAccount || '...'}`}>
                           <LedgerView ledger={ledger} />
                        </Card>
                    </div>

                    <div>
                        <Card title="Simulación de Concurrencia">
                           <SimulationControl selectedAccount={selectedAccount} onSimulate={handleSimulate} />
                        </Card>
                    </div>

                    <div className="lg:col-span-3">
                        <Card title="Resultados de la Simulación">
                           <SimulationResults results={simulationResult} />
                        </Card>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default App;
