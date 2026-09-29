import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../services/api.js'

const DIAS = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO']
const HORAS = [
  '07:00', '08:00', '09:00', '10:00', '11:00', '12:00',
  '13:00', '14:00', '15:00', '16:00', '17:00', '18:00',
  '19:00', '20:00', '21:00',
]

const COLORS = [
  { bg: '#dbeafe', fg: '#1d4ed8', bd: '#93c5fd' },
  { bg: '#dcfce7', fg: '#166534', bd: '#86efac' },
  { bg: '#fef9c3', fg: '#854d0e', bd: '#fde047' },
  { bg: '#fce7f3', fg: '#9d174d', bd: '#f9a8d4' },
  { bg: '#e0e7ff', fg: '#3730a3', bd: '#a5b4fc' },
  { bg: '#cffafe', fg: '#155e75', bd: '#67e8f9' },
  { bg: '#fed7aa', fg: '#9a3412', bd: '#fdba74' },
  { bg: '#ede9fe', fg: '#6b21a8', bd: '#c4b5fd' },
]

function parseHora(str) {
  return Number(str.split(':')[0]) * 60 + Number(str.split(':')[1])
}

function extraerBloques(horarioStr) {
  try {
    const [dia, rango] = horarioStr.split(' ')
    const [hi, hf] = rango.split('-')
    return {
      dia: dia.trim().toUpperCase(),
      hi: parseHora(hi.trim()),
      hf: parseHora(hf.trim()),
      raw: horarioStr,
    }
  } catch { return null }
}

