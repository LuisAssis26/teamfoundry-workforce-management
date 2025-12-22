// API base URL must come from .env.
import { getAccessToken, setTokens, clearTokens } from "../../auth/tokenStorage.js";
import { API_URL } from "./config.js";

const API_BASE = API_URL;

/**
 * Reads a response body and safely parses JSON.
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
 * Executes a fetch against the backend with base URL, headers and error handling.
 * @param {string} path
 * @param {RequestInit} [options]
 * @returns {Promise<any|null>}
 */
async function request(path, options = {}) {
  return performRequest(path, options, { alreadyRetried: false });
}

async function performRequest(path, options, { alreadyRetried }) {
  if (!API_BASE && !path.startsWith("http")) {
    throw new Error("VITE_API_BASE_URL (or VITE_API_URL) is not set.");
  }
  const url = path.startsWith("http") ? path : `${API_BASE}${path}`;
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
    if (!API_BASE) return false;
    const res = await fetch(`${API_BASE}/api/auth/refresh`, {
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
