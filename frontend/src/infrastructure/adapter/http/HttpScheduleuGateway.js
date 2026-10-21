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

  async restablecerContrasena(token, contrasena) {
    const res = await fetch(`${this.baseUrl}/auth/restablecer-contrasena`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, contrasena }),
    })
    if (!res.ok) throw new Error('El enlace no es valido o ya expiro')
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
