# ScheduleU - Backend (Spring Boot 3 + MySQL)

![ScheduleU](https://img.shields.io/badge/Backend-Spring%20Boot%203.2.5-green)
![ScheduleU](https://img.shields.io/badge/BD-MySQL%208-orange)
![ScheduleU](https://img.shields.io/badge/Auth-JWT%20Bearer-blue)

## Descripcion
Backend API REST del sistema ScheduleU para la gestion y asignacion de horarios universitarios.
Incluye autenticacion JWT, CRUD completo de 9 entidades y algoritmo de asignacion de horarios
con 2 rondas, preferencias de estudiante y confirmacion de matricula.

## Repositorios relacionados
- **Backend**: Este repositorio (`Horario_uni1`)
- **Frontend**: `Horario_uni1_frontend` (React 18 + Vite)

## Prerrequisitos
| Componente     | Version minima |
|----------------|----------------|
| Java           | 17+ (recomendado 21 LTS) |
| MySQL          | 8.x (via XAMPP/WAMP) |
| Maven          | 3.8+           |
| Git            | Cualquiera     |

> ⚠️ Nota: **Java 25** tiene incompatibilidades con versiones antiguas de Lombok.
> Si usas Java 25+, actualiza a Spring Boot 3.4+ o usa Java 21 LTS.

---

## 🚀 Instalacion y Ejecucion

### PASO 1: Base de Datos
1. Abre **XAMPP** y enciende **Apache** y **MySQL**
2. Abre **phpMyAdmin** (http://localhost/phpmyadmin)
3. Ejecuta el script [INSTALAR_TODO.sql](INSTALAR_TODO.sql) — crea la BD `horario_uni1`, tablas y datos de prueba

### PASO 2: Levantar el Backend (puerto 8080)
```bash
# Desde la raiz del proyecto
mvn spring-boot:run
```

O desde **IntelliJ IDEA**:
- Abre el proyecto y ejecuta la clase principal `ScheduleuApplication`

Al arrancar, el **DataInitializer** detecta y corrige automaticamente las contraseñas
(con prefijo `TMP_NO_ENCRYPTED_` en la BD), encriptandolas con BCrypt.

---

## 🔐 Credenciales Por Defecto

| Rol                 | Usuario                         | Contraseña      |
|---------------------|---------------------------------|-----------------|
| 🛡️ Administrador    | `admin@scheduleu.com`           | `admin123`      |
| 📋 Coordinador      | `coordinador@scheduleu.com`     | `coord123`      |
| 🎓 Estudiante       | `estudiante@scheduleu.com`      | `estudiante123` |

---

## 📡 Endpoints API (prefijo comun: `/api`)

| Grupo           | Metodo HTTP | Ruta                       | Descripcion                              |
|-----------------|-------------|----------------------------|------------------------------------------|
| **Auth**        | POST        | `/api/auth/login`          | Iniciar sesion (devuelve JWT Bearer)    |
| **Auth**        | POST        | `/api/auth/registrar`      | Registrar nuevo usuario                 |
| Asignaturas     | CRUD        | `/api/asignaturas`         | Gestionar asignaturas                   |
| Estudiantes     | CRUD        | `/api/estudiantes`         | Gestionar estudiantes                   |
| Docentes        | CRUD        | `/api/docentes`            | Gestionar docentes                      |
| Salones         | CRUD        | `/api/salones`             | Gestionar salones                       |
| Grupos          | CRUD        | `/api/grupos`              | Gestionar grupos de clase               |
| Horarios        | CRUD        | `/api/horarios`            | Gestionar horarios por grupo            |
| Inscripciones   | CRUD        | `/api/inscripciones`       | Gestionar inscripciones de materias     |
| Asignaciones    | CRUD        | `/api/asignaciones`        | Gestionar asignaciones de estudiantes   |
| Mi Horario      | GET/POST    | `/api/mis-horarios/**`     | Flujo estudiantil: generar/confirmar    |

---

## 📁 Arquitectura del Backend

```
Horario_uni1/
├── src/main/java/com/scheduleu/
│   ├── ScheduleuApplication.java
│   ├── config/              # SecurityConfig, DataInitializer, SetupDataRunner
│   ├── controller/          # 10 Controladores REST (api/*)
│   ├── dto/                 # LoginRequest, AuthResponse, PreferenciasRequest...
│   ├── exception/           # GlobalExceptionHandler
│   ├── model/               # 9 Entidades JPA + Enums (Usuario, Estudiante...)
│   ├── repository/          # Repositorios Spring Data JPA
│   ├── security/            # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
│   └── service/             # Logica de negocio + ScheduleAssignmentService (algoritmo)
├── src/main/resources/
│   └── application.properties
├── INSTALAR_TODO.sql        # Script de instalacion TODO-EN-1 (BD + datos)
├── schema.sql               # Schema base
├── parche_nuevo_modelo.sql  # Parches modelo
├── schema_datos_reales_semestre5.sql
└── pom.xml                  # Dependencias Maven
```

## ⚙️ Configuracion (`application.properties`)
```properties
# Base de datos MySQL (XAMPP root sin password)
spring.datasource.url=jdbc:mysql://localhost:3306/horario_uni1?useSSL=false&serverTimezone=America/Bogota
spring.datasource.username=root
spring.datasource.password=

# JWT
jwt.secret=scheduleu-secret-key-2024-universidad-cundinamarca-sistemas-informacion
jwt.expiration=86400000
```

---

## 📦 Subir a GitHub (este repo Backend)

```bash
git remote add origin https://github.com/TU_USUARIO/Horario_uni1.git
git push -u origin main
```

## 📝 Notas
- El DataInitializer corre automaticamente al iniciar la app: re-encripta contraseñas `TMP_NO_ENCRYPTED_`
- Cross-Origin (CORS) habilitado globalmente para que el frontend (puerto 3000) se conecte sin problemas
- JPA ddl-auto=update: actualiza el esquema sin borrar datos
- Los endpoints requieren token JWT Bearer excepto `/api/auth/**` y OPTIONS

