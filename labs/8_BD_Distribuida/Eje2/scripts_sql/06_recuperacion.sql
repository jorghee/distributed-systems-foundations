-- Recuperacion de Cusco
DO $$
DECLARE
    v_fase VARCHAR(30);
    v_decision VARCHAR(20);
BEGIN
    SELECT fase, decision_global
    INTO v_fase, v_decision
    FROM estado_2pc
    WHERE id_transaccion = 'TX-CAIDA-CUSCO';

    IF NOT FOUND THEN
        RAISE EXCEPTION
            'No existe TX-CAIDA-CUSCO. Ejecute primero 05_caida_nodo.sql';
    END IF;

    IF v_fase = 'PREPARED_PENDING' AND v_decision = 'PENDIENTE' THEN
        INSERT INTO log_transacciones
            (id_transaccion, nodo, evento, detalle)
        VALUES
            ('TX-CAIDA-CUSCO', 'Cusco', 'NODE_RECOVERED',
             'Cusco vuelve a estar disponible y consulta la bitacora.'),
            ('TX-CAIDA-CUSCO', 'Cusco', 'LOG_REVIEW',
             'No se encuentra una decision COMMIT global durable.'),
            ('TX-CAIDA-CUSCO', 'Arequipa', 'ROLLBACK_GLOBAL',
             'El coordinador comunica ROLLBACK para cerrar la transaccion.');

        UPDATE estado_2pc
        SET fase = 'RECOVERED_ROLLBACK',
            decision_global = 'ROLLBACK',
            detalle_falla =
                'Cusco recuperado; transaccion cancelada sin cambios parciales.',
            fecha_actualizacion = CURRENT_TIMESTAMP
        WHERE id_transaccion = 'TX-CAIDA-CUSCO';
    END IF;
END $$;

SELECT id_transaccion, fase, decision_global, detalle_falla
FROM estado_2pc
WHERE id_transaccion = 'TX-CAIDA-CUSCO';

SELECT id_log, id_transaccion, nodo, evento, detalle
FROM log_transacciones
WHERE id_transaccion = 'TX-CAIDA-CUSCO'
ORDER BY id_log;

SELECT numero_cuenta, titular, saldo, ciudad
FROM cuentas
WHERE numero_cuenta IN ('AQP-001', 'CUS-002')
ORDER BY numero_cuenta;
