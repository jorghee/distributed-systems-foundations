DROP TABLE IF EXISTS log_transacciones;
DROP TABLE IF EXISTS estado_2pc;
DROP TABLE IF EXISTS cuentas;

CREATE TABLE cuentas (
    numero_cuenta VARCHAR(10) PRIMARY KEY,
    titular VARCHAR(100) NOT NULL,
    saldo NUMERIC(12, 2) NOT NULL CHECK (saldo >= 0),
    ciudad VARCHAR(30) NOT NULL,
    fragmento VARCHAR(20) NOT NULL,
    estado VARCHAR(10) NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE estado_2pc (
    id_transaccion VARCHAR(40) PRIMARY KEY,
    coordinador VARCHAR(30) NOT NULL,
    participante VARCHAR(30) NOT NULL,
    fase VARCHAR(30) NOT NULL,
    decision_global VARCHAR(20),
    detalle_falla VARCHAR(100),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE log_transacciones (
    id_log BIGSERIAL PRIMARY KEY,
    id_transaccion VARCHAR(40) NOT NULL,
    nodo VARCHAR(30) NOT NULL,
    evento VARCHAR(30) NOT NULL,
    detalle VARCHAR(250),
    fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('cuentas', 'estado_2pc', 'log_transacciones')
ORDER BY table_name;
