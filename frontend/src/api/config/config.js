// Prefer relative URLs automatically in Vite dev, so we don't need .env.development.
// In production (or when explicitly set), honor VITE_API_URL; otherwise fallback.
const ENV = import.meta.env;
const RAW = ENV.VITE_API_URL;
export const API_URL = (RAW !== undefined)
  ? RAW
  : (ENV.DEV ? '' : 'http://localhost:8080');

