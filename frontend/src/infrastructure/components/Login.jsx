import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { GoogleLogin } from '@react-oauth/google'
import { authService } from '../config/container.js'

/** Client ID de Google (build-time). Si no está definido, no se ofrece el botón. */
const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID

export default function Login({ onLogin }) {
  const [correo, setCorreo] = useState('admin@scheduleu.com')
  const [contrasena, setContrasena] = useState('admin123')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(false)
    try {
      const data = await authService.login(correo, contrasena)
      onLogin(authService.getCurrentUser())
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  /**
   * Google entrega un ID token (credential) que el backend verifica antes de
   * emitir la sesión propia de ScheduleU.
   */
  async function handleGoogle(credentialResponse) {
    setError('')
    setLoading(true)
    try {
      await authService.loginConGoogle(credentialResponse.credential)
      onLogin(authService.getCurrentUser())
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <div className="login-header">
          <h1>📅 ScheduleU</h1>
          <p>Sistema de Gestion y Asignacion de Horarios</p>
        </div>
        <form onSubmit={handleSubmit} className="login-form">
          <label>
            <span>Correo</span>
            <input
              type="email"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
              placeholder="correo@ejemplo.com"
              required
            />
          </label>
          <label>
            <span>Contraseña</span>
            <input
              type="password"
              value={contrasena}
              onChange={(e) => setContrasena(e.target.value)}
              placeholder="••••••••"
              required
            />
          </label>
          {error && <div className="error-msg">{error}</div>}
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Ingresando...' : 'Iniciar Sesion'}
          </button>
        </form>

        {googleClientId && (
          <div className="login-google">
            <div className="login-separador">
              <span>o continúa con</span>
            </div>
            <GoogleLogin
              onSuccess={handleGoogle}
              onError={() => setError('No se pudo completar el inicio de sesión con Google')}
              text="continue_with"
              locale="es"
            />
          </div>
        )}

        <div className="login-tips">
          <p><strong>Cuentas de prueba:</strong></p>
          <ul>
            <li>admin@scheduleu.com / admin123 (Administrador)</li>
            <li>coordinador@scheduleu.com / coord123 (Coordinador)</li>
            <li>estudiante@scheduleu.com / estudiante123 (Estudiante)</li>
          </ul>
        </div>
      </div>
    </div>
  )
}
