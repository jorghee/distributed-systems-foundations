UPDATE cuentas
SET saldo = CASE numero_cuenta
    WHEN 'AQP-001' THEN 45000.00
    WHEN 'CUS-002' THEN 3500.00
    ELSE saldo
END
WHERE numero_cuenta IN ('AQP-001', 'CUS-002');

DELETE FROM log_transacciones WHERE id_transaccion = 'TX-FALLA-RED';
DELETE FROM estado_2pc WHERE id_transaccion = 'TX-FALLA-RED';

BEGIN;

UPDATE cuentas
SET saldo = saldo - 25000.00
WHERE numero_cuenta = 'AQP-001' AND saldo >= 25000.00;

UPDATE cuentas
SET saldo = saldo + 25000.00
WHERE numero_cuenta = 'CUS-002';

-- Timeout y rollback
ROLLBACK;

INSERT INTO estado_2pc
    (id_transaccion, coordinador, participante, fase,
     decision_global, detalle_falla)
VALUES
    ('TX-FALLA-RED', 'Arequipa', 'Cusco', 'ROLLED_BACK',
     'ROLLBACK', 'Arequipa no recibio el voto de Cusco.');

INSERT INTO log_transacciones (id_transaccion, nodo, evento, detalle)
VALUES
    ('TX-FALLA-RED', 'Arequipa', 'BEGIN',
     'Inicio de transferencia de S/ 25000.'),
    ('TX-FALLA-RED', 'Arequipa', 'PREPARE',
     'Arequipa solicita el voto de Cusco.'),
    ('TX-FALLA-RED', 'Red AQP-CUS', 'NETWORK_FAILURE',
     'Se interrumpe la comunicacion entre Arequipa y Cusco.'),
    ('TX-FALLA-RED', 'Arequipa', 'TIMEOUT',
     'No se recibe respuesta de Cusco dentro del tiempo esperado.'),
    ('TX-FALLA-RED', 'Arequipa', 'ROLLBACK',
     'El coordinador cancela la transferencia para preservar atomicidad.');

SELECT numero_cuenta, titular, saldo, ciudad
FROM cuentas
WHERE numero_cuenta IN ('AQP-001', 'CUS-002')
ORDER BY numero_cuenta;

SELECT id_log, id_transaccion, nodo, evento, detalle
FROM log_transacciones
WHERE id_transaccion = 'TX-FALLA-RED'
ORDER BY id_log;

SELECT id_transaccion, fase, decision_global, detalle_falla
FROM estado_2pc
WHERE id_transaccion = 'TX-FALLA-RED';
