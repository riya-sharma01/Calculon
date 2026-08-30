const TOKEN_KEY = 'calculon_token';

// In local dev, Vite's proxy forwards '/api' to localhost:8080, so the relative path works.
// In production (e.g. Render), the frontend and backend are separate domains, so this must
// be set at build time to the backend's full URL — see .env.example.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

let inMemoryToken = null;

export function setToken(token) {
  inMemoryToken = token;
  if (token) {
    sessionStorage.setItem(TOKEN_KEY, token);
  } else {
    sessionStorage.removeItem(TOKEN_KEY);
  }
}

export function getToken() {
  if (inMemoryToken) return inMemoryToken;
  inMemoryToken = sessionStorage.getItem(TOKEN_KEY);
  return inMemoryToken;
}

class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.status = status;
    this.body = body;
  }
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (auth) {
    const token = getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    let payload = null;
    try { payload = await res.json(); } catch { /* no body */ }
    const message = payload?.message || `Request failed (${res.status})`;
    throw new ApiError(message, res.status, payload);
  }

  if (res.status === 204) return null;
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  get: (path, opts) => request(path, { ...opts, method: 'GET' }),
  post: (path, body, opts) => request(path, { ...opts, method: 'POST', body }),
};

export { ApiError };
