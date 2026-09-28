# ============================================================
# ScheduleU - Sistema de Gestion de Horarios Universitarios
# Backend Spring Boot + MySQL
# ============================================================

## Requisitos previos
1. Java 17 o superior
2. Maven 3.6+
3. MySQL 8.0+ (instalado via XAMPP, WAMP o standalone)
4. Apache (XAMPP/WAMP) ejecutando MySQL en puerto 3306

## Pasos de configuracion

### 1. Crear la base de datos
**Opcion A - Ejecutar el script SQL:**
- Abre MySQL Workbench o phpMyAdmin
- Ejecuta el archivo `schema.sql` ubicado en la raiz del proyecto
- Este script crea la base de datos `horario_uni1` con todas las tablas y datos de ejemplo

**Opcion B - Creacion automatica:**
- Spring Boot creara automaticamente las tablas al iniciar la aplicacion
  (gracias a `spring.jpa.hibernate.ddl-auto=update` en application.properties)
- **Pero primero debes crear la base de datos manualmente:**
  ```sql
  CREATE DATABASE horario_uni1;
  ```

### 2. Verificar credenciales de MySQL
Si tus credenciales son diferentes de `root` / sin contraseña, edita el archivo:
`src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/horario_uni1?useSSL=false&serverTimezone=America/Bogota
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASENA
```

### 3. Compilar y ejecutar el proyecto
Abre una terminal en la raiz del proyecto y ejecuta:

```bash
# Compilar el proyecto
mvn clean install

# Ejecutar la aplicacion
mvn spring-boot:run
```

O usando el archivo JAR generado:
```bash
java -jar target/scheduleu-backend-1.0.0.jar
```

### 4. Acceder a la API
El backend estara disponible en: **http://localhost:8080/api**

## Usuarios iniciales (creados automaticamente)
| Tipo de usuario      | Correo                        | Contraseña     |
|----------------------|-------------------------------|----------------|
| Administrador        | admin@scheduleu.com           | admin123       |
| Coordinador Academico| coordinador@scheduleu.com     | coord123       |
| Estudiante           | estudiante@scheduleu.com      | estudiante123  |

## Endpoints principales

### Autenticacion
- `POST /api/auth/login` - Iniciar sesion
- `POST /api/auth/registrar` - Registrar nuevo usuario

### Gestion de entidades
| Metodo | Endpoint                      | Descripcion                      |
|--------|-------------------------------|----------------------------------|
| GET    | /api/estudiantes              | Listar estudiantes               |
| POST   | /api/estudiantes              | Crear estudiante                 |
| GET    | /api/estudiantes/{id}         | Obtener estudiante por ID        |
| PUT    | /api/estudiantes/{id}         | Actualizar estudiante            |
| DELETE | /api/estudiantes/{id}         | Eliminar estudiante              |
| GET    | /api/asignaturas              | Listar asignaturas               |
| POST   | /api/asignaturas              | Crear asignatura                 |
| GET    | /api/docentes                 | Listar docentes                  |
| POST   | /api/docentes                 | Crear docente                    |
| GET    | /api/salones                  | Listar salones                   |
| POST   | /api/salones                  | Crear salon                      |
| GET    | /api/grupos                   | Listar grupos                    |
| POST   | /api/grupos                   | Crear grupo                      |
| GET    | /api/horarios                 | Listar horarios                  |
| POST   | /api/horarios                 | Crear horario                    |
| GET    | /api/inscripciones            | Listar inscripciones             |
| POST   | /api/inscripciones            | Crear inscripcion                |
| GET    | /api/asignaciones             | Listar asignaciones              |
| POST   | /api/asignaciones/ejecutar    | Ejecutar primera asignacion      |
| POST   | /api/asignaciones/segunda-vuelta | Ejecutar segunda asignacion  |

## Ejemplo de uso

### 1. Iniciar sesion (obtener token JWT)
```bash
POST /api/auth/login
Content-Type: application/json

{
  "correo": "admin@scheduleu.com",
  "contrasena": "admin123"
}
```

Respuesta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "idUsuario": 1,
  "nombre": "Administrador ScheduleU",
  "correo": "admin@scheduleu.com",
  "rol": "ADMINISTRADOR"
}
```

### 2. Usar el token en peticiones protegidas
```bash
GET /api/asignaturas
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Crear una asignatura
```bash
POST /api/asignaturas
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
  "nombre": "Programacion Avanzada",
  "creditos": 4,
  "codigo": "SIS501",
  "semestreRecomendado": 5
}
```

### 4. Ejecutar la primera vuelta de asignacion
```bash
POST /api/asignaciones/ejecutar
Authorization: Bearer <TOKEN>
```

### 5. Ejecutar la segunda vuelta (para pendientes)
```bash
POST /api/asignaciones/segunda-vuelta
Authorization: Bearer <TOKEN>
```

## Estructura del proyecto
```
Horario_uni1/
├── pom.xml                              # Configuracion Maven
├── schema.sql                           # Script de base de datos
└── src/main/
    ├── java/com/scheduleu/
    │   ├── ScheduleuApplication.java   # Clase principal
    │   ├── config/                     # Configuracion (Security, DataInitializer)
    │   ├── controller/                 # Controladores REST
    │   ├── dto/                        # Objetos de transferencia de datos
    │   ├── exception/                  # Manejo de excepciones
    │   ├── model/                      # Entidades JPA
    │   ├── repository/                 # Repositorios JPA
    │   ├── security/                   # JWT y autenticacion
    │   └── service/                    # Logica de negocio
    └── resources/
        └── application.properties      # Configuracion de la aplicacion
```

## Diagrama de la base de datos
Las 9 tablas principales y sus relaciones:
- **usuarios** (1:N) -> Sistema de autenticacion
- **estudiantes** (1:N) -> inscripciones, asignaciones
- **asignaturas** (1:N) -> grupos, inscripciones
- **docentes** (1:N) -> grupos
- **salones** (1:N) -> grupos
- **grupos** (1:N) -> horarios, asignaciones
- **horarios** -> horarios de cada grupo
- **inscripciones** -> estudiantes + asignaturas que deben cursar
- **asignaciones** -> resultado final: estudiantes + grupo asignado

## Solucion de problemas comunes

### Error: "Communications link failure"
- Asegurate de que MySQL este corriendo (inicia XAMPP/WAMP)
- Verifica el puerto (3306 por defecto)
- Verifica credenciales en application.properties

### Error: "Unknown database 'horario_uni1'"
- Crea la base de datos primero: `CREATE DATABASE horario_uni1;`
- O ejecuta el script `schema.sql` completo

### Error de compilacion de Lombok
- Asegurate de tener el plugin de Lombok instalado en tu IDE
- Maven debe descargar las dependencias automaticamente: `mvn clean install`
