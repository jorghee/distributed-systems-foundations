import React, { useState } from 'react';

const SimulationControl: React.FC<{ selectedAccount: string | null; onSimulate: (threads: number, amount: number, type: 'DEPOSIT' | 'WITHDRAWAL') => void; }> = ({ selectedAccount, onSimulate }) => {
    const [threads, setThreads] = useState(10);
    const [amount, setAmount] = useState(100);
    const [type, setType] = useState<'DEPOSIT' | 'WITHDRAWAL'>('DEPOSIT');

    const handleSimulate = () => {
        if (selectedAccount) {
            onSimulate(threads, amount, type);
        }
    };

    return (
        <div className="space-y-4">
            <div>
                <label className="block text-sm font-medium text-gray-300">Threads</label>
                <input type="number" value={threads} onChange={e => setThreads(parseInt(e.target.value))} className="w-full bg-gray-700 text-white p-2 rounded-md mt-1" />
            </div>
            <div>
                <label className="block text-sm font-medium text-gray-300">Amount per Thread</label>
                <input type="number" value={amount} onChange={e => setAmount(parseInt(e.target.value))} className="w-full bg-gray-700 text-white p-2 rounded-md mt-1" />
            </div>
            <div>
                 <label className="block text-sm font-medium text-gray-300">Operation Type</label>
                <select value={type} onChange={e => setType(e.target.value as 'DEPOSIT' | 'WITHDRAWAL')} className="w-full bg-gray-700 text-white p-2 rounded-md mt-1">
                    <option value="DEPOSIT">Deposit</option>
                    <option value="WITHDRAWAL">Withdrawal</option>
                </select>
            </div>
            <button onClick={handleSimulate} disabled={!selectedAccount} className="w-full bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:bg-gray-500">
                Run Simulation
            </button>
            {!selectedAccount && <p className="text-xs text-yellow-400 text-center mt-2">Please select an account first.</p>}
        </div>
    );
};

export default SimulationControl;

