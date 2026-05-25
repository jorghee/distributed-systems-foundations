import React from 'react';
import type { ConcurrentLoadResult } from '../types/types';


const SimulationResults: React.FC<{ results: ConcurrentLoadResult | null }> = ({ results }) => {
    if (!results) return <p className="text-gray-400">Ejecuta una simulación para ver los resultados.</p>;

    return (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
            <div className="bg-gray-700 p-3 rounded-md"><p className="text-sm text-gray-300">Duración Total</p><p className="text-xl font-bold">{results.totalDurationMillis.toFixed(2)}ms</p></div>
            <div className="bg-gray-700 p-3 rounded-md"><p className="text-sm text-gray-300">Exitosas</p><p className="text-xl font-bold text-green-400">{results.successCount}/{results.totalRequested}</p></div>
            <div className="bg-gray-700 p-3 rounded-md"><p className="text-sm text-gray-300">Fallidas</p><p className="text-xl font-bold text-red-400">{results.failureCount}</p></div>
            <div className="bg-gray-700 p-3 rounded-md"><p className="text-sm text-gray-300">Contenciones</p><p className="text-xl font-bold text-yellow-400">{results.contentionCount}</p></div>
            <div className="bg-gray-700 p-3 rounded-md col-span-2"><p className="text-sm text-gray-300">Espera Prom. (Bloqueo)</p><p className="text-xl font-bold">{results.avgLockWaitMillis.toFixed(2)}ms</p></div>
             <div className="bg-gray-700 p-3 rounded-md col-span-2"><p className="text-sm text-gray-300">Espera Máx. (Bloqueo)</p><p className="text-xl font-bold">{results.maxLockWaitMillis.toFixed(2)}ms</p></div>
        </div>
    );
};

export default SimulationResults;

