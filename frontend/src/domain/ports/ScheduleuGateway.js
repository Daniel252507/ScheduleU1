/**
 * Puerto (contrato) de acceso a datos de ScheduleU.
 *
 * Define la interfaz que debe implementar cualquier adaptador concreto
 * (por ejemplo, el adaptador HTTP basado en `fetch`). La capa de aplicación
 * depende únicamente de esta abstracción y nunca del adaptador.
 *
 * @module domain/ports/ScheduleuGateway
 */

/**
 * @typedef {import('../model/index.js').Usuario} Usuario
 * @typedef {import('../model/index.js').Preferencias} Preferencias
 */

/**
 * Contrato de acceso a datos. Implementar todos los métodos.
 *
 * @interface ScheduleuGateway
 */
export class ScheduleuGateway {
  /**
   * Realiza una petición GET al recurso indicado.
   * @param {string} path Ruta relativa (ej: '/asignaturas').
   * @returns {Promise<any>}
   */
  async get(path) {
    throw new Error('ScheduleuGateway.get no implementado')
  }

  /**
   * Realiza una petición POST al recurso indicado.
   * @param {string} path Ruta relativa.
   * @param {any} [body] Cuerpo de la petición.
   * @returns {Promise<any>}
   */
  async post(path, body) {
    throw new Error('ScheduleuGateway.post no implementado')
  }

  /**
   * Realiza una petición PUT al recurso indicado.
   * @param {string} path Ruta relativa.
   * @param {any} [body] Cuerpo de la petición.
   * @returns {Promise<any>}
   */
  async put(path, body) {
    throw new Error('ScheduleuGateway.put no implementado')
  }

  /**
   * Realiza una petición DELETE al recurso indicado.
   * @param {string} path Ruta relativa.
   * @returns {Promise<any>}
   */
  async delete(path) {
    throw new Error('ScheduleuGateway.delete no implementado')
  }

  /**
   * Inicia sesión y persiste el token y los datos del usuario.
   * @param {string} correo
   * @param {string} contrasena
   * @returns {Promise<Usuario>}
   */
  async login(correo, contrasena) {
    throw new Error('ScheduleuGateway.login no implementado')
  }

  /**
   * Establece una nueva contraseña a partir del token recibido por correo.
   * @param {string} token Token del enlace de correo.
   * @param {string} contrasena Nueva contraseña.
   * @returns {Promise<void>}
   */
  async restablecerContrasena(token, contrasena) {
    throw new Error('ScheduleuGateway.restablecerContrasena no implementado')
  }

  /**
   * Cierra la sesión eliminando las credenciales almacenadas.
   * @returns {void}
   */
  logout() {
    throw new Error('ScheduleuGateway.logout no implementado')
  }

  /**
   * Devuelve el usuario actualmente autenticado o `null`.
   * @returns {Usuario|null}
   */
  getCurrentUser() {
    throw new Error('ScheduleuGateway.getCurrentUser no implementado')
  }

  /**
   * Devuelve el token de sesión almacenado o `null`.
   * @returns {string|null}
   */
  getToken() {
    throw new Error('ScheduleuGateway.getToken no implementado')
  }
}

export default ScheduleuGateway
