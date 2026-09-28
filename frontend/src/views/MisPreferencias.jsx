import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../services/api.js'

const JORNADAS = [
  { value: 'MANANA', label: '☀️ Mañana' },
  { value: 'TARDE', label: '🌤️ Tarde' },
  { value: 'NOCHE', label: '🌙 Noche' },
  { value: 'SIN_PREFERENCIA', label: '🎯 Sin preferencia' },
]

export default function MisPreferencias() {
  const navigate = useNavigate()
  const [asignaturas, setAsignaturas] = useState([])
  const [jornada, setJornada] = useState('SIN_PREFERENCIA')
  const [priorizaDocente, setPriorizaDocente] = useState(false)
  const [observaciones, setObservaciones] = useState('')
  const [seleccionadas, setSeleccionadas] = useState({})
  const [grupoPreferido, setGrupoPreferido] = useState({})
  const [loading, setLoading] = useState(false)
  const [guardando, setGuardando] = useState(false)
  const [generando, setGenerando] = useState(false)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  useEffect(() => {
    async function load() {
      setLoading(true)
      setError('')
      try {
        const [asigs, prefs] = await Promise.all([
          api.get('/mis-horarios/asignaturas-disponibles'),
          (async () => { try { return await api.get('/mis-horarios/preferencias') } catch { return null } })(),
        ])
        setAsignaturas(Array.isArray(asigs) ? asigs : [])
        if (prefs) {
          setJornada(prefs.jornadaPreferida || 'SIN_PREFERENCIA')
          setPriorizaDocente(!!prefs.priorizaDocente)
          setObservaciones(prefs.observaciones || '')
          const sel = {}
          const gp = {}
          ;(prefs.materiasSeleccionadas || []).forEach(m => {
            sel[m.idAsignatura] = true
            if (m.idGrupoPreferido) gp[m.idAsignatura] = m.idGrupoPreferido
          })
          setSeleccionadas(sel)
          setGrupoPreferido(gp)
        }
      } catch (err) {
        setError('No se pudieron cargar los datos. Asegúrate de estar registrado como estudiante.')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  function toggleMateria(id) {
    setSeleccionadas(s => {
      const n = { ...s }
      if (n[id]) delete n[id]
      else n[id] = true
      return n
    })
  }

  function setGrupoPref(idAsig, idGrupo) {
    setGrupoPreferido(g => {
      const n = { ...g }
      if (idGrupo === '') delete n[idAsig]
      else n[idAsig] = Number(idGrupo)
      return n
    })
  }

  function buildBody() {
    const materias = Object.keys(seleccionadas)
      .filter(id => seleccionadas[id])
      .map((id, idx) => ({
        idAsignatura: Number(id),
        idGrupoPreferido: grupoPreferido[id] ? Number(grupoPreferido[id]) : null,
        prioridad: idx + 1,
      }))
    return {
      jornadaPreferida: jornada,
      priorizaDocente: priorizaDocente,
      observaciones: observaciones,
      materias,
    }
  }

  async function handleGuardar() {
    setGuardando(true); setError(''); setMsg('')
    try {
      const body = buildBody()
      const r = await api.post('/mis-horarios/guardar-preferencias', body)
      setMsg(r.mensaje || 'Preferencias guardadas')
    } catch (err) {
      setError('No se pudieron guardar las preferencias.')
    } finally {
      setGuardando(false)
    }
  }

  async function handleGenerar() {
    setGenerando(true); setError(''); setMsg('')
    try {
      const body = buildBody()
      if (body.materias.length === 0) { setError('Selecciona al menos una materia antes de generar.'); setGenerando(false); return }
      await api.post('/mis-horarios/guardar-preferencias', body)
      await api.post('/mis-horarios/generar')
      navigate('/mi-horario')
    } catch (err) {
      setError('No se pudo generar el horario. Revisa tu selección.')
    } finally {
      setGenerando(false)
    }
  }

  const seleccionCount = Object.values(seleccionadas).filter(Boolean).length

  return (
    <div className="page">
      <header className="page-header">
        <h1>📝 Mis Preferencias</h1>
        <p>Selecciona las materias que quieres cursar, configura tu jornada preferida y genera tu horario.</p>
      </header>

      {error && <div className="error-msg big">{error}</div>}
      {msg && <div className="result-card" style={{ background: '#dcfce7', borderColor: '#86efac' }}><h3 style={{ color: '#166534' }}>✅ {msg}</h3></div>}

      <div className="actions-bar">
        <button className="btn-primary big" onClick={handleGuardar} disabled={guardando || generando}>
          💾 {guardando ? 'Guardando...' : 'Guardar preferencias'}
        </button>
        <button className="btn-primary big" onClick={handleGenerar} disabled={guardando || generando}>
          🎲 {generando ? 'Generando...' : 'Guardar y generar mi horario'}
        </button>
        <div className="mini-stat bg-blue" style={{ marginLeft: 'auto' }}>
          <span>Materias seleccionadas</span>
          <strong>{seleccionCount}</strong>
        </div>
      </div>

      <div className="two-col">
        <section className="card" style={{ height: 'fit-content' }}>
          <h3>⚙️ Configuración general</h3>
          <div className="form-grid">
            <label>
              <span>Jornada preferida</span>
              <select value={jornada} onChange={(e) => setJornada(e.target.value)}>
                {JORNADAS.map(j => <option key={j.value} value={j.value}>{j.label}</option>)}
              </select>
            </label>
            <label className="check-row" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 600 }}>
              <input
                type="checkbox"
                checked={priorizaDocente}
                onChange={(e) => setPriorizaDocente(e.target.checked)}
                style={{ width: '18px', height: '18px' }}
              />
              <span>Priorizar grupos con docentes de alta valoración</span>
            </label>
            <label>
              <span>Observaciones (opcional)</span>
              <textarea
                value={observaciones}
                onChange={(e) => setObservaciones(e.target.value)}
                rows={4}
                placeholder="Cualquier detalle que quieras comentar a coordinación..."
                style={{ padding: '10px 12px', border: '1px solid var(--border)', borderRadius: '8px', fontFamily: 'inherit', fontSize: '14px', resize: 'vertical' }}
              />
            </label>
          </div>

          <div className="help-card" style={{ marginTop: '20px' }}>
            <h3>💡 Consejos</h3>
            <ul style={{ paddingLeft: '18px' }}>
              <li>Selecciona <strong>todas</strong> las materias que necesites cursar.</li>
              <li>Marca un <strong>grupo preferido</strong> si tienes interés en un docente en específico.</li>
              <li>Si no te importa el grupo, déjalo en <em>"Sin preferencia"</em>.</li>
              <li>Puedes volver a generar el horario <strong>tantas veces como quieras</strong>.</li>
            </ul>
          </div>
        </section>

        <section className="card">
          <h3>📚 Asignaturas disponibles ({asignaturas.length})</h3>
          {loading ? <p>Cargando...</p> :
            asignaturas.length === 0 ? <p><em>No hay asignaturas disponibles.</em></p> :
              <div className="asig-list">
                {asignaturas.map(a => (
                  <div key={a.idAsignatura} className={`asig-card ${seleccionadas[a.idAsignatura] ? 'selected' : ''}`}>
                    <div className="asig-head">
                      <label className="asig-check" style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', cursor: 'pointer', flex: 1 }}>
                        <input
                          type="checkbox"
                          checked={!!seleccionadas[a.idAsignatura]}
                          onChange={() => toggleMateria(a.idAsignatura)}
                          style={{ width: '18px', height: '18px', marginTop: '3px' }}
                        />
                        <div style={{ flex: 1 }}>
                          <div style={{ fontWeight: 700, fontSize: '14.5px' }}>
                            {a.nombre}
                            <span className="badge badge-day" style={{ marginLeft: '8px' }}>{a.codigo}</span>
                          </div>
                          <div style={{ fontSize: '12.5px', color: 'var(--text-light)', marginTop: '3px' }}>
                            {a.creditos} créditos · Semestre recomendado: {a.semestreRecomendado || '-'} · {a.grupos?.length || 0} grupo(s) disponibles
                          </div>
                        </div>
                      </label>
                    </div>
                    {seleccionadas[a.idAsignatura] && (
                      <div className="asig-body">
                        <div className="form-grid" style={{ marginTop: '10px' }}>
                          <label>
                            <span>Grupo preferido (opcional)</span>
                            <select
                              value={grupoPreferido[a.idAsignatura] || ''}
                              onChange={(e) => setGrupoPref(a.idAsignatura, e.target.value)}
                            >
                              <option value="">Sin preferencia — cualquier grupo</option>
                              {(a.grupos || []).map(g => (
                                <option key={g.idGrupo} value={g.idGrupo}>
                                  {g.nombre} · {g.docente || 'Docente TBD'} · Cupo {g.cupo}
                                  {g.jornadas?.length ? ' · ' + g.jornadas.join(', ') : ''}
                                </option>
                              ))}
                            </select>
                          </label>
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
          }
        </section>
      </div>
    </div>
  )
}
