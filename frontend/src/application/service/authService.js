/**
 * Casos de uso de autenticación y sesión.
 *
 * Depende del puerto {@link ScheduleuGateway}, nunca del adaptador concreto.
 *
 * @module application/service/authService
 */

/**
 * @typedef {import('../../domain/ports/ScheduleuGateway.js').ScheduleuGateway} ScheduleuGateway
 * @typedef {import('../../domain/model/index.js').Usuario} Usuario
 */

/**
 * Crea el servicio de autenticación.
 * @param {ScheduleuGateway} gateway Puerto de acceso a datos.
 */
export function createAuthService(gateway) {
  return {
    /**
     * Inicia sesión con correo y contraseña.
     * @param {string} correo
     * @param {string} contrasena
     * @returns {Promise<Usuario>}
     */
    login: (correo, contrasena) => gateway.login(correo, contrasena),

    /**
     * Establece una nueva contraseña usando el token recibido por correo.
     * @param {string} token
     * @param {string} contrasena
     * @returns {Promise<void>}
     */
    restablecerContrasena: (token, contrasena) => gateway.restablecerContrasena(token, contrasena),

    /**
     * Cierra la sesión actual.
     * @returns {void}
     */
    logout: () => gateway.logout(),

    /**
     * Devuelve el usuario autenticado o `null`.
     * @returns {Usuario|null}
     */
    getCurrentUser: () => gateway.getCurrentUser(),

    /**
     * Devuelve el token de sesión o `null`.
     * @returns {string|null}
     */
    getToken: () => gateway.getToken(),
  }
}

export default createAuthService
