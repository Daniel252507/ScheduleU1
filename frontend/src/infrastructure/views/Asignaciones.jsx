import { useEffect, useState } from 'react'
import { asignacionesService } from '../config/container.js'

export default function Asignaciones() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function load() {
    setLoading(true); setError('')
    try { setData(await asignacionesService.list()) } catch (err) { setError('Error al cargar el reporte de asignaciones') }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  function badgeEstado(e) {
    const cls = e === 'ASIGNADO' ? 'badge-green'
              : e === 'PENDIENTE' ? 'badge-yellow'
              : 'badge-red'
    return <span className={`badge ${cls}`}>{e}</span>
  }

  const counts = data.reduce((acc, a) => {
    acc[a.estado] = (acc[a.estado] || 0) + 1; return acc
  }, {})
  const estudiantesUnicos = new Set(data.map(a => a.estudiante?.idEstudiante).filter(Boolean)).size
  const gruposUtilizados = new Set(data.filter(a => a.estado === 'ASIGNADO').map(a => a.grupo?.idGrupo).filter(Boolean)).size

  return (
    <div className="page">
      <header className="page-header">
        <h1>📋 Asignaciones (Reporte)</h1>
        <p>Vista de solo lectura. Los horarios son seleccionados y confirmados por cada estudiante desde su panel personal.</p>
      </header>

      {error && <div className="error-msg">{error}</div>}

      <div className="actions-bar">
        <button className="btn-secondary big" onClick={load}>🔄 Actualizar reporte</button>
      </div>

      <div className="stats-row">
        <div className="mini-stat bg-green">
          <span>✅ ASIGNADOS</span><strong>{counts.ASIGNADO || 0}</strong>
        </div>
        <div className="mini-stat bg-yellow">
          <span>⏳ PENDIENTES</span><strong>{counts.PENDIENTE || 0}</strong>
        </div>
        <div className="mini-stat bg-red">
          <span>❌ CONFLICTOS</span><strong>{counts.CONFLICTO || 0}</strong>
        </div>
        <div className="mini-stat bg-blue">
          <span>📋 TOTAL REGISTROS</span><strong>{data.length}</strong>
        </div>
        <div className="mini-stat" style={{ background: '#e0e7ff', color: '#3730a3' }}>
          <span>🎓 ESTUDIANTES</span><strong>{estudiantesUnicos}</strong>
        </div>
        <div className="mini-stat" style={{ background: '#fee2e2', color: '#7f1d1d' }}>
          <span>👥 GRUPOS UTILIZADOS</span><strong>{gruposUtilizados}</strong>
        </div>
      </div>

      <section className="card">
        <h3>Detalle de asignaciones ({data.length})</h3>
        {loading ? <p>Cargando...</p> :
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>#</th><th>Estudiante</th><th>Sem.</th>
                  <th>Asignatura</th><th>Grupo</th><th>Docente</th>
                  <th>Salón</th><th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {data.length === 0 && <tr><td colSpan={8}><em>No hay asignaciones registradas. Los estudiantes deben generar y confirmar su propio horario.</em></td></tr>}
                {data.map(a => (
                  <tr key={a.idAsignacion}>
                    <td>{a.idAsignacion}</td>
                    <td>{a.estudiante?.nombre || '-'}</td>
                    <td>{a.estudiante?.semestre || '-'}</td>
                    <td>{a.grupo?.asignatura?.nombre || a.inscripcion?.asignatura?.nombre || '-'}</td>
                    <td>{a.grupo?.nombre || '-'}</td>
                    <td>{a.grupo?.docente?.nombre || '-'}</td>
                    <td>{a.grupo?.salon?.nombre || '-'}</td>
                    <td>{badgeEstado(a.estado)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        }
      </section>
    </div>
  )
}
