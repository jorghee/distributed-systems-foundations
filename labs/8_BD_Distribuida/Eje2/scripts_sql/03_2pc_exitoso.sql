UPDATE cuentas
SET saldo = CASE numero_cuenta
    WHEN 'AQP-001' THEN 45000.00
    WHEN 'CUS-002' THEN 3500.00
    ELSE saldo
END
WHERE numero_cuenta IN ('AQP-001', 'CUS-002');

DELETE FROM log_transacciones WHERE id_transaccion = 'TX-2PC-EXITOSA';
DELETE FROM estado_2pc WHERE id_transaccion = 'TX-2PC-EXITOSA';

-- Fase 1: PREPARE
BEGIN;

INSERT INTO estado_2pc
    (id_transaccion, coordinador, participante, fase, decision_global)
VALUES
    ('TX-2PC-EXITOSA', 'Arequipa', 'Cusco', 'BEGIN', NULL);

INSERT INTO log_transacciones (id_transaccion, nodo, evento, detalle)
VALUES
    ('TX-2PC-EXITOSA', 'Arequipa', 'BEGIN',
     'El coordinador inicia la transferencia de S/ 25000.');

UPDATE estado_2pc
SET fase = 'PREPARE',
    fecha_actualizacion = CURRENT_TIMESTAMP
WHERE id_transaccion = 'TX-2PC-EXITOSA';

INSERT INTO log_transacciones (id_transaccion, nodo, evento, detalle)
VALUES
    ('TX-2PC-EXITOSA', 'Arequipa', 'PREPARE',
     'Se solicita a Arequipa y Cusco validar la operacion.');

DO $$
BEGIN
    UPDATE cuentas
    SET saldo = saldo - 25000.00
    WHERE numero_cuenta = 'AQP-001'
      AND estado = 'ACTIVO'
      AND saldo >= 25000.00;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Arequipa vota ABORT: saldo insuficiente o cuenta inactiva';
    END IF;
END $$;

INSERT INTO log_transacciones (id_transaccion, nodo, evento, detalle)
VALUES
    ('TX-2PC-EXITOSA', 'Arequipa', 'VOTE_COMMIT',
     'Participante 1 preparo el debito de AQP-001.');

DO $$
BEGIN
    UPDATE cuentas
    SET saldo = saldo + 25000.00
    WHERE numero_cuenta = 'CUS-002'
      AND estado = 'ACTIVO';

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Cusco vota ABORT: cuenta destino inexistente o inactiva';
    END IF;
END $$;

INSERT INTO log_transacciones (id_transaccion, nodo, evento, detalle)
VALUES
    ('TX-2PC-EXITOSA', 'Cusco', 'VOTE_COMMIT',
     'Participante 2 preparo el abono de CUS-002.');

-- Fase 2: COMMIT global
UPDATE estado_2pc
SET fase = 'COMMIT_GLOBAL',
    decision_global = 'COMMIT',
    fecha_actualizacion = CURRENT_TIMESTAMP
WHERE id_transaccion = 'TX-2PC-EXITOSA';

INSERT INTO log_transacciones (id_transaccion, nodo, evento, detalle)
VALUES
    ('TX-2PC-EXITOSA', 'Arequipa', 'COMMIT_GLOBAL',
     'El coordinador confirma la transferencia en ambos participantes.');

COMMIT;

SELECT numero_cuenta, titular, saldo, ciudad
FROM cuentas
WHERE numero_cuenta IN ('AQP-001', 'CUS-002')
ORDER BY numero_cuenta;

SELECT id_log, id_transaccion, nodo, evento, detalle
FROM log_transacciones
WHERE id_transaccion = 'TX-2PC-EXITOSA'
ORDER BY id_log;

SELECT id_transaccion, coordinador, participante, fase, decision_global
FROM estado_2pc
WHERE id_transaccion = 'TX-2PC-EXITOSA';
