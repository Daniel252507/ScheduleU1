/**
 * Casos de uso del recurso Horarios.
 *
 * @module application/service/horariosService
 */

/**
 * @typedef {import('../../domain/ports/ScheduleuGateway.js').ScheduleuGateway} ScheduleuGateway
 * @typedef {import('../../domain/model/index.js').Horario} Horario
 */

const BASE = '/horarios'

/**
 * Crea el servicio de horarios.
 * @param {ScheduleuGateway} gateway Puerto de acceso a datos.
 */
export function createHorariosService(gateway) {
  return {
    /** @returns {Promise<Horario[]>} */
    list: () => gateway.get(BASE),
    /** @param {Partial<Horario>} body @returns {Promise<Horario>} */
    create: (body) => gateway.post(BASE, body),
    /** @param {number|string} id @returns {Promise<any>} */
    remove: (id) => gateway.delete(`${BASE}/${id}`),
  }
}

export default createHorariosService
