import CrudView from '../components/CrudView.jsx'

export default function Docentes() {
  return (
    <CrudView
      title="Docentes"
      subtitle="Gestiona el personal docente del programa"
      endpoint="/docentes"
      initialForm={{ nombre: '', correo: '', identificacion: '' }}
      labels={{
        nombre: 'Nombre completo',
        correo: 'Correo institucional',
        identificacion: 'Identificacion (ej: D001)',
      }}
      columns={[
        { key: 'idDocente', label: '#' },
        { key: 'nombre', label: 'Nombre' },
        { key: 'identificacion', label: 'ID' },
        { key: 'correo', label: 'Correo' },
      ]}
    />
  )
}
