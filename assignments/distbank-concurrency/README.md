# DistBank: Arquitectura Cliente-Servidor distribuido

## Descripción del Proyecto

DistBank es una aplicación de demostración desarrollada para ilustrar la gestión de problemas de alta concurrencia en bases de datos relacionales, específicamente la anomalía de actualización perdida (Lost Update). Para solucionar este problema, el proyecto implementa bloqueos pesimistas (Pessimistic Locking mediante `FOR UPDATE`) a nivel de base de datos.

La documentación técnica exhaustiva sobre el modelo de datos, la arquitectura y la estrategia de concurrencia se encuentra en el directorio `/docs`. 

## Objetivo de la Demostración

El propósito principal de esta prueba es hacer visible el comportamiento de los hilos de ejecución al competir por un recurso compartido. Mediante una interfaz gráfica conectada vía WebSockets, es posible visualizar en tiempo real el ciclo de vida de las peticiones concurrentes:
* La llegada y encolamiento de múltiples solicitudes simultáneas.
* La adquisición del bloqueo exclusivo por parte de un hilo.
* Los tiempos de espera exactos (contención) generados por el bloqueo de la base de datos.
* La resolución secuencial de transacciones concurrentes bajo carga.

## Tecnologías Utilizadas

* **Backend:** Java, Spring Boot, Spring Data JPA, WebSockets (STOMP).
* **Base de Datos:** PostgreSQL (Ejecutado mediante contenedor Docker).
* **Frontend:** React 19, TypeScript, Vite, TailwindCSS v4, SockJS.

## Requisitos Previos

Para ejecutar este proyecto, se requiere tener instaladas las siguientes herramientas en el sistema operativo. Se recomiendan las siguientes versiones:

* **Java (JDK):** Versión 17 o superior.
* **Maven:** Versión 3.8.x o superior.
* **Node.js:** Versión 20.x o superior (LTS recomendada).
* **Docker Desktop:** Necesario para levantar el servicio de base de datos.

Para verificar que las herramientas están correctamente instaladas, abra una terminal (Símbolo del sistema o PowerShell en Windows) y ejecute los siguientes comandos:

```bash
java -version
mvn -v
node -v
npm -v
docker --version
```
*(Si alguno de los comandos devuelve un error, asegúrese de instalar la herramienta correspondiente y agregarla a la variable de entorno PATH del sistema).*

## Estructura del Proyecto

El repositorio está organizado en tres directorios principales. Es fundamental situarse en el directorio correcto antes de ejecutar cualquier comando.

* `/backend`: Contiene el código fuente de Spring Boot, configuración de Maven y la configuración de contenedores (Docker Compose).
* `/frontend`: Contiene el código fuente de React, configuración de Vite y la gestión de dependencias de npm.
* `/docs`: Contiene la documentación técnica del proyecto.

## Guía de Ejecución

Siga estos pasos en orden para iniciar el proyecto de manera local. Será necesario mantener dos instancias de terminal abiertas simultáneamente.

### 1. Iniciar la Base de Datos

Abra una terminal, diríjase al directorio del backend y levante el contenedor de PostgreSQL. Asegúrese de que Docker Desktop esté en ejecución.

```bash
cd backend
docker compose up -d
```
*(La creación de esquemas y tablas será gestionada automáticamente por Spring Boot durante el arranque).*

### 2. Ejecutar el Backend

En la misma terminal, ubicada en el directorio `backend`, compile e inicie la aplicación Spring Boot:

```bash
mvn clean spring-boot:run
```
El servidor backend estará disponible en `http://localhost:8080`.

### 3. Ejecutar el Frontend

Abra una segunda terminal independiente, diríjase al directorio del frontend, instale las dependencias y levante el servidor de desarrollo Vite:

```bash
cd frontend
npm install
npm run dev
```
La interfaz de usuario estará disponible en `http://localhost:5173`. Puede acceder a este enlace desde su navegador web.

## Cómo Probar la Concurrencia

1. Acceda a la interfaz gráfica en su navegador web (`http://localhost:5173`).
2. Verifique en la cabecera superior que el estado de conexión indique "En línea" (conexión WebSocket establecida).
3. Seleccione una de las cuentas disponibles en el panel de cuentas.
4. En el panel de configuración de simulación, establezca el número de hilos (por ejemplo, 20) y presione "Ejecutar Simulación".
5. Observe el panel derecho de monitorización en tiempo real. Podrá visualizar gráficamente cómo las solicitudes compiten por el bloqueo de la base de datos, entran en estado de espera y, finalmente, se procesan mostrando sus respectivos tiempos de ejecución y contención.

## Solución a Errores Frecuentes

* **Puertos Ocupados:** Si los puertos 5432, 8080 o 5173 ya están en uso por otra aplicación, los servicios no podrán iniciar. En Windows, puede identificar el proceso conflictivo utilizando `netstat -ano | findstr :<puerto>` y finalizarlo desde el Administrador de tareas.
* **Errores de compilación del Frontend (TailwindCSS / Node):** TailwindCSS v4 requiere versiones recientes de Node.js. Si se presentan errores durante el proceso de instalación de npm o al ejecutar Vite, asegúrese de estar utilizando Node.js versión 20.x o superior. Posteriormente, elimine la carpeta `node_modules` y vuelva a ejecutar `npm install`.
* **Comando "mvn" no reconocido:** Si Maven no se encuentra configurado en las variables de entorno del sistema, puede utilizar el script contenedor (wrapper) incluido en el directorio `backend`. En lugar de utilizar `mvn`, ejecute:
  * En sistemas Windows: `.\mvnw.cmd spring-boot:run`
  * En sistemas Linux/macOS: `./mvnw spring-boot:run`
* **Error de conexión con el demonio de Docker:** Si al ejecutar Docker Compose aparece un mensaje indicando que el servicio no está en ejecución, verifique que la aplicación de escritorio de Docker esté abierta y completamente inicializada.
