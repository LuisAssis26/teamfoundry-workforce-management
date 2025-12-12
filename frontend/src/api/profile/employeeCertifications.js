import { httpDelete, httpGet, httpPost, httpPut } from "../config/http.js";

const BASE_PATH = "/api/employee/certifications";

export const listEmployeeCertifications = () => httpGet(BASE_PATH);

export const createEmployeeCertification = (payload) => httpPost(BASE_PATH, payload);

export const updateEmployeeCertification = (id, payload) => httpPut(`${BASE_PATH}/${id}`, payload);

export const deleteEmployeeCertification = (id) => httpDelete(`${BASE_PATH}/${id}`);
