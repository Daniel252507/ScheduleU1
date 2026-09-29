import { useEffect, useState } from 'react'
import { api } from '../services/api.js'

const DIAS = ['LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO']

export default function Horarios() {
  const [items, setItems] = useState([])
  const [grupos, setGrupos] = useState([])
  const [form, setForm] = useState({ id_grupo: '', dia: 'LUNES', horaInicio: '08:00', horaFin: '10:00' })
  const [error, setError] = useState('')

  async function load() {
    try {
      const [h, g] = await Promise.all([api.get('/horarios'), api.get('/grupos')])
      setItems(h); setGrupos(g)
    } catch (err) { setError('Error al cargar') }
  }
  useEffect(() => { load() }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    try {
      await api.post('/horarios', {
        grupo: { idGrupo: Number(form.id_grupo) },
        dia: form.dia,
        horaInicio: form.horaInicio,
        horaFin: form.horaFin,
      })
      setForm({ id_grupo: '', dia: 'LUNES', horaInicio: '08:00', horaFin: '10:00' })
      load()
    } catch (err) { setError('No se pudo crear el horario') }
  }

  async function handleDelete(id) {
    if (!confirm('¿Eliminar?')) return
    await api.delete(`/horarios/${id}`)
    load()
  }

  return (
    <div className="page">
      <header className="page-header">
        <h1>Horarios</h1>
        <p>Define los dias y horas de cada grupo</p>
      </header>
      {error && <div className="error-msg">{error}</div>}

      <div className="two-col">
        <section className="card">
          <h3>➕ Agregar Horario</h3>
          <form onSubmit={handleSubmit} className="form-grid">
            <label><span>Grupo</span>
              <select value={form.id_grupo} onChange={e => setForm({...form, id_grupo: e.target.value})} required>
                <option value="">Selecciona...</option>
                {grupos.map(g => <option key={g.idGrupo} value={g.idGrupo}>
                  {g.nombre} - {g.asignatura?.nombre || '?'}
                </option>)}
              </select>
            </label>
            <label><span>Dia</span>
              <select value={form.dia} onChange={e => setForm({...form, dia: e.target.value})}>
                {DIAS.map(d => <option key={d} value={d}>{d}</option>)}
              </select>
            </label>
            <label><span>Hora inicio (HH:MM)</span>
              <input value={form.horaInicio} onChange={e => setForm({...form, horaInicio: e.target.value})} placeholder="08:00" />
            </label>
            <label><span>Hora fin (HH:MM)</span>
              <input value={form.horaFin} onChange={e => setForm({...form, horaFin: e.target.value})} placeholder="10:00" />
            </label>
            <div className="form-actions"><button className="btn-primary">Crear horario</button></div>
          </form>
        </section>

        <section className="card">
          <h3>📋 Horarios ({items.length})</h3>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr><th>#</th><th>Grupo</th><th>Asignatura</th><th>Dia</th><th>Inicio</th><th>Fin</th><th></th></tr>
              </thead>
              <tbody>
                {items.length === 0 && <tr><td colSpan={7}><em>Sin datos</em></td></tr>}
                {items.map(h => (
                  <tr key={h.idHorario}>
                    <td>{h.idHorario}</td>
                    <td>{h.grupo?.nombre}</td>
                    <td>{h.grupo?.asignatura?.nombre}</td>
                    <td><span className="badge badge-day">{h.dia}</span></td>
                    <td>{h.horaInicio}</td>
                    <td>{h.horaFin}</td>
                    <td className="actions">
                      <button className="btn-delete" onClick={() => handleDelete(h.idHorario)}>Eliminar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  )
}
