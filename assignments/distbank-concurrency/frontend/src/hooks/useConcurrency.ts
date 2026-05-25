import { useState, useEffect, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { WsEventPayload, TransactionLiveState } from '../types/types';

const WS_URL = 'http://localhost:8080/ws';

export function useConcurrency() {
    const [transactions, setTransactions] = useState<Map<string, TransactionLiveState>>(new Map());
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS(WS_URL),
            onConnect: () => {
                setIsConnected(true);
                // Suscribirse a los eventos de concurrencia
                client.subscribe('/topic/concurrency', (message) => {
                    const event: WsEventPayload = JSON.parse(message.body);
                    handleWsEvent(event);
                });
            },
            onDisconnect: () => setIsConnected(false),
            // debug: (str) => console.log(str), // Descomenta para debugguear el WS
        });

        client.activate();
        return () => { client.deactivate(); };
    }, []);

    const handleWsEvent = useCallback((event: WsEventPayload) => {
        setTransactions(prevMap => {
            const newMap = new Map(prevMap);

            if (event.eventType === 'LOAD_STARTED') {
                return new Map(); // Limpiamos la pantalla al iniciar nueva simulación
            }

            if (!event.referenceId) return prevMap;

            const existingTx = newMap.get(event.referenceId) || {
                referenceId: event.referenceId,
                thread: event.thread || 'Desconocido',
                type: event.transactionType || 'UNKNOWN',
                amount: event.amount || 0,
                status: 'WAITING',
                timestamp: new Date(event.timestamp)
            };

            switch (event.eventType) {
                case 'TX_STARTED':
                    existingTx.status = 'WAITING';
                    break;
                case 'LOCK_ACQUIRED':
                    existingTx.status = 'PROCESSING';
                    existingTx.lockWaitMillis = event.lockWaitMillis;
                    existingTx.hadContention = event.hadContention;
                    break;
                case 'TX_COMMITTED':
                    existingTx.status = 'COMPLETED';
                    existingTx.durationMillis = event.durationMillis;
                    // Asegurar que guardamos los datos de contención previos si existen
                    if (event.lockWaitMillis !== undefined) {
                         existingTx.lockWaitMillis = event.lockWaitMillis;
                         existingTx.hadContention = event.hadContention;
                    }
                    break;
                case 'TX_FAILED':
                case 'DEADLOCK':
                    existingTx.status = 'FAILED';
                    existingTx.errorMessage = event.errorMessage || event.errorType || 'Error desconocido';
                    break;
            }

            newMap.set(event.referenceId, existingTx);
            return newMap;
        });
    }, []);

    return { 
        // Convertimos el mapa a un array ordenado cronológicamente para renderizar
        liveTransactions: Array.from(transactions.values()).sort((a, b) => a.timestamp.getTime() - b.timestamp.getTime()), 
        isConnected 
    };
}
