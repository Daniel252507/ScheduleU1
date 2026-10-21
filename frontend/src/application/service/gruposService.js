/**
 * Casos de uso del recurso Grupos.
 *
 * @module application/service/gruposService
 */

/**
 * @typedef {import('../../domain/ports/ScheduleuGateway.js').ScheduleuGateway} ScheduleuGateway
 * @typedef {import('../../domain/model/index.js').Grupo} Grupo
 */

const BASE = '/grupos'

/**
 * Crea el servicio de grupos.
 * @param {ScheduleuGateway} gateway Puerto de acceso a datos.
 */
export function createGruposService(gateway) {
  return {
    /** @returns {Promise<Grupo[]>} */
    list: () => gateway.get(BASE),
    /** @param {Partial<Grupo>} body @returns {Promise<Grupo>} */
    create: (body) => gateway.post(BASE, body),
    /** @param {number|string} id @returns {Promise<any>} */
    remove: (id) => gateway.delete(`${BASE}/${id}`),
  }
}

export default createGruposService
