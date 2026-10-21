/**
 * Casos de uso del recurso Asignaturas.
 *
 * @module application/service/asignaturasService
 */

/**
 * @typedef {import('../../domain/ports/ScheduleuGateway.js').ScheduleuGateway} ScheduleuGateway
 * @typedef {import('../../domain/model/index.js').Asignatura} Asignatura
 */

const BASE = '/asignaturas'

/**
 * Crea el servicio de asignaturas.
 * @param {ScheduleuGateway} gateway Puerto de acceso a datos.
 */
export function createAsignaturasService(gateway) {
  return {
    /** @returns {Promise<Asignatura[]>} */
    list: () => gateway.get(BASE),
    /** @param {Partial<Asignatura>} body @returns {Promise<Asignatura>} */
    create: (body) => gateway.post(BASE, body),
    /** @param {number|string} id @param {Partial<Asignatura>} body @returns {Promise<Asignatura>} */
    update: (id, body) => gateway.put(`${BASE}/${id}`, body),
    /** @param {number|string} id @returns {Promise<any>} */
    remove: (id) => gateway.delete(`${BASE}/${id}`),
  }
}

export default createAsignaturasService
