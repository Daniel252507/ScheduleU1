import { ScheduleuGateway } from '../../../domain/ports/ScheduleuGateway.js'

/**
 * Adaptador HTTP del puerto {@link ScheduleuGateway}.
 *
 * Es el único punto de la aplicación que conoce `fetch`, la URL base de la
 * API y el manejo de credenciales/errores de autenticación.
 *
 * @implements {ScheduleuGateway}
 */
export class HttpScheduleuGateway extends ScheduleuGateway {
  /**
   * @param {string} [baseUrl] URL base de la API.
   */
  constructor(baseUrl = '/api') {
    super()
    this.baseUrl = baseUrl
  }

  /**
   * Devuelve el token de sesión almacenado.
   * @returns {string|null}
   */
  getToken() {
    return localStorage.getItem('scheduleu_token')
  }

  /**
   * Construye las cabeceras de autenticación.
   * @returns {Record<string, string>}
   * @private
   */
  _authHeaders() {
    const token = this.getToken()
    return {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : '',
    }
  }

  /**
   * Cierra la sesión cuando la API responde 401/403.
   * @private
   */
  _handleAuthError() {
    localStorage.removeItem('scheduleu_token')
    localStorage.removeItem('scheduleu_user')
    if (window.location.pathname !== '/') {
      window.location.href = '/'
    } else {
      window.location.reload()
    }
  }

  /**
   * @private
   */
  async _request(method, path, body = null) {
    const options = { method, headers: this._authHeaders() }
    if (body) options.body = JSON.stringify(body)
    const res = await fetch(`${this.baseUrl}${path}`, options)
    if (res.status === 401 || res.status === 403) {
      this._handleAuthError()
      throw new Error('Sesion expirada. Por favor inicia sesion nuevamente.')
    }
    if (!res.ok) throw new Error(`Error HTTP ${res.status}`)
    const text = await res.text()
    return text ? JSON.parse(text) : null
  }

  async get(path) {
    return this._request('GET', path)
  }

  async post(path, body) {
    return this._request('POST', path, body)
  }

  async put(path, body) {
    return this._request('PUT', path, body)
  }

  async delete(path) {
    return this._request('DELETE', path)
  }

  async login(correo, contrasena) {
    const res = await fetch(`${this.baseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ correo, contrasena }),
    })
    if (!res.ok) throw new Error('Credenciales invalidas')
    const data = await res.json()
    localStorage.setItem('scheduleu_token', data.token)
    localStorage.setItem('scheduleu_user', JSON.stringify(data))
    return data
  }

  /**
   * Inicia sesión con Google. Si el correo no está registrado, el backend
   * responde con `requiereRegistro: true` y NO se guarda sesión alguna.
   */
  async loginConGoogle(idToken) {
    const res = await fetch(`${this.baseUrl}/auth/google`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idToken }),
    })
    const data = await res.json().catch(() => null)
    if (!res.ok) {
      // El backend explica el motivo (por ejemplo, token inválido).
      throw new Error((data && data.error) || 'No se pudo iniciar sesion con Google')
    }
    if (data && data.requiereRegistro) {
      return data
    }
    this._guardarSesion(data)
    return data
  }

  /**
   * Completa el alta como estudiante de una cuenta de Google nueva.
   */
  async registrarEstudianteConGoogle(idToken, nombre, codigo, semestre) {
    const res = await fetch(`${this.baseUrl}/auth/google/registro`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idToken, nombre, codigo, semestre }),
    })
    const data = await res.json().catch(() => null)
    if (!res.ok) {
      throw new Error((data && data.error) || 'No se pudo crear el usuario')
    }
    this._guardarSesion(data)
    return data
  }

  /**
   * Agrega las clases del horario confirmado del estudiante a su Google
   * Calendar. El backend obtiene el horario a partir del JWT; aquí solo se
   * envía el token de acceso de Google con scope `calendar.events`.
   * @param {string} accessToken Token OAuth devuelto por Google.
   * @returns {Promise<{eventosCreados: number, mensaje: string}>}
   */
  async exportarACalendar(accessToken) {
    const res = await fetch(`${this.baseUrl}/calendario/exportar`, {
      method: 'POST',
      headers: this._authHeaders(),
      body: JSON.stringify({ accessToken }),
    })
    const data = await res.json().catch(() => null)
    if (!res.ok) {
      throw new Error((data && (data.mensaje || data.error)) || 'No se pudieron agregar las clases al calendario')
    }
    return data
  }

  /**
   * Envía una pregunta al asistente académico. El backend construye el
   * contexto con el horario del estudiante (obtenido del JWT).
   * @param {string} pregunta Pregunta en lenguaje natural.
   * @returns {Promise<{respuesta: string}>}
   */
  async consultarAsistente(pregunta) {
    const res = await fetch(`${this.baseUrl}/asistente/consultar`, {
      method: 'POST',
      headers: this._authHeaders(),
      body: JSON.stringify({ pregunta }),
    })
    const data = await res.json().catch(() => null)
    if (!res.ok) {
      throw new Error((data && (data.mensaje || data.error)) || 'El asistente no pudo responder')
    }
    return data
  }

  /**
   * Persiste el token y los datos del usuario autenticado.
   * @private
   */
  _guardarSesion(data) {
    localStorage.setItem('scheduleu_token', data.token)
    localStorage.setItem('scheduleu_user', JSON.stringify(data))
  }

  logout() {
    localStorage.removeItem('scheduleu_token')
    localStorage.removeItem('scheduleu_user')
  }

  getCurrentUser() {
    const raw = localStorage.getItem('scheduleu_user')
    return raw ? JSON.parse(raw) : null
  }
}

export default HttpScheduleuGateway
