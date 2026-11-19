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
  /** Cuenta de Google sin registrar: activa el formulario de alta como estudiante. */
  const [registro, setRegistro] = useState(null)
  const [nombre, setNombre] = useState('')
  const [codigo, setCodigo] = useState('')
  const [semestre, setSemestre] = useState(1)
  const navigate = useNavigate()

  function entrar() {
    onLogin(authService.getCurrentUser())
    navigate('/')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(false)
    try {
      await authService.login(correo, contrasena)
      entrar()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  /**
   * Google entrega un ID token (credential) que el backend verifica.
   * Si el correo ya existe se inicia sesión; si no, se piden los datos para
   * crear la cuenta de estudiante.
   */
  async function handleGoogle(credentialResponse) {
    setError('')
    setLoading(true)
    try {
      const data = await authService.loginConGoogle(credentialResponse.credential)
      if (data && data.requiereRegistro) {
        setRegistro({
          idToken: credentialResponse.credential,
          correo: data.correo,
          nombre: data.nombre,
        })
        setNombre(data.nombre || '')
        return
      }
      entrar()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  /** Crea la cuenta de estudiante y deja la sesión iniciada. */
  async function handleRegistro(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await authService.registrarEstudianteConGoogle(registro.idToken, nombre, codigo, semestre)
      entrar()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  if (registro) {
    return (
      <div className="login-wrapper">
        <div className="login-card">
          <div className="login-header">
            <h1>📅 ScheduleU</h1>
            <p>Crea tu cuenta de estudiante</p>
          </div>
          <form onSubmit={handleRegistro} className="login-form">
            <p>
              No encontramos una cuenta para <strong>{registro.correo}</strong>.
              Completa tus datos para registrarte como estudiante.
            </p>
            <label>
              <span>Nombre completo</span>
              <input
                type="text"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                placeholder="ej: Juan Pérez"
                required
              />
            </label>
            <label>
              <span>Código académico</span>
              <input
                type="text"
                value={codigo}
                onChange={(e) => setCodigo(e.target.value)}
                placeholder="ej: 20211001"
                required
              />
            </label>
            <label>
              <span>Semestre actual</span>
              <input
                type="number"
                min="1"
                max="12"
                value={semestre}
                onChange={(e) => setSemestre(Number(e.target.value))}
                required
              />
            </label>
            {error && <div className="error-msg">{error}</div>}
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Creando cuenta...' : 'Crear mi cuenta'}
            </button>
            <button
              type="button"
              className="btn-secondary"
              disabled={loading}
              onClick={() => { setRegistro(null); setError('') }}
            >
              Volver
            </button>
          </form>
        </div>
      </div>
    )
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
