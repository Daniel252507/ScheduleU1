/**
 * Casos de uso del asistente académico (IA).
 *
 * @module application/service/asistenteService
 */

/**
 * @typedef {import('../../domain/ports/ScheduleuGateway.js').ScheduleuGateway} ScheduleuGateway
 */

/**
 * Crea el servicio del asistente académico.
 * @param {ScheduleuGateway} gateway Puerto de acceso a datos.
 */
export function createAsistenteService(gateway) {
  return {
    /**
     * Envía una pregunta al asistente. El backend arma el contexto con el
     * horario confirmado del estudiante autenticado.
     * @param {string} pregunta Pregunta en lenguaje natural.
     * @returns {Promise<{respuesta: string}>}
     */
    consultar: (pregunta) => gateway.consultarAsistente(pregunta),
  }
}

export default createAsistenteService
