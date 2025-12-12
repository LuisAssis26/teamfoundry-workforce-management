// Permite configurar a API via .env. Se não houver valor, usamos caminhos relativos (útil em dev com proxy Vite).
import { getAccessToken, setTokens, clearTokens } from "../../auth/tokenStorage.js";

const envBase = (import.meta.env.VITE_API_BASE_URL ?? "").trim().replace(/\/$/, "");
const API_BASE = envBase;

/**
 * Lê o corpo textual de uma response e tenta convertê-lo para JSON com segurança.
 * @param {Response} res
 * @returns {Promise<any|null>}
 */
async function parseBody(res) {
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

/**
 * Executa um fetch contra o backend garantindo base URL, headers e tratamento de erros.
 * @param {string} path
 * @param {RequestInit} [options]
 * @returns {Promise<any|null>}
 */
async function request(path, options = {}) {
  return performRequest(path, options, { alreadyRetried: false });
}

async function performRequest(path, options, { alreadyRetried }) {
  const url = path.startsWith("http")
    ? path
    : API_BASE
      ? `${API_BASE}${path}`
      : path;
  const headers = new Headers(options.headers || {});
  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (!headers.has("Authorization")) {
    const accessToken = getAccessToken();
    if (accessToken) {
      headers.set("Authorization", `Bearer ${accessToken}`);
    }
  }

  const response = await fetch(url, { ...options, headers, credentials: "include" });
  const data = await parseBody(response);

  if (response.ok) {
    return data;
  }

  if (response.status === 401 && !alreadyRetried) {
    const refreshed = await tryRefresh();
    if (refreshed) {
      return performRequest(path, options, { alreadyRetried: true });
    }
    clearTokens();
  }

  throw new Error(data?.error || `HTTP ${response.status}`);
}

async function tryRefresh() {
  try {
    const res = await fetch("/api/auth/refresh", {
      method: "POST",
      credentials: "include",
    });
    if (!res.ok) return false;
    const data = await parseBody(res);
    if (data?.accessToken) {
      setTokens({ accessToken: data.accessToken });
      window.dispatchEvent(new Event("auth-change"));
      return true;
    }
    return false;
  } catch {
    return false;
  }
}

export const httpGet = (path) => request(path);
export const httpPost = (path, body) =>
  request(path, { method: "POST", body: body ? JSON.stringify(body) : undefined });
export const httpPut = (path, body) =>
  request(path, { method: "PUT", body: body ? JSON.stringify(body) : undefined });
export const httpDelete = (path) => request(path, { method: "DELETE" });
