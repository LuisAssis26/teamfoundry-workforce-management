import { httpGet, httpPut, httpPost } from "../config/http.js";

/**
 * Busca o perfil da empresa autenticada.
 */
export const fetchCompanyProfile = () => httpGet("/api/company/profile");

/**
 * Atualiza dados do responsável da conta.
 * @param {{name: string, phone: string, position: string}} payload
 */
export const updateCompanyManager = (payload) =>
  httpPut("/api/company/profile", payload);

/**
 * Envia código de verificação para um novo email de responsável.
 */
export const sendCompanyManagerCode = (newEmail) =>
  httpPost("/api/company/verification/send", { newEmail });

/**
 * Confirma código e aplica novo email + dados do responsável.
 */
export const confirmCompanyManagerEmail = (payload) =>
  httpPost("/api/company/verification/confirm", payload);

/**
 * Desativa a conta da empresa (mantém dados, remove acesso).
 * @param {string} password
 */
export const deactivateCompanyAccount = (password) =>
  httpPost("/api/company/profile/deactivate", { password });
