# Estrategia de Bloqueo de Concurrencia

## Concepto: Pessimistic Locking (PESSIMISTIC_WRITE)

Hemos optado por delegar el control de concurrencia directamente al motor de base de datos (PostgreSQL) utilizando bloqueos pesimistas, en lugar de usar bloqueos en memoria (como la palabra reservada `synchronized` en Java).

### ¿Por qué NO usar `synchronized`?
En una arquitectura distribuida moderna, la aplicación se despliega en múltiples instancias (contenedores). Un bloqueo en la memoria de la Máquina Virtual de Java (JVM) del Nodo 1 no tiene visibilidad sobre el Nodo 2. Por tanto, dos peticiones concurrentes a distintos nodos corromperían el saldo.

### Implementación
En `AccountRepository`, el método `findByAccountNumberForUpdate` está anotado con `@Lock(LockModeType.PESSIMISTIC_WRITE)`. 

Esto genera la instrucción SQL:
`SELECT ... FROM account WHERE account_number = ? FOR UPDATE;`

### Flujo de Ejecución (Lo que observaremos en la demo):
1. **Petición A** (Canal ATM) invoca la consulta para la Cuenta `ACC-001`.
2. PostgreSQL otorga el bloqueo exclusivo (*Row-Level Lock*) al hilo de la Petición A.
3. **Petición B** (Canal App Móvil) invoca la misma consulta milisegundos después para `ACC-001`.
4. PostgreSQL intercepta la petición y pone el hilo de la Petición B en estado de **espera**, sin consumir CPU extra, hasta que la Petición A termine su transacción.
5. Si Petición A y Petición B intentan bloquear cuentas cruzadas simultáneamente, PostgreSQL detectará el **Deadlock**, abortará una de las transacciones y lanzará una excepción que capturaremos en el backend.
