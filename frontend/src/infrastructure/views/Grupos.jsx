import { useEffect, useState } from 'react'
import {
  gruposService,
  asignaturasService,
  docentesService,
  salonesService,
} from '../config/container.js'

export default function Grupos() {
  const [items, setItems] = useState([])
  const [asigs, setAsigs] = useState([])
  const [docs, setDocs] = useState([])
  const [salones, setSalones] = useState([])
  const [form, setForm] = useState({ id_asignatura: '', id_docente: '', id_salon: '', cupo: 30, nombre: 'G1' })
  const [error, setError] = useState('')

  async function load() {
    try {
      const [g, a, d, s] = await Promise.all([
        gruposService.list(),
        asignaturasService.list(),
        docentesService.list(),
        salonesService.list(),
      ])
      setItems(Array.isArray(g) ? g : [])
      setAsigs(a); setDocs(d); setSalones(s)
    } catch (err) { setError('Error al cargar') }
  }
  useEffect(() => { load() }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    try {
      const body = {
        cupo: Number(form.cupo),
        nombre: form.nombre,
        asignatura: form.id_asignatura ? { idAsignatura: Number(form.id_asignatura) } : null,
        docente: form.id_docente ? { idDocente: Number(form.id_docente) } : null,
        salon: form.id_salon ? { idSalon: Number(form.id_salon) } : null,
      }
      await gruposService.create(body)
      setForm({ id_asignatura: '', id_docente: '', id_salon: '', cupo: 30, nombre: 'G1' })
      load()
    } catch (err) { setError('No se pudo crear el grupo') }
  }

  async function handleDelete(id) {
    if (!confirm('¿Eliminar?')) return
    await gruposService.remove(id)
    load()
  }

  return (
    <div className="page">
      <header className="page-header">
        <h1>Grupos</h1>
        <p>Asocia cada grupo con su asignatura, docente, salon y cupo maximo</p>
      </header>
      {error && <div className="error-msg">{error}</div>}

      <div className="two-col">
        <section className="card">
          <h3>➕ Crear Grupo</h3>
          <form onSubmit={handleSubmit} className="form-grid">
            <label><span>Asignatura</span>
              <select value={form.id_asignatura} onChange={e => setForm({...form, id_asignatura: e.target.value})} required>
                <option value="">Selecciona...</option>
                {asigs.map(a => <option key={a.idAsignatura} value={a.idAsignatura}>{a.nombre}</option>)}
              </select>
            </label>
            <label><span>Docente</span>
              <select value={form.id_docente} onChange={e => setForm({...form, id_docente: e.target.value})}>
                <option value="">(ninguno)</option>
                {docs.map(d => <option key={d.idDocente} value={d.idDocente}>{d.nombre}</option>)}
              </select>
            </label>
            <label><span>Salon</span>
              <select value={form.id_salon} onChange={e => setForm({...form, id_salon: e.target.value})}>
                <option value="">(ninguno)</option>
                {salones.map(s => <option key={s.idSalon} value={s.idSalon}>{s.nombre} ({s.capacidad})</option>)}
              </select>
            </label>
            <label><span>Nombre (G1, G2...)</span>
              <input value={form.nombre} onChange={e => setForm({...form, nombre: e.target.value})} />
            </label>
            <label><span>Cupo maximo</span>
              <input type="number" min={1} value={form.cupo} onChange={e => setForm({...form, cupo: e.target.value})} />
            </label>
            <div className="form-actions"><button className="btn-primary">Crear grupo</button></div>
          </form>
        </section>

        <section className="card">
          <h3>📋 Grupos ({items.length})</h3>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>#</th><th>Nombre</th><th>Asignatura</th><th>Docente</th><th>Salon</th><th>Cupo</th><th></th>
                </tr>
              </thead>
              <tbody>
                {items.length === 0 && <tr><td colSpan={7}><em>Sin datos</em></td></tr>}
                {items.map(g => (
                  <tr key={g.idGrupo}>
                    <td>{g.idGrupo}</td>
                    <td>{g.nombre}</td>
                    <td>{g.asignatura?.nombre || '-'}</td>
                    <td>{g.docente?.nombre || '-'}</td>
                    <td>{g.salon?.nombre || '-'}</td>
                    <td>{g.cupo}</td>
                    <td className="actions">
                      <button className="btn-delete" onClick={() => handleDelete(g.idGrupo)}>Eliminar</button>
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
