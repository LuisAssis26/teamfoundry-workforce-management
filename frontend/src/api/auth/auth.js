import { httpPost } from "../config/http.js";
import { setTokens, clearTokens } from "../../auth/tokenStorage.js";

/**
 * Envia credenciais ao backend e retorna o tipo do utilizador autenticado.
 * @param {string} email
 * @param {string} password
 * @returns {Promise<{userType:string,message:string}>}
 */
export async function login(email, password, rememberMe = false) {
  try {
    const data = await httpPost("/api/auth/login", { email, password, rememberMe });
    if (data?.accessToken) {
      setTokens({ accessToken: data.accessToken });
    }
    if (data?.userType) {
      localStorage.setItem("tf-user-type", data.userType);
    }
    return data;
  } catch (error) {
    clearTokens();
    localStorage.removeItem("tf-user-type");
    throw error;
  }
}

/**
 * Passo 1 do registo de funcionário: cria a conta pendente.
 * @param {{email:string,password:string}} payload
 * @returns {Promise<{message:string}>}
 */
export function registerStep1(payload) {
  return httpPost("/api/employee/register/step1", payload);
}

/**
 * Passo 2 do registo: dados pessoais + CV opcional.
 * @param {{email:string,firstName:string,lastName:string,phone:string,nif:number,birthDate:string,nationality:string,cvFile?:string,cvFileName?:string}} payload
 * @returns {Promise<{message:string}>}
 */
export function registerStep2(payload) {
  return httpPost("/api/employee/register/step2", payload);
}

/**
 * Passo 3 do registo: preferências de função/área/competências.
 * @param {{email:string,role:string,areas:string[],skills:string[],termsAccepted:boolean}} payload
 * @returns {Promise<{message:string}>}
 */
export function registerStep3(payload) {
  return httpPost("/api/employee/register/step3", payload);
}

/**
 * Passo 4 do registo: validação do código enviado ao candidato.
 * @param {{email:string,verificationCode:string}} payload
 * @returns {Promise<{message:string}>}
 */
export function registerStep4(payload) {
  return httpPost("/api/employee/register/step4", payload);
}

/**
 * Solicita o reenvio do código de verificação para o email indicado.
 * @param {string} email
 */
export function resendVerificationCode(email) {
  return httpPost("/api/employee/verification/send", { email });
}

export function forgotPassword(email) {
  return httpPost("/auth/forgot-password", { email });
}

export function resetPassword(email, code, newPassword) {
  return httpPost("/auth/reset-password", { email, code, newPassword });
}

export function verifyResetCode(email, code) {
  return httpPost("/auth/reset-password/verify", { email, code });
}
