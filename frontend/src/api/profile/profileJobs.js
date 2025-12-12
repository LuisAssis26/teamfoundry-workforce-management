import { httpGet, httpPost } from "../config/http.js";

const BASE_PATH = "/api/employee/jobs";
const OFFERS_PATH = "/api/employee/offers";

// Devolve histórico de jobs do colaborador autenticado.
export const listEmployeeJobs = () => httpGet(BASE_PATH);

// Ofertas pendentes (sem funcionário associado).
export const listEmployeeOffers = () => httpGet(OFFERS_PATH);

// Aceita oferta (associa o colaborador ao request).
export const acceptEmployeeOffer = (offerId) => httpPost(`${OFFERS_PATH}/${offerId}/accept`);
