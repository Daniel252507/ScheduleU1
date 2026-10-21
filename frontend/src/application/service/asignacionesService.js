/**
 * Casos de uso del reporte de Asignaciones.
 *
 * @module application/service/asignacionesService
 */

/**
 * @typedef {import('../../domain/ports/ScheduleuGateway.js').ScheduleuGateway} ScheduleuGateway
 * @typedef {import('../../domain/model/index.js').Asignacion} Asignacion
 */

const BASE = '/asignaciones'

/**
 * Crea el servicio de asignaciones.
 * @param {ScheduleuGateway} gateway Puerto de acceso a datos.
 */
export function createAsignacionesService(gateway) {
  return {
    /** @returns {Promise<Asignacion[]>} */
    list: () => gateway.get(BASE),
  }
}

export default createAsignacionesService
