import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  asignaturasService,
  estudiantesService,
  docentesService,
  salonesService,
  gruposService,
  horariosService,
  misHorariosService,
} from '../config/container.js'

const ROLES_CATALOGO = ['ADMINISTRADOR', 'COORDINADOR_ACADEMICO']

export default function Dashboard({ user }) {
  const [stats, setStats] = useState(null)
  const [estadisticas, setEstadisticas] = useState(null)
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const esCatalogo = user && ROLES_CATALOGO.includes(user.rol)
  const esEstudiante = user && user.rol === 'ESTUDIANTE'

  useEffect(() => {
    async function load() {
      try {
        if (esCatalogo) {
          const [a, e, d, s, g, h] = await Promise.all([
            asignaturasService.list(),
            estudiantesService.list(),
            docentesService.list(),
            salonesService.list(),
            gruposService.list(),
            horariosService.list(),
          ])
          setStats({
            asignaturas: a.length,
            estudiantes: e.length,
            docentes: d.length,
            salones: s.length,
            grupos: g.length,
            horarios: h.length,
          })
        } else if (esEstudiante) {
          try {
            const res = await misHorariosService.getAsignaciones()
            const asig = Array.isArray(res) ? res : []
            const ok = asig.filter(x => x.estado === 'ASIGNADO').length
            const pend = asig.filter(x => x.estado === 'PENDIENTE').length
            setEstadisticas({ asignadas: ok, pendientes: pend, total: asig.length })
            const prev = await misHorariosService.getPreferencias()
            setStats({
              materiasSeleccionadas: (prev?.materiasSeleccionadas || []).length,
              jornadaPreferida: prev?.jornadaPreferida || 'Sin seleccionar',
            })
          } catch (e2) { setError('Tu usuario aún no esta asociado a un estudiante. Contacta a coordinación.') }
        }
      } catch (err) {
        setError('No se pudieron cargar los datos. Verifica que el backend este corriendo.')
      }
    }
    load()
  }, [esCatalogo, esEstudiante])

  if (esEstudiante) {
    return (
      <div className="page">
        <header className="page-header">
          <h1>👋 Bienvenido, {user.nombre.split(' ')[0]}</h1>
          <p>Gestiona tus preferencias y genera tu horario personalizado.</p>
        </header>
        {error && <div className="error-msg big">{error}</div>}

        <div className="actions-bar">
          <button className="btn-primary big" onClick={() => navigate('/mis-preferencias')}>
            📝 Configurar mis preferencias
          </button>
          <button className="btn-secondary big" onClick={() => navigate('/mi-horario')}>
            📅 Ver mi horario
          </button>
        </div>

        <div className="stats-grid">
          <div className="stat-card card-blue">
            <div className="stat-icon">📚</div>
            <div className="stat-body">
              <div className="stat-value">{stats ? stats.materiasSeleccionadas : '...'}</div>
              <div className="stat-label">Materias seleccionadas</div>
            </div>
          </div>
          <div className="stat-card card-green">
            <div className="stat-icon">✅</div>
            <div className="stat-body">
              <div className="stat-value">{estadisticas ? estadisticas.asignadas : '...'}</div>
              <div className="stat-label">Materias asignadas</div>
            </div>
          </div>
          <div className="stat-card card-yellow">
            <div className="stat-icon">⏳</div>
            <div className="stat-body">
              <div className="stat-value">{estadisticas ? estadisticas.pendientes : '...'}</div>
              <div className="stat-label">Materias pendientes</div>
            </div>
          </div>
          <div className="stat-card card-purple">
            <div className="stat-icon">🌤️</div>
            <div className="stat-body">
              <div className="stat-value">{stats ? (stats.jornadaPreferida === 'MANANA' ? 'Mañana' : stats.jornadaPreferida === 'TARDE' ? 'Tarde' : stats.jornadaPreferida === 'NOCHE' ? 'Noche' : 'Sin pref.') : '...'}</div>
              <div className="stat-label">Jornada preferida</div>
            </div>
          </div>
        </div>

        <div className="help-card">
          <h3>🚀 Como generar tu horario</h3>
          <ol>
            <li>Ve a <strong>Mis Preferencias</strong>.</li>
            <li>Selecciona <strong>que materias quieres cursar</strong> este semestre.</li>
            <li>Indica tu <strong>jornada preferida</strong> (mañana, tarde, noche, sin preferencia).</li>
            <li>Si te interesa un <strong>grupo en específico</strong>, selecciónalo en la lista desplegable.</li>
            <li>Guarda y haz clic en <strong>"Generar mi horario"</strong>.</li>
            <li>Revisa los resultados en <strong>"Mi horario"</strong>. Puedes reconfigurar y volver a generar.</li>
          </ol>
        </div>
      </div>
    )
  }

  const cards = [
    { label: 'Asignaturas', key: 'asignaturas', icon: '📚', color: 'card-blue' },
    { label: 'Estudiantes', key: 'estudiantes', icon: '🎓', color: 'card-green' },
    { label: 'Docentes', key: 'docentes', icon: '👨‍🏫', color: 'card-purple' },
    { label: 'Salones', key: 'salones', icon: '🏫', color: 'card-orange' },
    { label: 'Grupos', key: 'grupos', icon: '👥', color: 'card-pink' },
    { label: 'Franjas Horarias', key: 'horarios', icon: '🕒', color: 'card-teal' },
  ]

  return (
    <div className="page">
      <header className="page-header">
        <h1>Panel de Control - Coordinación</h1>
        <p>Catálogo oficial del semestre 5. Los estudiantes seleccionan sus propias preferencias y generan su horario.</p>
      </header>
      {error && <div className="error-msg big">{error}</div>}
      <div className="stats-grid">
        {cards.map(c => (
          <div key={c.key} className={`stat-card ${c.color}`}>
            <div className="stat-icon">{c.icon}</div>
            <div className="stat-body">
              <div className="stat-value">{stats ? stats[c.key] : '...'}</div>
              <div className="stat-label">{c.label}</div>
            </div>
          </div>
        ))}
      </div>
      <div className="help-card">
        <h3>ℹ️ Flujo correcto de ScheduleU</h3>
        <ol>
          <li><strong>Administración:</strong> carga Asignaturas, Docentes, Salones, Grupos y sus Horarios (catálogo).</li>
          <li><strong>Estudiantes:</strong> entran al sistema, seleccionan sus materias, marcan preferencias y generan su propio horario.</li>
          <li><strong>Validaciones automáticas:</strong> el sistema evita cruces de horario y respeta cupos, jornadas y grupos preferidos.</li>
        </ol>
      </div>
    </div>
  )
}
