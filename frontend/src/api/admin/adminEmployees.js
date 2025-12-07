import { apiFetch } from "../auth/client.js";

async function parseJson(resp) {
    try {
        return await resp.json();
    } catch {
        return null;
    }
}

async function handleResponse(resp, fallbackMessage) {
    if (resp.ok) return (await parseJson(resp)) ?? null;
    const payload = await parseJson(resp);
    throw new Error(payload?.error || fallbackMessage);
}

export async function fetchAdminEmployeeProfile(id) {
    const data = await handleResponse(
        await apiFetch(`/api/admin/employees/${id}/profile`),
        "Falha ao carregar perfil do funcionário."
    );
    return data;
}
