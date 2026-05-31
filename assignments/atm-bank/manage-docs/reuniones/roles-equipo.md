# Documento de Especificación: Investigación para Sistema Bancario Distribuido

**Objetivo Principal:** 
Investigar, evaluar y seleccionar las tecnologías y patrones arquitectónicos más adecuados para el sistema. La investigación debe tener un enfoque transversal en dos pilares fundamentales: **algoritmos de coordinación distribuida** y **patrones de diseño para sistemas distribuidos**, aplicados específicamente a las exigencias del sector financiero (alta disponibilidad, consistencia, tolerancia a fallos y seguridad).

## Áreas de Investigación y Asignaciones

A continuación, se detallan los tres frentes de investigación. Cada equipo debe responder a las "Preguntas Guía" para asegurar que el contenido sea relevante para nuestro caso de uso bancario.

### 1. Estado del Arte en Sistemas Distribuidos Financieros

**Responsable:** *Dolly Yadhira Mollo Chuquicaña*

El objetivo es analizar cómo los bancos modernos y las empresas Fintech están utilizando sistemas distribuidos en la actualidad, con énfasis en algoritmos de coordinación.

**Preguntas Guía:**
- ¿Cuáles son los algoritmos de consenso más utilizados (por ejemplo, Raft, Paxos) y en qué herramientas se implementan (por ejemplo, etcd, Apache ZooKeeper)?
- ¿Cómo se utilizan los patrones de diseño (por ejemplo, *Microservices*, *Event-Sourcing*) en arquitecturas bancarias modernas?
- ¿Qué alternativas existen para el descubrimiento de servicios y la gestión de configuraciones distribuidas?

### 2. Problemas de Manejo de Concurrencia y Consistencia

**Responsables:** *Denise Andrea Huacani Jara, Jorge Luis Mamani Huarsaya*

El objetivo es investigar las problemáticas clásicas de la concurrencia en redes distribuidas y cómo resolverlas para garantizar que las transacciones financieras sean seguras y no presenten anomalías (como el doble gasto).

**Preguntas Guía:**
- ¿Cómo se gestionan las transacciones distribuidas? (Comparar el patrón *2-Phase Commit [2PC]* vs. el patrón *Saga*).
- ¿Cómo equilibrar el Teorema CAP (Consistencia, Disponibilidad, Tolerancia a particiones) en un entorno bancario? (ACID vs. BASE).
- ¿Qué estrategias y algoritmos existen para bloqueos distribuidos (*Distributed Locking*) aplicables a cuentas de usuario concurrentes?

### 3. Aplicación Práctica y Arquitectura del Sistema

**Responsables:** Fabiana Francinet Pacheco Palo, Yordano Hernan Boza Portilla (2 personas)

El objetivo es traducir la teoría de las secciones anteriores en componentes arquitectónicos concretos para el sistema bancario que vamos a construir.

**Preguntas Guía:**
- ¿Qué patrones de resiliencia debemos implementar? (por ejemplo, *Circuit Breaker*, *Retry*, *Bulkhead*).
- ¿Cómo estructurar la comunicación entre nodos? (REST vs. gRPC vs. Mensajería asíncrona como Apache Kafka o RabbitMQ).
- ¿Qué bases de datos distribuidas (SQL/NoSQL) o enfoques (por ejemplo, *CQRS*) se adaptan mejor a la lectura intensiva de saldos y la escritura segura de transferencias?

## Criterios de Aceptación y Detalles de Entrega

Para dar por válida la investigación, cada equipo deberá entregar un documento que cumpla estrictamente con los siguientes requisitos:

1. **Descripción Técnica Exhaustiva pero Concisa:** 
   - Evitar el relleno. Explicar el "qué es", "cómo funciona" y "por qué es útil para un banco".
2. **Soporte Visual Obligatorio (Diagramas):** 
   - Se debe incluir al menos un diagrama principal por tema. 
   - *Recomendación:* Usar diagramas de secuencia, diagramas de arquitectura (estilo C4 Model) o diagramas de flujo transaccional. Herramientas sugeridas: Mermaid, PlantUML.
3. **Respaldo Académico/Técnico:** 
   - Mínimo **1 artículo científico o técnico indexado** (IEEE, ACM, Springer, o *Whitepapers* oficiales de la industria como AWS, Google Cloud, etc.) que respalde la investigación. Se debe citar en formato APA.

## Objetivo Final: Matriz de Decisión Tecnológica

El resultado de esta investigación no es solo un documento teórico. El entregable final de cada equipo debe culminar en una **propuesta tecnológica**. 
De las tecnologías y patrones investigados, cada equipo deberá presentar una **Matriz de Decisión** recomendando exactamente **qué se va a utilizar para construir nuestro sistema bancario** y justificando el porqué frente a otras alternativas (evaluando pros, contras, curva de aprendizaje y adecuación al caso bancario).
