# ScheduleU - Frontend React

Interfaz web basica para el sistema de gestion de horarios, construida con **React + Vite**.

## Requisitos
- Node.js 18+
- El backend Spring Boot corriendo en `http://localhost:8080`

## Instalacion

Abre una terminal **DENTRO** de la carpeta `frontend`:
```bash
cd c:\Users\danie\Documents\trae_projects\Horario_uni1\frontend

npm install
```

> Esto creara la carpeta `node_modules` y descargara React, React Router y Vite.

## Ejecutar en modo desarrollo

```bash
npm run dev
```

Abre tu navegador en **http://localhost:3000**

> Vite esta configurado con un **proxy**: todas las peticiones a `/api` se reenvian
> automaticamente al backend en `http://localhost:8080/api`, evitando problemas de CORS.

## Construir para produccion

```bash
npm run build
npm run preview
```

## Usuarios de prueba

| Correo | Contraseña | Rol |
|--------|-----------|-----|
| admin@scheduleu.com | admin123 | Administrador |
| coordinador@scheduleu.com | coord123 | Coordinador Academico |
| estudiante@scheduleu.com | estudiante123 | Estudiante |

## Vistas incluidas

- **🏠 Inicio (Dashboard)**: Estadisticas y resumen general
- **📚 Asignaturas**: CRUD de materias
- **🎓 Estudiantes**: CRUD de estudiantes
- **👨‍🏫 Docentes**: CRUD de personal docente
- **🏫 Salones**: CRUD de espacios fisicos
- **👥 Grupos**: Crear grupos asociando asignatura, docente, salon, cupo
- **🕒 Horarios**: Crear franjas (dia + hora) para cada grupo
- **📋 Asignaciones**: **Ejecutar el algoritmo** (1ª vuelta + 2ª vuelta) y ver resultados

## Estructura

```
frontend/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.jsx            # Entrada React + Router
    ├── App.jsx             # Rutas y layout principal
    ├── styles.css          # Estilos completos
    ├── services/
    │   └── api.js          # Conexion fetch al backend (incluye JWT)
    ├── components/
    │   ├── Login.jsx       # Pantalla de login
    │   ├── Navbar.jsx      # Barra superior con menu
    │   └── CrudView.jsx    # Plantilla generica CRUD (tabla + formulario)
    └── views/
        ├── Dashboard.jsx
        ├── Asignaturas.jsx
        ├── Estudiantes.jsx
        ├── Docentes.jsx
        ├── Salones.jsx
        ├── Grupos.jsx
        ├── Horarios.jsx
        └── Asignaciones.jsx
```
