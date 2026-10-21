/**
 * Modelo de dominio de ScheduleU.
 *
 * Este módulo solo contiene definiciones de tipos (JSDoc typedefs) que
 * describen las entidades del negocio. No depende de React, del navegador
 * ni de ninguna capa de infraestructura.
 *
 * @module domain/model
 */

/**
 * Usuario autenticado en el sistema.
 * @typedef {Object} Usuario
 * @property {number} [idUsuario] Identificador del usuario.
 * @property {string} nombre Nombre completo.
 * @property {string} correo Correo institucional.
 * @property {'ADMINISTRADOR'|'COORDINADOR_ACADEMICO'|'ESTUDIANTE'|string} rol Rol del usuario.
 * @property {string} token Token de sesión devuelto por el backend.
 */

/**
 * Asignatura (materia) del programa académico.
 * @typedef {Object} Asignatura
 * @property {number} [idAsignatura] Identificador.
 * @property {string} nombre Nombre de la asignatura.
 * @property {number} [creditos] Número de créditos.
 * @property {string} [codigo] Código (ej: SIS101).
 * @property {number} [semestreRecomendado] Semestre recomendado.
 * @property {Grupo[]} [grupos] Grupos disponibles asociados.
 */

/**
 * Estudiante.
 * @typedef {Object} Estudiante
 * @property {number} [idEstudiante] Identificador.
 * @property {string} nombre Nombre completo.
 * @property {string} [codigo] Código académico.
 * @property {number} [semestre] Semestre actual.
 * @property {string} [correo] Correo institucional.
 */

/**
 * Docente.
 * @typedef {Object} Docente
 * @property {number} [idDocente] Identificador.
 * @property {string} nombre Nombre completo.
 * @property {string} [correo] Correo institucional.
 * @property {string} [identificacion] Identificación interna (ej: D001).
 */

/**
 * Salón (espacio físico).
 * @typedef {Object} Salon
 * @property {number} [idSalon] Identificador.
 * @property {string} nombre Identificación del salón (ej: A-101).
 * @property {number} [capacidad] Capacidad máxima.
 * @property {string} [ubicacion] Ubicación física.
 */

/**
 * Grupo de una asignatura.
 * @typedef {Object} Grupo
 * @property {number} [idGrupo] Identificador.
 * @property {string} [nombre] Nombre del grupo (G1, G2...).
 * @property {number} [cupo] Cupo máximo.
 * @property {Asignatura} [asignatura] Asignatura asociada.
 * @property {Docente} [docente] Docente asociado.
 * @property {Salon} [salon] Salón asociado.
 * @property {string[]} [jornadas] Jornadas disponibles.
 */

/**
 * Franja horaria de un grupo.
 * @typedef {Object} Horario
 * @property {number} [idHorario] Identificador.
 * @property {Grupo} [grupo] Grupo asociado.
 * @property {string} dia Día de la semana (LUNES..SABADO).
 * @property {string} horaInicio Hora de inicio (HH:MM).
 * @property {string} horaFin Hora de fin (HH:MM).
 */

/**
 * Asignación de una materia a un estudiante.
 * @typedef {Object} Asignacion
 * @property {number} [idAsignacion] Identificador.
 * @property {Estudiante} [estudiante] Estudiante asociado.
 * @property {Grupo} [grupo] Grupo asignado.
 * @property {'ASIGNADO'|'PENDIENTE'|'CONFLICTO'|string} [estado] Estado de la asignación.
 * @property {number} [idAsignatura] Id de la asignatura.
 * @property {number} [idGrupo] Id del grupo.
 * @property {string} [nombreAsignatura] Nombre de la asignatura.
 * @property {string} [nombreGrupo] Nombre del grupo.
 * @property {string} [docente] Nombre del docente.
 * @property {string} [salon] Nombre del salón.
 * @property {string[]} [horarios] Franjas horarias legibles.
 * @property {string} [razonPendiente] Motivo de la pendiente (si aplica).
 */

/**
 * Preferencias de horario de un estudiante.
 * @typedef {Object} Preferencias
 * @property {'MANANA'|'TARDE'|'NOCHE'|'SIN_PREFERENCIA'|string} [jornadaPreferida] Jornada preferida.
 * @property {boolean} [priorizaDocente] Si prioriza docentes de alta valoración.
 * @property {string} [observaciones] Observaciones libres.
 * @property {Array<{idAsignatura:number, idGrupoPreferido:(number|null), prioridad:number}>} [materiasSeleccionadas] Materias seleccionadas.
 */

export {}
