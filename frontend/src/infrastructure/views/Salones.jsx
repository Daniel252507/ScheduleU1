import CrudView from '../components/CrudView.jsx'
import { salonesService } from '../config/container.js'

export default function Salones() {
  return (
    <CrudView
      title="Salones"
      subtitle="Administra los espacios fisicos y su capacidad"
      service={salonesService}
      initialForm={{ nombre: '', capacidad: 30, ubicacion: '' }}
      labels={{
        nombre: 'Identificacion del salon (ej: A-101)',
        capacidad: 'Capacidad maxima de estudiantes',
        ubicacion: 'Ubicacion (ej: Bloque A, Piso 1)',
      }}
      columns={[
        { key: 'idSalon', label: '#' },
        { key: 'nombre', label: 'Salon' },
        { key: 'capacidad', label: 'Capacidad' },
        { key: 'ubicacion', label: 'Ubicacion' },
      ]}
    />
  )
}
