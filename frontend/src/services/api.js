const API_URL = '/api'

function getToken() {
  return localStorage.getItem('scheduleu_token')
}

function authHeaders() {
  const token = getToken()
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  }
}

export async function login(correo, contrasena) {
  const res = await fetch(`${API_URL}/auth/login`, {
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

export function logout() {
  localStorage.removeItem('scheduleu_token')
  localStorage.removeItem('scheduleu_user')
}

export function getCurrentUser() {
  const raw = localStorage.getItem('scheduleu_user')
  return raw ? JSON.parse(raw) : null
}

async function request(method, path, body = null) {
  const options = { method, headers: authHeaders() }
  if (body) options.body = JSON.stringify(body)
  const res = await fetch(`${API_URL}${path}`, options)
  if (!res.ok) throw new Error(`Error HTTP ${res.status}`)
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

export const api = {
  get: (p) => request('GET', p),
  post: (p, b) => request('POST', p, b),
  put: (p, b) => request('PUT', p, b),
  delete: (p) => request('DELETE', p),
}
