import { useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { authService } from '../config/container.js'

/**
 * Vista publica para establecer la contrasena desde el enlace enviado por correo.
 * Lee el token de la URL (`?token=...`) y lo envia junto con la nueva contrasena.
 */
export default function EstablecerContrasena() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token') || ''

  const [contrasena, setContrasena] = useState('')
  const [confirmar, setConfirmar] = useState('')
  const [error, setError] = useState('')
  const [ok, setOk] = useState(false)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    if (contrasena.length < 6) {
      setError('La contrasena debe tener al menos 6 caracteres')
      return
    }
    if (contrasena !== confirmar) {
      setError('Las contrasenas no coinciden')
      return
    }

    setLoading(true)
    try {
      await authService.restablecerContrasena(token, contrasena)
      setOk(true)
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
          <p>Establece tu contrasena</p>
        </div>

        {!token && <div className="error-msg">El enlace no es valido o esta incompleto.</div>}

        {ok ? (
          <div className="login-form">
            <div className="success-msg">Tu contrasena se actualizo correctamente.</div>
            <button className="btn-primary" onClick={() => navigate('/')}>
              Iniciar sesion
            </button>
          </div>
        ) : (
          token && (
            <form onSubmit={handleSubmit} className="login-form">
              <label>
                <span>Nueva contrasena</span>
                <input
                  type="password"
                  value={contrasena}
                  onChange={(e) => setContrasena(e.target.value)}
                  placeholder="••••••••"
                  required
                />
              </label>
              <label>
                <span>Confirmar contrasena</span>
                <input
                  type="password"
                  value={confirmar}
                  onChange={(e) => setConfirmar(e.target.value)}
                  placeholder="••••••••"
                  required
                />
              </label>
              {error && <div className="error-msg">{error}</div>}
              <button type="submit" disabled={loading} className="btn-primary">
                {loading ? 'Guardando...' : 'Guardar contrasena'}
              </button>
            </form>
          )
        )}
      </div>
    </div>
  )
}
