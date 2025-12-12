import { apiFetch } from "../auth/client.js";

async function parseJson(resp) {
  try {
    return await resp.json();
  } catch {
    return null;
  }
}

async function handleResponse(resp, fallbackMessage) {
  if (resp.ok) {
    if (resp.status === 204) return null;
    return (await parseJson(resp)) ?? null;
  }

  const payload = await parseJson(resp);
  throw new Error(payload?.error || fallbackMessage);
}

export const teamRequestsAPI = {
  async getSuperAdminList() {
    const data = await handleResponse(
        await apiFetch("/api/super-admin/work-requests"),
        "Falha ao carregar requisições de trabalho."
    );
    return Array.isArray(data) ? data : [];
  },

  async getAdminOptions() {
    const data = await handleResponse(
        await apiFetch("/api/super-admin/work-requests/admin-options"),
        "Falha ao carregar administradores disponíveis."
    );
    return Array.isArray(data) ? data : [];
  },

  async assignToAdmin(requestId, adminId) {
    const data = await handleResponse(
        await apiFetch(`/api/super-admin/work-requests/${requestId}/responsible-admin`, {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ adminId }),
        }),
        "Falha ao atribuir administrador."
    );
    if (!data) throw new Error("Resposta inesperada do servidor.");
    return data;
  },

  async getAssignedToMe() {
    const data = await handleResponse(
        await apiFetch("/api/admin/work-requests"),
        "Falha ao carregar as requisições atribuídas."
    );
    return Array.isArray(data) ? data : [];
  },

  async getAssignedRequest(requestId) {
    const data = await handleResponse(
        await apiFetch(`/api/admin/work-requests/${requestId}`),
        "Falha ao carregar detalhes da requisição."
    );
    if (!data) throw new Error("Resposta inesperada do servidor.");
    return data;
  },

  async getRoleSummaries(requestId) {
    const data = await handleResponse(
        await apiFetch(`/api/admin/work-requests/${requestId}/roles`),
        "Falha ao carregar as funções requisitadas."
    );
    return Array.isArray(data) ? data : [];
  },
};
