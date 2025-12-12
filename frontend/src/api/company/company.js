import { httpGet, httpPost } from "../config/http.js";

/**
 * Obtém opções utilizadas no registo de empresa (setores e países).
 */
export function fetchCompanyOptions() {
  return httpGet("/api/company/options");
}

/**
 * Submete o pedido de registo de empresa.
 */
export function registerCompany(payload) {
  return httpPost("/api/company/register", payload);
}
