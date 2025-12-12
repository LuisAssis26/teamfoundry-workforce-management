import { apiFetch } from "../auth/client.js";

async function parseJson(resp) {
    try { return await resp.json(); } catch { return null; }
}

async function handleResponse(resp, fallback) {
    if (resp.ok) return (await parseJson(resp)) ?? null;
    const payload = await parseJson(resp);
    throw new Error(payload?.error || fallback);
}

export async function sendInvites(teamId, role, candidateIds) {
    const data = await handleResponse(
        await apiFetch(`/api/admin/work-requests/${teamId}/roles/${encodeURIComponent(role)}/invites`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ candidateIds }),
        }),
        "Falha ao enviar convites."
    );
    return data;
}

export async function listInvitedIds(teamId, role) {
    const data = await handleResponse(
        await apiFetch(`/api/admin/work-requests/${teamId}/roles/${encodeURIComponent(role)}/invites`),
        "Falha ao carregar convites."
    );
    return Array.isArray(data) ? data : [];
}

export async function listAcceptedIds(teamId) {
    const data = await handleResponse(
        await apiFetch(`/api/admin/work-requests/${teamId}/accepted`),
        "Falha ao carregar aceites."
    );
    return Array.isArray(data) ? data : [];
}