export default function MiHorario() {
  const navigate = useNavigate()
  const [asignacionesConfirmadas, setAsignacionesConfirmadas] = useState([])
  const [opciones, setOpciones] = useState([])
  const [opcionSel, setOpcionSel] = useState(null)
  const [loading, setLoading] = useState(false)
  const [generando, setGenerando] = useState(false)
  const [confirmando, setConfirmando] = useState(false)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')
  const [modo, setModo] = useState('vista')

  async function loadConfirmadas() {
    setLoading(true); setError('')
    try {
      const res = await api.get('/mis-horarios/asignaciones')
      setAsignacionesConfirmadas(Array.isArray(res) ? res : [])
    } catch (err) {
      setError('No se pudieron cargar tus asignaciones.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadConfirmadas() }, [])

  async function handleGenerar() {
    setGenerando(true); setError(''); setMsg('')
    try {
      const r = await api.post('/mis-horarios/generar')
      const ops = (r.opciones || []).slice(0, 8)
      setOpciones(ops)
      setOpcionSel(ops[0] || null)
      setModo('seleccion')
      if (ops.length === 0) setError('No se encontraron combinaciones posibles con tu selección actual.')
    } catch (err) {
      setError('No se pudo generar horario. Asegúrate de haber guardado tus preferencias primero (Mis Preferencias > Guardar).')
    } finally {
      setGenerando(false)
    }
  }

  async function handleConfirmar() {
    if (!opcionSel) { setError('Selecciona una opción primero.'); return }
    setConfirmando(true); setError(''); setMsg('')
    try {
      const r = await api.post('/mis-horarios/confirmar', opcionSel)
      setMsg(r.mensaje || 'Confirmado correctamente')
      await loadConfirmadas()
      setModo('vista')
    } catch (err) {
      setError('No se pudo confirmar el horario.')
    } finally {
      setConfirmando(false)
    }
  }

  const resultados = modo === 'vista' ? asignacionesConfirmadas : (opcionSel?.resultados || [])

  const stats = useMemo(() => {
    const total = resultados.length
    const ok = resultados.filter(a => a.estado === 'ASIGNADO').length
    const pend = resultados.filter(a => a.estado === 'PENDIENTE').length
    const conf = resultados.filter(a => a.estado === 'CONFLICTO').length
    return { total, ok, pend, conf }
  }, [resultados])

  const colorMap = useMemo(() => {
    const cm = {}
    let idx = 0
    resultados.filter(a => a.estado === 'ASIGNADO').forEach(a => {
      if (!cm[a.idAsignatura]) {
        cm[a.idAsignatura] = COLORS[idx % COLORS.length]
        idx++
      }
    })
    return cm
  }, [resultados])

  const grid = useMemo(() => {
    const g = {}
    DIAS.forEach(d => { g[d] = {} })
    resultados.filter(a => a.estado === 'ASIGNADO').forEach(a => {
      ;(a.horarios || []).forEach(h => {
        const b = extraerBloques(h)
        if (!b) return
        const rowStartIdx = HORAS.findIndex(x => parseHora(x) >= b.hi)
        const rowEndIdx = HORAS.findIndex(x => parseHora(x) >= b.hf)
        const rs = rowStartIdx >= 0 ? rowStartIdx + 2 : 2
        const re = rowEndIdx >= 0 ? rowEndIdx + 1 : HORAS.length + 1
        if (!g[b.dia]) return
        const key = `${a.idAsignatura}-${a.idGrupo}-${h}`
        g[b.dia][key] = { rs, re, a, color: colorMap[a.idAsignatura] || COLORS[0] }
      })
    })
    return g
  }, [resultados, colorMap])

  function badgeEstado(e) {
    const cls = e === 'ASIGNADO' ? 'badge-green'
              : e === 'PENDIENTE' ? 'badge-yellow'
              : 'badge-red'
    return <span className={`badge ${cls}`}>{e}</span>
  }

  return (
    <div className="page">
      <header className="page-header">
        <h1>📅 Mi Horario</h1>
        <p>
          {modo === 'vista'
            ? 'Tu horario confirmado. Pulsa "Generar opciones" para ver nuevas combinaciones y elegir la que más te guste.'
            : 'Explora las combinaciones posibles. Selecciona una y pulsa "Confirmar selección" para guardar tu horario.'}
        </p>
      </header>

      {error && <div className="error-msg big">{error}</div>}
      {msg && <div className="result-card" style={{ background: '#dcfce7', borderColor: '#86efac' }}><h3 style={{ color: '#166534' }}>✅ {msg}</h3></div>}

      <div className="actions-bar">
        <button className="btn-primary big" onClick={handleGenerar} disabled={generando || confirmando}>
          🎲 {generando ? 'Calculando combinaciones...' : '🔄 Generar opciones de horario'}
        </button>
        {modo === 'seleccion' && (
          <button className="btn-primary big" onClick={handleConfirmar} disabled={!opcionSel || confirmando}>
            ✅ {confirmando ? 'Guardando...' : 'Confirmar selección'}
          </button>
        )}
        <button className="btn-secondary big" onClick={() => navigate('/mis-preferencias')}>
          📝 Cambiar mis preferencias
        </button>
        {modo === 'seleccion' && (
          <button className="btn-secondary big" onClick={() => setModo('vista')}>
            ← Volver al horario confirmado
          </button>
        )}
      </div>

      {modo === 'seleccion' && opciones.length > 0 && (
        <section className="card" style={{ marginBottom: '20px' }}>
          <h3>🎯 Elige tu combinación favorita ({opciones.length} disponible(s))</h3>
          <div className="opciones-grid">
            {opciones.map(op => (
              <div
                key={op.indice}
                className={`opcion-card ${opcionSel?.indice === op.indice ? 'sel' : ''}`}
                onClick={() => setOpcionSel(op)}
              >
                <div className="opcion-head">
                  <strong>Opción {op.indice}</strong>
                  <span className="puntaje">★ {op.puntajeTotal}</span>
                </div>
                <div className="opcion-body">
                  <div className="mini-stat bg-green" style={{ padding: '8px 10px' }}>
                    <span>Asignadas</span><strong style={{ fontSize: '20px' }}>{op.totalAsignadas}</strong>
                  </div>
                  <div className="mini-stat bg-yellow" style={{ padding: '8px 10px' }}>
                    <span>Pendientes</span><strong style={{ fontSize: '20px' }}>{op.totalPendientes}</strong>
                  </div>
                </div>
                <div className="opcion-desc">{op.descripcion || 'Combinación válida sin cruces'}</div>
                {opcionSel?.indice === op.indice && <div className="opcion-sel-badge">✓ Seleccionada</div>}
              </div>
            ))}
          </div>
        </section>
      )}

      <div className="stats-row">
        <div className="mini-stat bg-green"><span>✅ ASIGNADAS</span><strong>{stats.ok}</strong></div>
        <div className="mini-stat bg-yellow"><span>⏳ PENDIENTES</span><strong>{stats.pend}</strong></div>
        <div className="mini-stat bg-red"><span>❌ CONFLICTOS</span><strong>{stats.conf}</strong></div>
        <div className="mini-stat bg-blue"><span>📋 TOTAL</span><strong>{stats.total}</strong></div>
      </div>

      <section className="card" style={{ marginBottom: '20px', overflowX: 'auto' }}>
        <h3>🗓️ {modo === 'vista' ? 'Horario Confirmado' : `Vista Previa - Opción ${opcionSel?.indice || '-'}`}</h3>
        {loading ? <p>Cargando...</p> : stats.ok === 0 ? (
          <p style={{ color: 'var(--text-light)', padding: '20px', textAlign: 'center' }}>
            <em>{modo === 'vista'
                ? 'Aún no tienes horario confirmado. Pulsa "Generar opciones de horario" para empezar.'
                : 'Esta opción no tiene materias asignadas válidas.'}</em>
          </p>
        ) : (
          <div className="schedule-grid">
            <div className="sg-corner"><strong>Hora</strong></div>
            {DIAS.map(d => <div key={d} className="sg-day"><strong>{d.charAt(0) + d.slice(1).toLowerCase()}</strong></div>)}
            {HORAS.map(h => (
              <>
                <div key={'t-' + h} className="sg-time">{h}</div>
                {DIAS.map(d => <div key={'c-' + d + '-' + h} className="sg-cell" />)}
              </>
            ))}
            {DIAS.map(d => Object.keys(grid[d]).map(k => {
              const cell = grid[d][k]
              return (
                <div
                  key={'e-' + d + '-' + k}
                  className="sg-event"
                  style={{
                    gridColumn: (DIAS.indexOf(d) + 2),
                    gridRow: `${cell.rs} / ${cell.re}`,
                    background: cell.color.bg,
                    color: cell.color.fg,
                    borderColor: cell.color.bd,
                  }}
                  title={`${cell.a.nombreAsignatura}\nGrupo: ${cell.a.nombreGrupo || '-'}\nDocente: ${cell.a.docente || '-'}\nSalón: ${cell.a.salon || '-'}`}
                >
                  <div className="sg-mat">{cell.a.nombreAsignatura}</div>
                  <div className="sg-sub">{cell.a.nombreGrupo || ''} · {cell.a.salon || 'Salón TBD'}</div>
                  <div className="sg-doc">{cell.a.docente || 'Docente TBD'}</div>
                </div>
              )
            }))}
          </div>
        )}
      </section>

      <section className="card">
        <h3>📋 Detalle de materias ({resultados.length})</h3>
        {loading ? <p>Cargando...</p> :
          resultados.length === 0 ? <p><em>No hay datos. Guarda tus preferencias y genera opciones.</em></p> : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Asignatura</th>
                    <th>Grupo</th>
                    <th>Docente</th>
                    <th>Salón</th>
                    <th>Horarios</th>
                    <th>Estado</th>
                    <th>Motivo (si aplica)</th>
                  </tr>
                </thead>
                <tbody>
                  {resultados.map((a, i) => (
                    <tr key={a.idAsignacion || ('opt-' + i)}>
                      <td>{i + 1}</td>
                      <td><strong>{a.nombreAsignatura}</strong></td>
                      <td>{a.nombreGrupo || '-'}</td>
                      <td>{a.docente || '-'}</td>
                      <td>{a.salon || '-'}</td>
                      <td>
                        {(a.horarios || []).length === 0
                          ? <em>-</em>
                          : (a.horarios || []).map((h, j) => (
                            <span key={j} className="badge badge-day" style={{ marginRight: '4px' }}>{h}</span>
                          ))}
                      </td>
                      <td>{badgeEstado(a.estado)}</td>
                      <td style={{ color: 'var(--text-light)' }}>{a.razonPendiente || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
      </section>
    </div>
  )
}
