// Read API base URL from .env only.
const ENV = import.meta.env;
const RAW = (ENV.VITE_API_BASE_URL ?? ENV.VITE_API_URL ?? "").trim();
export const API_URL = RAW.replace(/\/$/, "");

