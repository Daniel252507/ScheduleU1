/**
 * Casos de uso del recurso Salones.
 *
 * @module application/service/salonesService
 */

/**
 * @typedef {import('../../domain/ports/ScheduleuGateway.js').ScheduleuGateway} ScheduleuGateway
 * @typedef {import('../../domain/model/index.js').Salon} Salon
 */

const BASE = '/salones'

/**
 * Crea el servicio de salones.
 * @param {ScheduleuGateway} gateway Puerto de acceso a datos.
 */
export function createSalonesService(gateway) {
  return {
    /** @returns {Promise<Salon[]>} */
    list: () => gateway.get(BASE),
    /** @param {Partial<Salon>} body @returns {Promise<Salon>} */
    create: (body) => gateway.post(BASE, body),
    /** @param {number|string} id @param {Partial<Salon>} body @returns {Promise<Salon>} */
    update: (id, body) => gateway.put(`${BASE}/${id}`, body),
    /** @param {number|string} id @returns {Promise<any>} */
    remove: (id) => gateway.delete(`${BASE}/${id}`),
  }
}

export default createSalonesService
