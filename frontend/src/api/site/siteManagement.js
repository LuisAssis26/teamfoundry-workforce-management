import { apiFetch } from "../auth/client.js";

const ADMIN_BASE = "/api/super-admin/site";
const FUNCTIONS_BASE = "/api/functions";

async function toJsonOrThrow(resp, defaultMessage) {
  if (!resp.ok) {
    const fallback = defaultMessage || "OperaÃ§Ã£o nÃ£o pÃ´de ser concluÃ­da.";
    const error = new Error(fallback);
    error.status = resp.status;
    throw error;
  }
  return resp.json();
}

function jsonOptions(method, body) {
  return {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  };
}

// --- Public ---
export async function fetchPublicHomepage() {
  const resp = await apiFetch("/api/site/homepage", {}, { autoRefresh: false });
  return toJsonOrThrow(resp, "Falha ao carregar a home page pÃºblica.");
}

// --- Homepage sections ---
export async function fetchHomepageConfig() {
  const resp = await apiFetch(`${ADMIN_BASE}/homepage`);
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel carregar as configuraÃ§Ãµes do site.");
}

export async function updateSection(sectionId, payload) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/homepage/sections/${sectionId}`,
    jsonOptions("PUT", payload)
  );
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel guardar a secÃ§Ã£o.");
}

export async function reorderSections(sectionIds) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/homepage/sections/reorder`,
    jsonOptions("PUT", { ids: sectionIds })
  );
  return toJsonOrThrow(resp, "OrdenaÃ§Ã£o das secÃ§Ãµes falhou.");
}

// --- Industries ---
export async function createIndustry(payload) {
  const resp = await apiFetch(`${ADMIN_BASE}/industries`, jsonOptions("POST", payload));
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel criar a indÃºstria.");
}

export async function updateIndustry(industryId, payload) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/industries/${industryId}`,
    jsonOptions("PUT", payload)
  );
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel atualizar a indÃºstria.");
}

export async function toggleIndustry(industryId, active) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/industries/${industryId}/visibility`,
    jsonOptions("PATCH", { active })
  );
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel alterar a visibilidade da indÃºstria.");
}

export async function reorderIndustries(ids) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/industries/reorder`,
    jsonOptions("PUT", { ids })
  );
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel reordenar indÃºstrias.");
}

export async function deleteIndustry(industryId) {
  const resp = await apiFetch(`${ADMIN_BASE}/industries/${industryId}`, {
    method: "DELETE",
  });
  if (!resp.ok) {
    const error = new Error("NÃ£o foi possÃ­vel eliminar a indÃºstria.");
    error.status = resp.status;
    throw error;
  }
}

// --- Partners ---
export async function createPartner(payload) {
  const resp = await apiFetch(`${ADMIN_BASE}/partners`, jsonOptions("POST", payload));
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel criar o parceiro.");
}

export async function updatePartner(partnerId, payload) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/partners/${partnerId}`,
    jsonOptions("PUT", payload)
  );
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel atualizar o parceiro.");
}

export async function togglePartner(partnerId, active) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/partners/${partnerId}/visibility`,
    jsonOptions("PATCH", { active })
  );
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel alterar a visibilidade do parceiro.");
}

export async function reorderPartners(ids) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/partners/reorder`,
    jsonOptions("PUT", { ids })
  );
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel reordenar parceiros.");
}

export async function deletePartner(partnerId) {
  const resp = await apiFetch(`${ADMIN_BASE}/partners/${partnerId}`, {
    method: "DELETE",
  });
  if (!resp.ok) {
    const error = new Error("NÃ£o foi possÃ­vel eliminar o parceiro.");
    error.status = resp.status;
    throw error;
  }
}

// --- Media ---
export async function uploadSiteImage(file) {
  const formData = new FormData();
  formData.append("file", file);
  const resp = await apiFetch(`${ADMIN_BASE}/media/upload`, {
    method: "POST",
    body: formData,
  });
  return toJsonOrThrow(resp, "NÃ£o foi possÃ­vel carregar a imagem.");
}

