import { httpGet, httpPut } from "../config/http.js";

const PREFERENCES_ENDPOINT = "/api/employee/preferences";

export function fetchEmployeePreferences() {
  return httpGet(PREFERENCES_ENDPOINT);
}

export function updateEmployeePreferences(payload) {
  return httpPut(PREFERENCES_ENDPOINT, payload);
}
