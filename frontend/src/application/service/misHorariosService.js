/**
 * Casos de uso del flujo personal del estudiante ("mis horarios").
 *
 * @module application/service/misHorariosService
 */

/**
 * @typedef {import('../../domain/ports/ScheduleuGateway.js').ScheduleuGateway} ScheduleuGateway
 * @typedef {import('../../domain/model/index.js').Asignacion} Asignacion
 * @typedef {import('../../domain/model/index.js').Asignatura} Asignatura
 * @typedef {import('../../domain/model/index.js').Preferencias} Preferencias
 */

const BASE = '/mis-horarios'

/**
 * Crea el servicio de horarios personales del estudiante.
 * @param {ScheduleuGateway} gateway Puerto de acceso a datos.
 */
export function createMisHorariosService(gateway) {
  return {
    /** @returns {Promise<Asignacion[]>} */
    getAsignaciones: () => gateway.get(`${BASE}/asignaciones`),
    /** @returns {Promise<Preferencias|null>} */
    getPreferencias: () => gateway.get(`${BASE}/preferencias`),
    /** @returns {Promise<Asignatura[]>} */
    getAsignaturasDisponibles: () => gateway.get(`${BASE}/asignaturas-disponibles`),
    /** @param {object} body @returns {Promise<{mensaje?: string}>} */
    guardarPreferencias: (body) => gateway.post(`${BASE}/guardar-preferencias`, body),
    /** @returns {Promise<{opciones?: any[]}>} */
    generar: () => gateway.post(`${BASE}/generar`),
    /** @param {any} opcion @returns {Promise<{mensaje?: string}>} */
    confirmar: (opcion) => gateway.post(`${BASE}/confirmar`, opcion),
  }
}

export default createMisHorariosService
