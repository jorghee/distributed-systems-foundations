TRUNCATE TABLE log_transacciones RESTART IDENTITY;
TRUNCATE TABLE estado_2pc;
TRUNCATE TABLE cuentas;

INSERT INTO cuentas
    (numero_cuenta, titular, saldo, ciudad, fragmento, estado)
VALUES
    ('AQP-001', 'Juan Mamani', 45000.00, 'Arequipa', 'AQP_CUENTAS', 'ACTIVO'),
    ('AQP-002', 'María Quispe', 12500.00, 'Arequipa', 'AQP_CUENTAS', 'ACTIVO'),
    ('CUS-001', 'Carlos Huanca', 8000.00, 'Cusco', 'CUS_CUENTAS', 'ACTIVO'),
    ('CUS-002', 'Ana Flores', 3500.00, 'Cusco', 'CUS_CUENTAS', 'ACTIVO'),
    ('TRU-001', 'Pedro Vega', 20000.00, 'Trujillo', 'TRU_CUENTAS', 'ACTIVO');

SELECT numero_cuenta, titular, saldo, ciudad, fragmento, estado
FROM cuentas
ORDER BY numero_cuenta;
