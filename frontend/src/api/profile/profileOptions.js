import { httpGet } from "../config/http.js";

/**
 * Obtém funções, competências e áreas disponíveis para o passo 3.
 */
export function fetchProfileOptions() {
  return httpGet("/api/profile-options");
}
