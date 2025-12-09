import { httpGet, httpPut, httpPost, httpDelete } from "../config/http.js";

const PROFILE_ENDPOINT = "/api/employee/profile";

export async function fetchEmployeeProfile() {
  return httpGet(PROFILE_ENDPOINT);
}

export async function updateEmployeeProfile(payload) {
  return httpPut(PROFILE_ENDPOINT, payload);
}

export async function fetchEmployeeCurriculum() {
  return httpGet(`${PROFILE_ENDPOINT}/cv`);
}

export async function uploadEmployeeCurriculum(payload) {
  return httpPost(`${PROFILE_ENDPOINT}/cv`, payload);
}

export async function deleteEmployeeCurriculum() {
  return httpDelete(`${PROFILE_ENDPOINT}/cv`);
}

export async function uploadIdentificationDocument(payload) {
  return httpPost(`${PROFILE_ENDPOINT}/id-document`, payload);
}

export async function deleteIdentificationDocument(type) {
  return httpDelete(`${PROFILE_ENDPOINT}/id-document?type=${type}`);
}

export async function uploadEmployeeProfilePicture(payload) {
  return httpPost(`${PROFILE_ENDPOINT}/photo`, payload);
}

export async function deleteEmployeeProfilePicture() {
  return httpDelete(`${PROFILE_ENDPOINT}/photo`);
}

export async function fetchEmployeeProfileSummary() {
  return httpGet(`${PROFILE_ENDPOINT}/summary`);
}
