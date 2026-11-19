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
   * Inicia sesión con el ID token entregado por Google Identity Services
   * y persiste el token y los datos del usuario.
   *
   * Si el correo no está registrado, la respuesta trae `requiereRegistro: true`
   * con el nombre y correo propuestos (sin sesión); habrá que llamar a
   * {@link ScheduleuGateway#registrarEstudianteConGoogle}.
   * @param {string} idToken Token compacto (JWT) emitido por Google.
   * @returns {Promise<{requiereRegistro?: boolean, correo?: string, nombre?: string}>}
   */
  async loginConGoogle(idToken) {
    throw new Error('ScheduleuGateway.loginConGoogle no implementado')
  }

  /**
   * Completa el alta como estudiante de una cuenta de Google que ingresa por
   * primera vez, y persiste el token y los datos del usuario resultantes.
   * @param {string} idToken Token compacto (JWT) emitido por Google.
   * @param {string} nombre Nombre del estudiante.
   * @param {string} codigo Código académico del estudiante.
   * @param {number} semestre Semestre actual.
   * @returns {Promise<Usuario>}
   */
  async registrarEstudianteConGoogle(idToken, nombre, codigo, semestre) {
    throw new Error('ScheduleuGateway.registrarEstudianteConGoogle no implementado')
  }

  /**
   * Agrega las clases del horario confirmado del estudiante a su Google
   * Calendar. Requiere un token de acceso con el scope `calendar.events`.
   * @param {string} accessToken Token OAuth devuelto por Google.
   * @returns {Promise<{eventosCreados: number, mensaje: string}>}
   */
  async exportarACalendar(accessToken) {
    throw new Error('ScheduleuGateway.exportarACalendar no implementado')
  }

  /**
   * Envía una consulta al asistente académico (LLM) usando el horario del
   * estudiante autenticado como contexto.
   * @param {string} pregunta Pregunta en lenguaje natural.
   * @returns {Promise<{respuesta: string}>}
   */
  async consultarAsistente(pregunta) {
    throw new Error('ScheduleuGateway.consultarAsistente no implementado')
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
