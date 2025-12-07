import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import TeamRequestHeader from "./components/TeamRequestHeader.jsx";
import TeamRequestGrid from "./components/TeamRequestGrid.jsx";
import AdminNavbar from "../components/AdminNavbar.jsx";
import { useAdminData } from "../AdminDataContext.jsx";

export default function TeamEmployeeRequests() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const teamId = searchParams.get("team");

  const {
    requests: {
      details: { refresh: refreshDetails },
      roles: { refresh: refreshRoles },
    },
  } = useAdminData();

  const [teamName, setTeamName] = useState("Equipa");
  const [requests, setRequests] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let canceled = false;

    async function load() {
      if (!teamId) {
        setError("Selecione uma requisicao para montar a equipa.");
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError("");
      try {
        const [details, roles] = await Promise.all([
          refreshDetails(teamId),
          refreshRoles(teamId),
        ]);

        if (canceled) return;

        setTeamName(details?.teamName || "Equipa");
        const teamState = details?.state;

        const mapped = (roles || []).map((item) => {
          const total = item.totalPositions ?? item.totalRequested ?? item.requestedCount ?? 0;
          const filled = item.filledPositions ?? item.filled ?? 0;
          const open = item.openPositions ?? Math.max(total - filled, 0);

          return {
            id: `${teamId}-${item.role}`,
            role: item.role,
            workforceCurrent: filled,
            workforceTotal: total,
            proposalsSent: item.proposalsSent ?? 0,
            status: teamState === "COMPLETE" ? "COMPLETE" : open <= 0 ? "COMPLETE" : "IN_PROGRESS",
          };
        });

        setRequests(mapped);
      } catch (err) {
        if (!canceled) setError(err.message || "Erro ao carregar funcoes da equipa.");
      } finally {
        if (!canceled) setIsLoading(false);
      }
    }

    load();
    return () => {
      canceled = true;
    };
  }, [teamId, refreshDetails, refreshRoles]);

  const handleSelect = (request) => {
    if (!teamId) return;
    const params = new URLSearchParams({ team: teamId, role: request.role });
    navigate(`/admin/team-management/build?${params.toString()}`);
  };

  return (
      <div className="min-h-screen bg-[#F5F5F5]">
        <AdminNavbar />
        <div className="mx-auto flex max-w-screen-xl flex-col gap-8 px-8 py-10">
          <button
              type="button"
              className="inline-flex items-center gap-2 text-sm font-semibold text-[#1F2959] transition hover:text-[#0f1635]"
              onClick={() => navigate("/admin/team-management")}
          >
            <span aria-hidden="true">&larr;</span>
            Voltar
          </button>

          <TeamRequestHeader teamName={teamName} />

          {error && (
              <div className="alert alert-error shadow">
                <span>{error}</span>
              </div>
          )}

          {isLoading ? (
              <div className="rounded-2xl border border-[#E5E7EB] bg-white p-8 text-center text-base-content/70 shadow">
                Carregando funcoes requisitadas...
              </div>
          ) : requests.length === 0 ? (
              <div className="alert alert-info shadow">
                <span>Nao ha funcoes requisitadas para esta equipa.</span>
              </div>
          ) : (
              <TeamRequestGrid requests={requests} onSelect={handleSelect} />
          )}
        </div>
      </div>
  );
}
