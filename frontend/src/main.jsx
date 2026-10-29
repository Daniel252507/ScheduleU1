import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { GoogleOAuthProvider } from '@react-oauth/google'
import App from './App.jsx'
import './styles.css'

/** Client ID de Google (build-time). Sin este valor no se ofrece el login federado. */
const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID

const aplicacion = (
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
)

ReactDOM.createRoot(document.getElementById('root')).render(
  // El proveedor solo se monta cuando hay Client ID, de modo que la aplicación
  // sigue funcionando con correo y contraseña mientras Google no esté listo.
  googleClientId ? (
    <GoogleOAuthProvider clientId={googleClientId}>{aplicacion}</GoogleOAuthProvider>
  ) : (
    aplicacion
  ),
)
