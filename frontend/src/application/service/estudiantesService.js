/**
 * Casos de uso del recurso Estudiantes.
 *
 * @module application/service/estudiantesService
 */

/**
 * @typedef {import('../../domain/ports/ScheduleuGateway.js').ScheduleuGateway} ScheduleuGateway
 * @typedef {import('../../domain/model/index.js').Estudiante} Estudiante
 */

const BASE = '/estudiantes'

/**
 * Crea el servicio de estudiantes.
 * @param {ScheduleuGateway} gateway Puerto de acceso a datos.
 */
export function createEstudiantesService(gateway) {
  return {
    /** @returns {Promise<Estudiante[]>} */
    list: () => gateway.get(BASE),
    /** @param {Partial<Estudiante>} body @returns {Promise<Estudiante>} */
    create: (body) => gateway.post(BASE, body),
    /** @param {number|string} id @param {Partial<Estudiante>} body @returns {Promise<Estudiante>} */
    update: (id, body) => gateway.put(`${BASE}/${id}`, body),
    /** @param {number|string} id @returns {Promise<any>} */
    remove: (id) => gateway.delete(`${BASE}/${id}`),
  }
}

export default createEstudiantesService
