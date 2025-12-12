import { httpGet, httpPost } from "../config/http.js";

/**
 * Lista requisições da empresa autenticada.
 */
export const fetchCompanyRequests = () => httpGet("/api/company/requests");

/**
 * Cria nova requisição de equipa.
 */
export const createCompanyRequest = (payload) =>
  httpPost("/api/company/requests", payload);
