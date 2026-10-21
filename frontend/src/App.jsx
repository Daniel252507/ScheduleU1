import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { useEffect, useState } from 'react'
import Login from './infrastructure/components/Login.jsx'
import EstablecerContrasena from './infrastructure/views/EstablecerContrasena.jsx'
import Navbar from './infrastructure/components/Navbar.jsx'
import Dashboard from './infrastructure/views/Dashboard.jsx'
import Asignaturas from './infrastructure/views/Asignaturas.jsx'
import Estudiantes from './infrastructure/views/Estudiantes.jsx'
import Docentes from './infrastructure/views/Docentes.jsx'
import Salones from './infrastructure/views/Salones.jsx'
import Grupos from './infrastructure/views/Grupos.jsx'
import Horarios from './infrastructure/views/Horarios.jsx'
import Asignaciones from './infrastructure/views/Asignaciones.jsx'
import MisPreferencias from './infrastructure/views/MisPreferencias.jsx'
import MiHorario from './infrastructure/views/MiHorario.jsx'
import { authService } from './infrastructure/config/container.js'

const ROLES_CATALOGO = ['ADMINISTRADOR', 'COORDINADOR_ACADEMICO']

export default function App() {
  const [user, setUser] = useState(null)
  const location = useLocation()

  useEffect(() => {
    setUser(authService.getCurrentUser())
  }, [])

  const onAuthChange = (u) => setUser(u)

  // Ruta publica: establecer contrasena desde el enlace enviado por correo.
  if (location.pathname === '/establecer-contrasena') {
    return <EstablecerContrasena />
  }

  if (!user) {
    return <Login onLogin={onAuthChange} />
  }

  const esCatalogo = ROLES_CATALOGO.includes(user.rol)

  return (
    <div className="app">
      <Navbar user={user} onLogout={() => { onAuthChange(null) }} />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Dashboard user={user} />} />

          {/* Rutas del ESTUDIANTE */}
          <Route path="/mis-preferencias" element={<MisPreferencias />} />
          <Route path="/mi-horario" element={<MiHorario />} />

          {/* Rutas de CATALOGO (ADMIN / COORDINADOR) */}
          {esCatalogo && (
            <>
              <Route path="/asignaturas" element={<Asignaturas />} />
              <Route path="/estudiantes" element={<Estudiantes />} />
              <Route path="/docentes" element={<Docentes />} />
              <Route path="/salones" element={<Salones />} />
              <Route path="/grupos" element={<Grupos />} />
              <Route path="/horarios" element={<Horarios />} />
              <Route path="/asignaciones" element={<Asignaciones />} />
            </>
          )}

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  )
}
