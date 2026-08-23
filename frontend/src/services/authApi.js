const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')

let csrfToken = null
let csrfPromise = null

async function readJson(response) {
  if (response.status === 204) return null
  const text = await response.text()
  if (!text) return null
  try { return JSON.parse(text) } catch { return { message: 'The server returned an invalid response.' } }
}

async function ensureCsrf() {
  if (csrfToken) return csrfToken
  if (!csrfPromise) {
    csrfPromise = fetch(`${API_BASE_URL}/api/v1/auth/csrf`, { credentials: 'include' })
      .then(async (response) => {
        if (!response.ok) throw new Error('Could not initialize secure authentication.')
        const body = await response.json()
        csrfToken = body.token
        return csrfToken
      })
      .finally(() => { csrfPromise = null })
  }
  return csrfPromise
}

export async function apiRequest(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase()
  const headers = new Headers(options.headers)
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) headers.set('X-XSRF-TOKEN', await ensureCsrf())
  if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, method, headers, credentials: 'include' })
  const body = await readJson(response)
  if (!response.ok) {
    if (response.status === 401 && path !== '/api/v1/auth/login' && typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('lankastay:auth-error', { detail: { status: response.status, body } }))
    }
    if (response.status === 403 && body?.message === 'Initial password change is required.' && typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('lankastay:auth-error', { detail: { status: response.status, body } }))
    }
    const error = new Error(body?.message || 'The request could not be completed.')
    error.status = response.status
    error.body = body
    throw error
  }
  return body
}

export const authApi = {
  me: () => apiRequest('/api/v1/auth/me'),
  login: (credentials) => apiRequest('/api/v1/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
  changeInitialPassword: (data) => apiRequest('/api/v1/auth/change-initial-password', { method: 'POST', body: JSON.stringify(data) }),
  changePassword: (data) => apiRequest('/api/v1/auth/change-password', { method: 'POST', body: JSON.stringify(data) }),
  logout: async () => {
    try { return await apiRequest('/api/v1/auth/logout', { method: 'POST' }) }
    finally { csrfToken = null }
  },
  customerRegister: (data) => apiRequest('/api/v1/customer/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  customerLogin: (credentials) => apiRequest('/api/v1/customer/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
  customerMe: () => apiRequest('/api/v1/customer/auth/me'),
  customerLogout: async () => {
    try { return await apiRequest('/api/v1/customer/auth/logout', { method: 'POST' }) }
    finally { csrfToken = null }
  },
  forgotPassword: (data) => apiRequest('/api/v1/auth/forgot-password', { method: 'POST', body: JSON.stringify(data) }),
  resetPassword: (data) => apiRequest('/api/v1/auth/reset-password', { method: 'POST', body: JSON.stringify(data) }),
  getDevLastResetLink: (email) => apiRequest(`/api/v1/auth/dev-last-reset-link${email ? `?email=${encodeURIComponent(email)}` : ''}`),
}

