import React from 'react';
import type { LedgerEntryDto } from '../types/types';

const LedgerView: React.FC<{ ledger: LedgerEntryDto[] }> = ({ ledger }) => (
    <div className="space-y-2 max-h-96 overflow-y-auto">
        {ledger.length === 0 && <p className="text-gray-400">No transactions yet.</p>}
        {ledger.map(entry => (
            <div key={entry.id} className={`p-3 rounded-md ${entry.transactionType === 'DEPOSIT' ? 'bg-green-900' : 'bg-red-900'}`}>
                <p className="font-mono text-sm">Ref: {entry.referenceId}</p>
                <div className="flex justify-between items-center">
                    <p>{entry.transactionType}</p>
                    <p className="font-bold text-lg">${entry.amount.toLocaleString()}</p>
                </div>
                 <p className="text-xs text-gray-400">{new Date(entry.createdAt).toLocaleString()}</p>
            </div>
        ))}
    </div>
);

export default LedgerView;

