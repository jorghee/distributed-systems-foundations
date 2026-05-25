import React from 'react';
import type { TransactionLiveState } from '../types/types';
import {
  Clock3,
  LoaderCircle,
  Lock,
  CheckCircle2,
  XCircle
} from "lucide-react";

const LiveConcurrencyView: React.FC<{ transactions: TransactionLiveState[], isConnected: boolean }> = ({ transactions, isConnected }) => {
    
    if (!isConnected) {
        return <div className="text-yellow-500 animate-pulse text-sm">Conectando al servidor WebSocket...</div>;
    }

    if (transactions.length === 0) {
        return <div className="text-gray-400 text-center p-8 border border-dashed border-gray-600 rounded-lg">Esperando que inicie la simulación...</div>;
    }

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {transactions.map(tx => {
                // Definir colores y animaciones según el estado
                let bgClass = "bg-gray-800 border-gray-600";
                let statusText = "En cola";
                let Icon = Clock3;
                let animationClass = "";

                if (tx.status === 'WAITING') {
                    bgClass = "bg-yellow-900/50 border-yellow-500";
                    statusText = "Esperando Lock (Bloqueado)";
                    animationClass = "animate-pulse";
                    Icon = LoaderCircle;
                } else if (tx.status === 'PROCESSING') {
                    bgClass = "bg-blue-900/50 border-blue-400";
                    statusText = "Lock Adquirido (Procesando)";
                    animationClass = "animate-pulse";
                    Icon = Lock;
                } else if (tx.status === 'COMPLETED') {
                    bgClass = "bg-green-900/50 border-green-500";
                    statusText = "Completado";
                    Icon = CheckCircle2;
                } else if (tx.status === 'FAILED') {
                    bgClass = "bg-red-900/50 border-red-500";
                    statusText = "Fallido";
                    Icon = XCircle;
                }

                return (
                    <div key={tx.referenceId} className={`border p-4 rounded-lg shadow-md transition-all duration-300 ${bgClass} ${animationClass}`}>
                        <div className="flex justify-between items-center mb-2 border-b border-gray-700 pb-2">
                            <span className="text-xs font-mono text-gray-400">{tx.thread}</span>
                            <span className="text-sm font-bold bg-gray-900 px-2 py-1 rounded">{tx.type}</span>
                        </div>
                        
                        <div className="mb-3">
                            <p className="text-xl font-bold">${tx.amount}</p>
                            <p className="text-sm flex items-center gap-1 mt-1 font-semibold">
                              <Icon className={`w-4 h-4 ${animationClass}`} />
                              <span>{statusText}</span>
                            </p>
                        </div>

                        <div className="text-xs space-y-1 bg-black/30 p-2 rounded">
                            {tx.status === 'WAITING' && <p className="text-yellow-300">Intentando acceder a la BBDD...</p>}
                            
                            {tx.lockWaitMillis !== undefined && (
                                <p className={tx.hadContention ? "text-orange-400 font-bold" : "text-gray-300"}>
                                    Tiempo espera (Lock): {tx.lockWaitMillis.toFixed(2)}ms {tx.hadContention && "(Contención!)"}
                                </p>
                            )}
                            
                            {tx.durationMillis !== undefined && (
                                <p className="text-blue-300">Duración total: {tx.durationMillis.toFixed(2)}ms</p>
                            )}
                            
                            {tx.errorMessage && (
                                <p className="text-red-400 line-clamp-2" title={tx.errorMessage}>{tx.errorMessage}</p>
                            )}
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

export default LiveConcurrencyView;
