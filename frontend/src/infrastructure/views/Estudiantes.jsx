import CrudView from '../components/CrudView.jsx'
import { estudiantesService } from '../config/container.js'

export default function Estudiantes() {
  return (
    <CrudView
      title="Estudiantes"
      subtitle="Gestiona los estudiantes. Al crearlos se les envia un correo para establecer su contrasena"
      service={estudiantesService}
      initialForm={{ nombre: '', codigo: '', semestre: 1, correo: '' }}
      labels={{
        nombre: 'Nombre completo',
        codigo: 'Codigo academico (ej: 20211001)',
        semestre: 'Semestre actual',
        correo: 'Correo institucional',
      }}
      columns={[
        { key: 'idEstudiante', label: '#' },
        { key: 'nombre', label: 'Nombre' },
        { key: 'codigo', label: 'Codigo' },
        { key: 'semestre', label: 'Semestre' },
        { key: 'correo', label: 'Correo' },
      ]}
    />
  )
}
