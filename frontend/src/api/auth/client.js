import { API_URL } from '../config/config.js';
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from '../../auth/tokenStorage.js';

async function doFetch(path, options = {}) {
  if (!API_URL && !path.startsWith('http')) {
    throw new Error('VITE_API_BASE_URL (or VITE_API_URL) is not set.');
  }
  const url = path.startsWith('http') ? path : `${API_URL}${path}`;
  const headers = new Headers(options.headers || {});
  const isFormData = typeof FormData !== "undefined" && options.body instanceof FormData;
  if (!headers.has('Content-Type') && options.body && !isFormData) {
    headers.set('Content-Type', 'application/json');
  }

  const access = getAccessToken();
  if (access && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${access}`);
  }

  const resp = await fetch(url, { ...options, headers });
  return resp;
}

export async function apiFetch(path, options = {}, { autoRefresh = true } = {}) {
  let resp = await doFetch(path, options);
  if (resp.status !== 401) return resp;

  if (!autoRefresh) return resp;

  // Try refresh
  const refresh = getRefreshToken();
  if (!refresh) return resp;

  const r = await doFetch('/auth/refresh', {
    method: 'POST',
    body: JSON.stringify({ refreshToken: refresh })
  });

  if (!r.ok) {
    clearTokens();
    return resp; // original 401
  }

  const data = await r.json();
  // Backend usa 'acessToken' (com 1 'c'); suportar ambos por compatibilidade
  const accessToken = data.accessToken || data.acessToken;
  setTokens({ accessToken, refreshToken: data.refreshToken });

  // Retry original request with new token
  resp = await doFetch(path, options);
  return resp;
}

