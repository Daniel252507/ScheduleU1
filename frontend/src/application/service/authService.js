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
     * Inicia sesión con la cuenta de Google del usuario.
     * Si el correo todavía no está registrado, devuelve una respuesta con
     * `requiereRegistro: true`: hay que completar el alta con
     * `registrarEstudianteConGoogle`.
     * @param {string} idToken Token entregado por Google Identity Services.
     * @returns {Promise<{requiereRegistro?: boolean, nombre?: string, correo?: string}>}
     */
    loginConGoogle: (idToken) => gateway.loginConGoogle(idToken),

    /**
     * Completa el alta como estudiante de una cuenta de Google nueva.
     * @param {string} idToken Token entregado por Google Identity Services.
     * @param {string} nombre Nombre del estudiante.
     * @param {string} codigo Código académico.
     * @param {number} semestre Semestre actual.
     * @returns {Promise<Usuario>}
     */
    registrarEstudianteConGoogle: (idToken, nombre, codigo, semestre) =>
      gateway.registrarEstudianteConGoogle(idToken, nombre, codigo, semestre),

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
