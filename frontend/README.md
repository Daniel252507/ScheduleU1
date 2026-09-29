# ScheduleU - Frontend (React 18 + Vite)

![ScheduleU](https://img.shields.io/badge/Frontend-React%2018%20%2B%20Vite-blue)

## Descripcion
Frontend del sistema ScheduleU para la gestion y asignacion de horarios universitarios.
Interfaz grafica con vistas CRUD, login protegido, dashboard y modulo de asignaciones.

## Repositorios relacionados
- **Backend**: `Horario_uni1` (Spring Boot + MySQL)
- **Frontend**: Este repositorio

## Prerrequisitos
- Node.js 18+
- NPM
- Backend ScheduleU corriendo en http://localhost:8080 (obligatorio para login y datos)

## 🚀 Instalacion y Ejecucion

```bash
# 1. Instalar dependencias (solo primera vez)
npm install

# 2. Iniciar servidor de desarrollo
npm run dev
```

Abre tu navegador en: **http://localhost:3000**

## 📦 Build para Produccion

```bash
npm run build
# Los archivos compilados quedan en /dist
```

## 🔐 Credenciales (usa las del backend)

| Rol                 | Usuario                         | Contraseña      |
|---------------------|---------------------------------|-----------------|
| Administrador       | `admin@scheduleu.com`           | `admin123`      |
| Coordinador         | `coordinador@scheduleu.com`     | `coord123`      |
| Estudiante          | `estudiante@scheduleu.com`      | `estudiante123` |

## ⚙️ Configuracion

**API client**: `src/services/api.js`
```js
const API_URL = '/api'  // Proxy de Vite hacia http://localhost:8080
```

**Proxy Vite** (`vite.config.js`):
```js
proxy: { '/api': 'http://localhost:8080' }
```

Si tu backend corre en un puerto distinto, actualiza este valor.

## 📁 Estructura

```
Horario_uni1_frontend/
├── src/
│   ├── components/      # Login.jsx, Navbar.jsx, CrudView.jsx
│   ├── services/        # api.js (cliente HTTP)
│   ├── views/           # Dashboard, Asignaturas, Estudiantes, Docentes...
│   ├── App.jsx          # Router principal y rutas protegidas
│   ├── main.jsx         # Punto de entrada React
│   └── styles.css       # Estilos globales
├── index.html
├── package.json
├── vite.config.js
└── README.md           # Este archivo
```

## Notas
- Si `npm install` falla por politicas de PowerShell, ejecuta antes:
  ```powershell
  Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
  ```
  O usa el Símbolo del sistema (CMD).
