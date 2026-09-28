import CrudView from '../components/CrudView.jsx'

export default function Asignaturas() {
  return (
    <CrudView
      title="Asignaturas"
      subtitle="Gestiona las materias del programa de Sistemas de Informacion"
      endpoint="/asignaturas"
      initialForm={{ nombre: '', creditos: 3, codigo: '', semestreRecomendado: 1 }}
      labels={{
        nombre: 'Nombre de la asignatura',
        creditos: 'Numero de creditos',
        codigo: 'Codigo (ej: SIS101)',
        semestreRecomendado: 'Semestre recomendado',
      }}
      columns={[
        { key: 'idAsignatura', label: '#' },
        { key: 'nombre', label: 'Nombre' },
        { key: 'creditos', label: 'Creditos' },
        { key: 'codigo', label: 'Codigo' },
        { key: 'semestreRecomendado', label: 'Semestre' },
      ]}
    />
  )
}
