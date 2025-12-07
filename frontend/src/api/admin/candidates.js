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
        if (resp.status === 204) return [];
        return (await parseJson(resp)) ?? [];
    }
    const payload = await parseJson(resp);
    throw new Error(payload?.error || fallbackMessage);
}

export async function searchCandidates({ role, areas = [], skills = [] }) {
    const params = new URLSearchParams();
    if (role) params.append("role", role);
    areas.forEach((area) => params.append("areas", area));
    skills.forEach((skill) => params.append("skills", skill));

    const data = await handleResponse(
        await apiFetch(`/api/admin/candidates/search?${params.toString()}`),
        "Falha ao carregar candidatos."
    );
    return Array.isArray(data) ? data : [];
}
