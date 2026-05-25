# Modelo de Datos

## Objetivo
Definir la base estructural donde ocurrirán las colisiones y bloqueos transaccionales.

## Decisiones Técnicas
1. **Delegación del DDL:** Utilizamos `schema.sql` en lugar de la autogeneración de Hibernate (`ddl-auto: update`). Esto garantiza que el esquema sea predecible, explícito y exacto, lo cual es crítico cuando evaluamos el comportamiento de base de datos a bajo nivel.
2. **Registro Inmutable (Ledger):** Además de actualizar el `balance` en la tabla `account`, cada operación inserta un registro en `ledger_entry`. Al finalizar las solicitudes concurrentes, la suma matemática del `Ledger` debe coincidir exactamente con el `balance` final de la cuenta. Si no coincide, hemos demostrado una falla de concurrencia (Condición de Carrera / Lost Update).
3. **Docker Compose Nativo:** Integrado directamente con el ciclo de vida de Spring Boot 3.1+ para facilitar la ejecución en múltiples plataformas (Linux/Windows).
