# 📚 Biblioteca Digital - Administrador de Libros

Una aplicación web moderna y responsive para la administración de libros con búsqueda avanzada, desarrollada con **Spring Boot** y **API RESTful**.

## ✨ Características

- ✅ **Gestión Completa de Libros**
  - Agregar, editar, eliminar y visualizar libros
  - Modal emergente para formularios
  - Validación de datos en tiempo real

- 🔍 **Búsqueda Avanzada**
  - Búsqueda simple por título o autor
  - Búsqueda avanzada con múltiples filtros:
    - Por título
    - Por autor
    - Por ISBN
    - Por rango de años de publicación
    - Por rango de precios
    - Por disponibilidad en stock
  - Slider interactivo para precios
  - Filtro de disponibilidad

- 📊 **Interfaz Intuitiva**
  - Diseño moderno con gradientes azules
  - Tabla responsiva de libros
  - Notificaciones visuales en tiempo real
  - Modal elegante para agregar/editar libros
  - Alertas en esquina superior derecha

- 💾 **Base de Datos**
  - Persistencia de datos con JPA
  - Validación de ISBN único
  - Almacenamiento de múltiples atributos por libro

## 🛠️ Tecnologías Utilizadas

**Backend:**
- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- Base de datos relacional (H2/MySQL)
- Maven

**Frontend:**
- HTML5
- CSS3 (Diseño responsivo)
- JavaScript (Vanilla)
- API Fetch

## 📋 Requisitos Previos

- Java JDK 17 o superior
- Maven 3.6+
- Navegador moderno (Chrome, Firefox, Safari, Edge)

## 🚀 Instalación

### 1. Clonar el repositorio
```bash
git clone https://github.com/jorghee/distributed-systems-foundations.git
cd labs/6_rest_restful/library_spring
```

### 2. Compilar el proyecto
```bash
mvn clean compile
```

### 3. Empaquetar la aplicación
```bash
mvn clean package
```

### 4. Ejecutar la aplicación
```bash
mvn spring-boot:run
```

O usando el script:
```bash
./run.bat
```

La aplicación estará disponible en: `http://localhost:8080`

## 📖 Uso

### Interfaz Web
1. **Acceder a la aplicación**: Abre `http://localhost:8080` en tu navegador
2. **Agregar Libro**: Haz clic en "+ Agregar Libro" en la esquina superior derecha
3. **Editar Libro**: Haz clic en "Editar" en cualquier fila de la tabla
4. **Eliminar Libro**: Haz clic en "Eliminar" en cualquier fila de la tabla
5. **Buscar Libros**: 
   - **Búsqueda Simple**: Ingresa título o autor
   - **Búsqueda Avanzada**: Usa los filtros disponibles (título, autor, ISBN, años, precios, disponibilidad)

## 🔌 Endpoints de la API

### Operaciones CRUD

```
GET    /api/libros                           # Obtener todos los libros
GET    /api/libros/{id}                      # Obtener libro por ID
POST   /api/libros                           # Crear nuevo libro
PUT    /api/libros/{id}                      # Actualizar libro
DELETE /api/libros/{id}                      # Eliminar libro
```

### Búsquedas

```
GET    /api/libros/buscar/titulo?titulo=...              # Buscar por título
GET    /api/libros/buscar/autor?autor=...                # Buscar por autor
GET    /api/libros/buscar/isbn?isbn=...                  # Buscar por ISBN
GET    /api/libros/buscar/anio?anio=...                  # Buscar por año
GET    /api/libros/buscar/anios?minAnio=...&maxAnio=...  # Buscar por rango de años
GET    /api/libros/buscar/precio?min=...&max=...         # Buscar por rango de precios
GET    /api/libros/buscar/avanzada?...                   # Búsqueda avanzada combinada
```

### Ejemplo de Request - Crear Libro

```bash
curl -X POST http://localhost:8080/api/libros \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "El Quijote",
    "autor": "Miguel de Cervantes",
    "isbn": "978-8408037704",
    "anioPublicacion": 1605,
    "descripcion": "La novela más famosa del Siglo de Oro español",
    "precio": 29.99,
    "cantidad": 15
  }'
```

### Ejemplo de Request - Búsqueda Avanzada

```bash
curl "http://localhost:8080/api/libros/buscar/avanzada?titulo=Java&minPrecio=10&maxPrecio=100&minAnio=2000&maxAnio=2024"
```

## 📁 Estructura del Proyecto

```
library_spring/
├── src/
│   └── main/
│       ├── java/com/biblioteca/
│       │   ├── model/
│       │   │   └── Libro.java           # Entidad JPA
│       │   ├── repository/
│       │   │   └── LibroRepository.java # Interfaz de acceso a datos
│       │   ├── service/
│       │   │   └── LibroService.java    # Lógica de negocio
│       │   ├── controller/
│       │   │   └── LibroController.java # Endpoints REST
│       │   ├── config/
│       │   │   └── DataLoader.java      # Carga inicial de datos
│       │   └── BibliotecaApplication.java # Punto de entrada
│       └── resources/
│           └── static/
│               └── index.html           # Interfaz web
├── pom.xml                              # Configuración Maven
└── README.md                            # Este archivo
```

## 📊 Modelo de Datos

### Entidad: Libro

| Campo | Tipo | Restricción |
|-------|------|-------------|
| id | Long | PK, Auto-generado |
| titulo | String | Obligatorio |
| autor | String | Obligatorio |
| isbn | String | Obligatorio, Único |
| anioPublicacion | Integer | Opcional |
| descripcion | String | Opcional |
| precio | Double | Opcional |
| cantidad | Integer | Opcional |

## 🎨 Colores y Estilos

La aplicación utiliza una paleta de colores azulada moderna:

- **Primario**: `#4a90e2` (Azul medio)
- **Secundario**: `#357abd` (Azul oscuro)
- **Fondo**: Gradiente de azul a azul oscuro
- **Alertas**: Verde para éxito, Rojo para errores

## 🔄 Flujo de la Aplicación

1. **Al cargar**: Se obtienen todos los libros de la BD
2. **Agregar**: Modal emerge → Validación → POST a API → Recarga de lista
3. **Editar**: Modal con datos precargados → PUT a API → Recarga de lista
4. **Eliminar**: Confirmación → DELETE a API → Recarga de lista
5. **Buscar**: Filtros aplicados → GET a API → Mostrar resultados

## ⚙️ Configuración

### Base de Datos (application.properties)

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### Puerto
```properties
server.port=8080
```

## 🤝 Contribuir

Las contribuciones son bienvenidas. Para cambios grandes:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📚 Recursos Útiles

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [RESTful API Best Practices](https://restfulapi.net/)
- [MDN Web Docs](https://developer.mozilla.org/)

---

**Última actualización**: Mayo 2026

Hecho con ❤️ para Sistemas Distribuidos
