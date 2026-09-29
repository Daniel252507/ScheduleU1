import { Link, useLocation, useNavigate } from 'react-router-dom'
import { logout } from '../services/api.js'

const ROLES_CATALOGO = ['ADMINISTRADOR', 'COORDINADOR_ACADEMICO']

export default function Navbar({ user, onLogout }) {
  const location = useLocation()
  const navigate = useNavigate()
  const esCatalogo = ROLES_CATALOGO.includes(user.rol)
  const esEstudiante = user.rol === 'ESTUDIANTE'

  const linksEstudiante = [
    { to: '/', label: '🏠 Inicio', end: true },
    { to: '/mis-preferencias', label: '📝 Mis Preferencias' },
    { to: '/mi-horario', label: '📅 Mi Horario' },
  ]
  const linksCatalogo = [
    { to: '/', label: '🏠 Inicio', end: true },
    { to: '/asignaturas', label: '📚 Asignaturas' },
    { to: '/estudiantes', label: '🎓 Estudiantes' },
    { to: '/docentes', label: '👨‍🏫 Docentes' },
    { to: '/salones', label: '🏫 Salones' },
    { to: '/grupos', label: '👥 Grupos' },
    { to: '/horarios', label: '🕒 Horarios' },
    { to: '/asignaciones', label: '📋 Asignaciones' },
  ]
  const links = esEstudiante ? linksEstudiante : esCatalogo ? linksCatalogo : linksEstudiante

  function handleLogout() {
    logout()
    onLogout()
    navigate('/')
  }

  function isActive(l) {
    if (l.end) return location.pathname === l.to
    return location.pathname.startsWith(l.to)
  }

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <div className="nav-brand">
          <span className="brand-icon">📅</span>
          <span className="brand-name">ScheduleU</span>
          <span className={'role-chip ' + user.rol.toLowerCase().replace('_', '-')}>
            {esEstudiante ? 'Estudiante' : esCatalogo ? 'Coordinación' : 'Usuario'}
          </span>
        </div>
        <div className="nav-links">
          {links.map(l => (
            <Link key={l.to} to={l.to} className={'nav-link' + (isActive(l) ? ' active' : '')} end={l.end}>
              {l.label}
            </Link>
          ))}
        </div>
        <div className="nav-user">
          <div className="user-info">
            <strong>{user.nombre}</strong>
            <small>{user.rol.replace(/_/g, ' ')}</small>
          </div>
          <button className="btn-logout" onClick={handleLogout}>Salir</button>
        </div>
      </div>
    </nav>
  )
}