// --- Authenticated home ---
export async function fetchAppHomePublic() {
  const resp = await apiFetch("/api/site/app-home", {}, { autoRefresh: false });
  return toJsonOrThrow(resp, "Falha ao carregar a home autenticada.");
}

export async function fetchAppHomeConfig() {
  const resp = await apiFetch(`${ADMIN_BASE}/app-home`);
  return toJsonOrThrow(resp, "Não foi possível carregar a home autenticada.");
}

export async function updateAppHomeSection(sectionId, payload) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/app-home/sections/${sectionId}`,
    jsonOptions("PUT", payload)
  );
  return toJsonOrThrow(resp, "Não foi possível atualizar a secção.");
}

export async function reorderAppHomeSections(ids) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/app-home/sections/reorder`,
    jsonOptions("PUT", { ids })
  );
  return toJsonOrThrow(resp, "Não foi possível reordenar as secções.");
}

export async function createAppMetric(payload) {
  const resp = await apiFetch(`${ADMIN_BASE}/app-home/metrics`, jsonOptions("POST", payload));
  return toJsonOrThrow(resp, "Não foi possível criar a métrica.");
}

export async function updateAppMetric(metricId, payload) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/app-home/metrics/${metricId}`,
    jsonOptions("PUT", payload)
  );
  return toJsonOrThrow(resp, "Não foi possível atualizar a métrica.");
}

export async function toggleAppMetric(metricId, active) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/app-home/metrics/${metricId}/visibility`,
    jsonOptions("PATCH", { active })
  );
  return toJsonOrThrow(resp, "Não foi possível alterar a visibilidade da métrica.");
}

export async function reorderAppMetrics(ids) {
  const resp = await apiFetch(
    `${ADMIN_BASE}/app-home/metrics/reorder`,
    jsonOptions("PUT", { ids })
  );
  return toJsonOrThrow(resp, "Não foi possível reordenar as métricas.");
}

export async function deleteAppMetric(metricId) {
  const resp = await apiFetch(`${ADMIN_BASE}/app-home/metrics/${metricId}`, {
    method: "DELETE",
  });
  if (!resp.ok) {
    const error = new Error("Não foi possível eliminar a métrica.");
    error.status = resp.status;
    throw error;
  }
}

// --- Weekly tips ---
export async function fetchWeeklyTipsPage() {
    const resp = await apiFetch("/api/site/weekly-tips", {}, { autoRefresh: false });
    return toJsonOrThrow(resp, "Falha ao carregar as dicas.");
}

export async function fetchWeeklyTipsAdmin() {
    const resp = await apiFetch(`${ADMIN_BASE}/weekly-tips`);
    return toJsonOrThrow(resp, "N�o foi poss�vel carregar as dicas da semana.");
}

export async function createWeeklyTip(payload) {
    const resp = await apiFetch(`${ADMIN_BASE}/weekly-tips`, jsonOptions("POST", payload));
    return toJsonOrThrow(resp, "N�o foi poss�vel criar a dica.");
}

export async function updateWeeklyTip(id, payload) {
    const resp = await apiFetch(`${ADMIN_BASE}/weekly-tips/${id}`, jsonOptions("PUT", payload));
    return toJsonOrThrow(resp, "N�o foi poss�vel atualizar a dica.");
}

export async function toggleWeeklyTipVisibility(id, active) {
    const resp = await apiFetch(
        `${ADMIN_BASE}/weekly-tips/${id}/visibility`,
        jsonOptions("PATCH", { active })
    );
    return toJsonOrThrow(resp, "N�o foi poss�vel alterar a visibilidade da dica.");
}

