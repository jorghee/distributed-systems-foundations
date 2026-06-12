UPDATE cuentas
SET saldo = CASE numero_cuenta
    WHEN 'AQP-001' THEN 45000.00
    WHEN 'CUS-002' THEN 3500.00
    ELSE saldo
END
WHERE numero_cuenta IN ('AQP-001', 'CUS-002');

DELETE FROM log_transacciones WHERE id_transaccion = 'TX-CAIDA-CUSCO';
DELETE FROM estado_2pc WHERE id_transaccion = 'TX-CAIDA-CUSCO';

BEGIN;

UPDATE cuentas
SET saldo = saldo - 25000.00
WHERE numero_cuenta = 'AQP-001' AND saldo >= 25000.00;

UPDATE cuentas
SET saldo = saldo + 25000.00
WHERE numero_cuenta = 'CUS-002';

-- Caida de Cusco antes del COMMIT global
ROLLBACK;

INSERT INTO estado_2pc
    (id_transaccion, coordinador, participante, fase,
     decision_global, detalle_falla)
VALUES
    ('TX-CAIDA-CUSCO', 'Arequipa', 'Cusco', 'PREPARED_PENDING',
     'PENDIENTE', 'Cusco dejo de responder antes de la decision global.');

INSERT INTO log_transacciones (id_transaccion, nodo, evento, detalle)
VALUES
    ('TX-CAIDA-CUSCO', 'Arequipa', 'BEGIN',
     'Inicio de transferencia de S/ 25000.'),
    ('TX-CAIDA-CUSCO', 'Arequipa', 'PREPARE',
     'El coordinador solicita preparar la transferencia.'),
    ('TX-CAIDA-CUSCO', 'Arequipa', 'VOTE_COMMIT',
     'Arequipa valida que AQP-001 tiene fondos suficientes.'),
    ('TX-CAIDA-CUSCO', 'Cusco', 'PREPARED',
     'Cusco recibe PREPARE, pero aun no existe COMMIT global.'),
    ('TX-CAIDA-CUSCO', 'Cusco', 'NODE_DOWN',
     'El nodo Cusco deja de responder durante el protocolo.'),
    ('TX-CAIDA-CUSCO', 'Arequipa', 'PENDING_RECOVERY',
     'No se aplican cambios; la decision se resolvera al recuperar Cusco.');

SELECT numero_cuenta, titular, saldo, ciudad
FROM cuentas
WHERE numero_cuenta IN ('AQP-001', 'CUS-002')
ORDER BY numero_cuenta;

SELECT id_log, id_transaccion, nodo, evento, detalle
FROM log_transacciones
WHERE id_transaccion = 'TX-CAIDA-CUSCO'
ORDER BY id_log;

SELECT id_transaccion, fase, decision_global, detalle_falla
FROM estado_2pc
WHERE id_transaccion = 'TX-CAIDA-CUSCO';
