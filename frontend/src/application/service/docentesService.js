/**
 * Casos de uso del recurso Docentes.
 *
 * @module application/service/docentesService
 */

/**
 * @typedef {import('../../domain/ports/ScheduleuGateway.js').ScheduleuGateway} ScheduleuGateway
 * @typedef {import('../../domain/model/index.js').Docente} Docente
 */

const BASE = '/docentes'

/**
 * Crea el servicio de docentes.
 * @param {ScheduleuGateway} gateway Puerto de acceso a datos.
 */
export function createDocentesService(gateway) {
  return {
    /** @returns {Promise<Docente[]>} */
    list: () => gateway.get(BASE),
    /** @param {Partial<Docente>} body @returns {Promise<Docente>} */
    create: (body) => gateway.post(BASE, body),
    /** @param {number|string} id @param {Partial<Docente>} body @returns {Promise<Docente>} */
    update: (id, body) => gateway.put(`${BASE}/${id}`, body),
    /** @param {number|string} id @returns {Promise<any>} */
    remove: (id) => gateway.delete(`${BASE}/${id}`),
  }
}

export default createDocentesService