export async function markWeeklyTipFeatured(id) {
    const resp = await apiFetch(`${ADMIN_BASE}/weekly-tips/${id}/featured`, {
        method: "PATCH",
    });
    return toJsonOrThrow(resp, "N�o foi poss�vel definir a dica da semana.");
}

export async function reorderWeeklyTips(ids) {
    const resp = await apiFetch(
        `${ADMIN_BASE}/weekly-tips/reorder`,
        jsonOptions("PUT", { ids })
    );
    return toJsonOrThrow(resp, "N�o foi poss�vel reordenar as dicas.");
}

export async function deleteWeeklyTip(id) {
    const resp = await apiFetch(`${ADMIN_BASE}/weekly-tips/${id}`, {
        method: "DELETE",
    });
    if (!resp.ok) {
        const error = new Error("N�o foi poss�vel eliminar a dica.");
        error.status = resp.status;
        throw error;
    }
}

// --- Funcoes ---
export async function fetchFunctions() {
  const resp = await apiFetch(FUNCTIONS_BASE);
  return toJsonOrThrow(resp, "Nao foi possivel carregar as funcoes.");
}

export async function createFunction(payload) {
  const resp = await apiFetch(FUNCTIONS_BASE, jsonOptions("POST", payload));
  return toJsonOrThrow(resp, "Nao foi possivel criar a funcao.");
}

export async function deleteFunction(functionId) {
  const resp = await apiFetch(`${FUNCTIONS_BASE}/${functionId}`, { method: "DELETE" });
  if (!resp.ok) {
    const error = new Error("Nao foi possivel eliminar a funcao.");
    error.status = resp.status;
    throw error;
  }
}

export async function fetchCompetences() {
  const resp = await apiFetch(`${FUNCTIONS_BASE}/competences`);
  return toJsonOrThrow(resp, "Nao foi possivel carregar competencias.");
}

export async function createCompetence(payload) {
  const resp = await apiFetch(`${FUNCTIONS_BASE}/competences`, jsonOptions("POST", payload));
  return toJsonOrThrow(resp, "Nao foi possivel criar a competencia.");
}

export async function deleteCompetence(id) {
  const resp = await apiFetch(`${FUNCTIONS_BASE}/competences/${id}`, { method: "DELETE" });
  if (!resp.ok) {
    const error = new Error("Nao foi possivel eliminar a competencia.");
    error.status = resp.status;
    throw error;
  }
}

export async function fetchGeoAreas() {
  const resp = await apiFetch(`${FUNCTIONS_BASE}/geo-areas`);
  return toJsonOrThrow(resp, "Nao foi possivel carregar areas geograficas.");
}

export async function createGeoArea(payload) {
  const resp = await apiFetch(`${FUNCTIONS_BASE}/geo-areas`, jsonOptions("POST", payload));
  return toJsonOrThrow(resp, "Nao foi possivel criar a area geografica.");
}

export async function deleteGeoArea(id) {
  const resp = await apiFetch(`${FUNCTIONS_BASE}/geo-areas/${id}`, { method: "DELETE" });
  if (!resp.ok) {
    const error = new Error("Nao foi possivel eliminar a area geografica.");
    error.status = resp.status;
    throw error;
  }
}

export async function fetchActivitySectors() {
  const resp = await apiFetch(`${FUNCTIONS_BASE}/activity-sectors`);
  return toJsonOrThrow(resp, "Nao foi possivel carregar setores de atividade.");
}

export async function createActivitySector(payload) {
  const resp = await apiFetch(`${FUNCTIONS_BASE}/activity-sectors`, jsonOptions("POST", payload));
  return toJsonOrThrow(resp, "Nao foi possivel criar o setor de atividade.");
}

export async function deleteActivitySector(id) {
  const resp = await apiFetch(`${FUNCTIONS_BASE}/activity-sectors/${id}`, { method: "DELETE" });
  if (!resp.ok) {
    const error = new Error("Nao foi possivel eliminar o setor de atividade.");
    error.status = resp.status;
    throw error;
  }
}
