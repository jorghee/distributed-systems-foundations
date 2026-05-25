import React from 'react';
import type { AccountSnapshot } from '../types/types';

const AccountList: React.FC<{ onAccountSelect: (accountNumber: string) => void; accounts: AccountSnapshot[] }> = ({ onAccountSelect, accounts }) => {
    return (
        <div className="space-y-3">
            {accounts.map(acc => (
                <div key={acc.id} onClick={() => onAccountSelect(acc.accountNumber)} className="bg-gray-700 p-4 rounded-md cursor-pointer hover:bg-gray-600 transition-colors duration-200 flex justify-between items-center">
                    <div>
                        <p className="font-mono text-lg">{acc.accountNumber}</p>
                        <p className="text-sm text-gray-400">Creada: {new Date(acc.createdAt).toLocaleString()}</p>
                    </div>
                    <p className="text-2xl font-bold text-green-400">${acc.balance.toLocaleString()}</p>
                </div>
            ))}
        </div>
    );
};

export default AccountList;
