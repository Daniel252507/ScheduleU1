import { useEffect, useState } from 'react'

export default function CrudView({
  title,
  subtitle,
  service,
  columns,
  initialForm,
  labels,
}) {
  const [items, setItems] = useState([])
  const [form, setForm] = useState(initialForm)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function load() {
    setLoading(true)
    setError('')
    try {
      const data = await service.list()
      setItems(Array.isArray(data) ? data : [])
    } catch (err) {
      setError('Error al cargar los datos')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [service])

  function resetForm() {
    setForm(initialForm)
    setEditingId(null)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    try {
      if (editingId) {
        await service.update(editingId, form)
      } else {
        await service.create(form)
      }
      resetForm()
      await load()
    } catch (err) {
      setError('Error al guardar. Verifica los campos requeridos.')
    }
  }

  async function handleEdit(item) {
    setEditingId(item[Object.keys(item)[0]])
    const f = {}
    Object.keys(initialForm).forEach(k => {
      const val = item[k]
      if (val && typeof val === 'object' && val.id) {
        f[k] = val.id
      } else if (val && typeof val === 'object') {
        f[k] = ''
      } else {
        f[k] = val ?? ''
      }
    })
    setForm(f)
  }

  async function handleDelete(id) {
    if (!confirm('¿Estas seguro de eliminar este registro?')) return
    try {
      await service.remove(id)
      await load()
    } catch (err) {
      setError('No se pudo eliminar. Verifica que no tenga datos relacionados.')
    }
  }

  return (
    <div className="page">
      <header className="page-header">
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </header>

      {error && <div className="error-msg">{error}</div>}

      <div className="two-col">
        <section className="card">
          <h3>{editingId ? '✏️ Editar' : '➕ Agregar nuevo'}</h3>
          <form onSubmit={handleSubmit} className="form-grid">
            {Object.keys(initialForm).map(key => (
              <label key={key}>
                <span>{labels[key] || key}</span>
                <input
                  type={key.toLowerCase().includes('contrasena') ? 'password' :
                        key.toLowerCase().includes('hora') || key.toLowerCase().includes('fin') ? 'text' :
                        key.includes('id_') || key === 'creditos' || key === 'semestre' || key === 'cupo' || key === 'capacidad' || key === 'semestreRecomendado' ? 'number' : 'text'}
                  value={form[key] ?? ''}
                  placeholder={labels[key] || key}
                  onChange={(e) => setForm({ ...form, [key]: e.target.value })}
                />
              </label>
            ))}
            <div className="form-actions">
              <button type="submit" className="btn-primary">
                {editingId ? 'Guardar cambios' : 'Crear'}
              </button>
              {editingId && (
                <button type="button" className="btn-secondary" onClick={resetForm}>
                  Cancelar
                </button>
              )}
            </div>
          </form>
        </section>

        <section className="card">
          <h3>📋 Registros ({items.length})</h3>
          {loading ? <p>Cargando...</p> : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    {columns.map(c => <th key={c.key}>{c.label}</th>)}
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {items.length === 0 && (
                    <tr><td colSpan={columns.length + 1}><em>No hay registros.</em></td></tr>
                  )}
                  {items.map(item => (
                    <tr key={item[Object.keys(item)[0]]}>
                      {columns.map(c => (
                        <td key={c.key}>
                          {c.render ? c.render(item) :
                           (typeof item[c.key] === 'object' && item[c.key]?.nombre)
                             ? item[c.key].nombre
                             : item[c.key] ?? ''}
                        </td>
                      ))}
                      <td className="actions">
                        <button className="btn-edit" onClick={() => handleEdit(item)}>Editar</button>
                        <button className="btn-delete" onClick={() => handleDelete(item[Object.keys(item)[0]])}>Eliminar</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
