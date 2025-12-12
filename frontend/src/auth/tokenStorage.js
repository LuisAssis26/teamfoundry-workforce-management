const ACCESS_KEY = 'accessToken';
const REFRESH_KEY = 'refreshToken';

export function getAccessToken() {
  return sessionStorage.getItem(ACCESS_KEY) ?? localStorage.getItem(ACCESS_KEY);
}

export function getRefreshToken() {
  return sessionStorage.getItem(REFRESH_KEY) ?? localStorage.getItem(REFRESH_KEY);
}

/**
 * Guarda tokens em sessionStorage (padrão) e opcionalmente em localStorage.
 * @param {{accessToken?:string, refreshToken?:string}} tokens
 * @param {{persist?: 'session' | 'local' | 'both'}} [options]
 */
export function setTokens({ accessToken, refreshToken }, { persist = "both" } = {}) {
  const storeInSession = persist === "session" || persist === "both";
  const storeInLocal = persist === "local" || persist === "both";

  if (accessToken) {
    if (storeInSession) sessionStorage.setItem(ACCESS_KEY, accessToken);
    if (storeInLocal) localStorage.setItem(ACCESS_KEY, accessToken);
  }
  if (refreshToken) {
    if (storeInSession) sessionStorage.setItem(REFRESH_KEY, refreshToken);
    if (storeInLocal) localStorage.setItem(REFRESH_KEY, refreshToken);
  }
  window.dispatchEvent(new Event("auth-change"));
}

export function clearTokens() {
  sessionStorage.removeItem(ACCESS_KEY);
  sessionStorage.removeItem(REFRESH_KEY);
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
  window.dispatchEvent(new Event("auth-change"));
}
