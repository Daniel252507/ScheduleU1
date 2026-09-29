import { Routes, Route, Navigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import Login from './components/Login.jsx'
import Navbar from './components/Navbar.jsx'
import Dashboard from './views/Dashboard.jsx'
import Asignaturas from './views/Asignaturas.jsx'
import Estudiantes from './views/Estudiantes.jsx'
import Docentes from './views/Docentes.jsx'
import Salones from './views/Salones.jsx'
import Grupos from './views/Grupos.jsx'
import Horarios from './views/Horarios.jsx'
import Asignaciones from './views/Asignaciones.jsx'
import MisPreferencias from './views/MisPreferencias.jsx'
import MiHorario from './views/MiHorario.jsx'
import { getCurrentUser } from './services/api.js'

const ROLES_CATALOGO = ['ADMINISTRADOR', 'COORDINADOR_ACADEMICO']

export default function App() {
  const [user, setUser] = useState(null)

  useEffect(() => {
    setUser(getCurrentUser())
  }, [])

  const onAuthChange = (u) => setUser(u)

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
