# ScheduleU - Sistema de Gestion y Asignacion de Horarios Universitarios

![ScheduleU](https://img.shields.io/badge/Backend-Spring%20Boot%203.2.5-green)
![ScheduleU](https://img.shields.io/badge/Frontend-React%2018%20%2B%20Vite-blue)
![ScheduleU](https://img.shields.io/badge/BD-MySQL%208-orange)

## Descripcion
ScheduleU es un sistema completo para la gestion academica universitaria que permite:
- Administrar asignaturas, docentes, salones, grupos y horarios
- Realizar inscripciones de estudiantes
- Generar asignaciones inteligentes de horarios (primera y segunda ronda)
- Gestionar preferencias de estudiantes (jornada, docente, grupos preferidos)
- Visualizar horarios confirmados por estudiante

## Arquitectura
```
Horario_uni1/
├── src/                     # Backend Spring Boot (Java 17+)
│   └── main/java/com/scheduleu/
│       ├── config/          # Seguridad, DataInitializer
│       ├── controller/      # API REST
│       ├── dto/             # Objetos de transferencia
│       ├── model/           # Entidades JPA
│       ├── repository/      # Repositorios JPA
│       ├── security/        # JWT, Autenticacion
│       └── service/         # Logica de negocio
├── frontend/                # Frontend React 18 + Vite
│   └── src/
│       ├── components/      # Login, Navbar, CrudView
│       ├── services/        # API client (api.js)
│       └── views/           # Vistas CRUD + Asignaciones
├── INSTALAR_TODO.sql        # Script de instalacion TODO-EN-1
├── pom.xml                  # Configuracion Maven (backend)
└── frontend/package.json    # Configuracion NPM (frontend)
```

## Prerrequisitos
| Componente     | Version minima |
|----------------|----------------|
| Java           | 17+ (recomendado 21 LTS) |
| Node.js        | 18+            |
| MySQL          | 8.x (via XAMPP) |
| Maven          | 3.8+           |
| Git            | Cualquiera     |

---

## 🚀 Instalacion y Ejecucion (Paso a Paso)

### PASO 1: Instalar la Base de Datos
1. Abre **XAMPP** y enciende **Apache** y **MySQL**
2. Abre **phpMyAdmin** (http://localhost/phpmyadmin)
3. Importa / ejecuta el script [INSTALAR_TODO.sql](INSTALAR_TODO.sql)
4. Verifica que se haya creado la BD `horario_uni1` con los datos iniciales

### PASO 2: Levantar el Backend (puerto 8080)
```bash
# Desde la raiz del proyecto
mvn spring-boot:run
```
O desde IntelliJ IDEA:
- Abre el proyecto y ejecuta `ScheduleuApplication` (clase principal)

Al arrancar, el **DataInitializer** valida y corrige automaticamente las contraseñas de los usuarios.

### PASO 3: Levantar el Frontend (puerto 3000)
```bash
cd frontend
npm install          # Solo la primera vez
npm run dev
```
Abre tu navegador en: **http://localhost:3000**

---

## 🔐 Credenciales Por Defecto

| Rol                 | Usuario                         | Contraseña      |
|---------------------|---------------------------------|-----------------|
| 🛡️ Administrador    | `admin@scheduleu.com`           | `admin123`      |
| 📋 Coordinador      | `coordinador@scheduleu.com`     | `coord123`      |
| 🎓 Estudiante       | `estudiante@scheduleu.com`      | `estudiante123` |

---

## 📡 Endpoints API (prefijo: `/api`)

| Grupo         | Metodo | Ruta                   | Descripcion                     |
|---------------|--------|------------------------|---------------------------------|
| **Auth**      | POST   | `/api/auth/login`      | Iniciar sesion (devuelve JWT)  |
| **Auth**      | POST   | `/api/auth/registrar`  | Registrar nuevo usuario        |
| Asignaturas   | CRUD   | `/api/asignaturas`     | Gestionar asignaturas          |
| Estudiantes   | CRUD   | `/api/estudiantes`     | Gestionar estudiantes          |
| Docentes      | CRUD   | `/api/docentes`        | Gestionar docentes             |
| Salones       | CRUD   | `/api/salones`         | Gestionar salones              |
| Grupos        | CRUD   | `/api/grupos`          | Gestionar grupos               |
| Horarios      | CRUD   | `/api/horarios`        | Gestionar horarios             |
| Inscripciones | CRUD   | `/api/inscripciones`   | Gestionar inscripciones        |
| Asignaciones  | CRUD   | `/api/asignaciones`    | Gestionar asignaciones         |
| Mi Horario    | Varios | `/api/mis-horarios/**` | Flujo estudiantil              |

---

## ⚙️ Configuracion Importante

**Backend** - Edita `src/main/resources/application.properties` si necesitas cambiar credenciales MySQL:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/horario_uni1
spring.datasource.username=root
spring.datasource.password=
jwt.secret=scheduleu-secret-key-2024-universidad-cundinamarca-sistemas-informacion
```

**Frontend** - Edita `frontend/src/services/api.js` si cambias el puerto backend:
```js
const API_URL = '/api'  // El proxy de Vite redirige a http://localhost:8080
```

El `vite.config.js` ya trae configurado el proxy:
```js
proxy: { '/api': 'http://localhost:8080' }
```

---

## 📦 Subir a GitHub

```bash
# Inicializar repositorio (solo primera vez)
git init
git add .
git commit -m "feat: ScheduleU inicial - Backend y Frontend completos"
git branch -M main

# Conectar a tu repositorio remoto
git remote add origin https://github.com/TU_USUARIO/Horario_uni1.git
git push -u origin main
```

## 📝 Notas Importantes

- **Java 25** puede tener incompatibilidades con Lombok. Usa **Java 21 LTS** o actualiza a Spring Boot 3.4+
- En Windows PowerShell, si `npm install` falla por permisos, usa:
  ```powershell
  Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
  ```
  o ejecuta los comandos desde **CMD**.
- Nunca subas archivos `.env` o secrets a GitHub — ya estan bloqueados en `.gitignore`.
