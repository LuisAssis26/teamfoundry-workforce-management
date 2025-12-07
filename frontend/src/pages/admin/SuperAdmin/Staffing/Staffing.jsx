import { useEffect, useMemo, useRef, useState } from "react";
import WorkRequestCard from "./components/WorkRequestCard.jsx";
import AssignAdminModal from "./components/AssignAdminModal.jsx";
import { teamRequestsAPI } from "../../../../api/admin/teamRequests.js";
import { useSuperAdminData } from "../SuperAdminDataContext.jsx";

const STATUS_FILTERS = [
  { value: "ALL", label: "Todas" },
  { value: "INCOMPLETE", label: "Incompletas" },
  { value: "COMPLETE", label: "Concluídas" },
];

export default function GestaoTrabalho() {
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [responsibleFilter, setResponsibleFilter] = useState("ALL");
  const [responsibleQuery, setResponsibleQuery] = useState("");
  const [responsibleOpen, setResponsibleOpen] = useState(false);
  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [selectedRequestId, setSelectedRequestId] = useState(null);

  const {
    staffing: {
      requests: {
        data: workRequests = [],
        loading: isLoadingRequests,
        loaded: requestsLoaded,
        error: requestsError,
        refresh: refreshRequests,
        setData: setWorkRequests,
      },
      adminOptions: {
        data: adminOptions = [],
        loading: isLoadingAdmins,
        loaded: adminOptionsLoaded,
        error: adminError,
        refresh: refreshAdminOptions,
      },
    },
  } = useSuperAdminData();

  const [assignError, setAssignError] = useState(null);
  const [isAssigning, setIsAssigning] = useState(false);

  const initialRequestsLoad = useRef(false);
  useEffect(() => {
    if (requestsLoaded || initialRequestsLoad.current) return;
    initialRequestsLoad.current = true;
    refreshRequests().catch(() => {});
  }, [requestsLoaded, refreshRequests]);

  const initialAdminOptionsLoad = useRef(false);
  useEffect(() => {
    if (adminOptionsLoaded || initialAdminOptionsLoad.current) return;
    initialAdminOptionsLoad.current = true;
    refreshAdminOptions().catch(() => {});
  }, [adminOptionsLoaded, refreshAdminOptions]);

  const filteredResponsibleOptions = useMemo(() => {
    const term = responsibleQuery.trim().toLowerCase();
    const list = term
        ? adminOptions.filter((a) => a.name.toLowerCase().includes(term))
        : adminOptions;
    return list.slice(0, 5);
  }, [adminOptions, responsibleQuery]);

  const filteredRequests = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();
    return workRequests.filter((request) => {
      const team = request.teamName?.toLowerCase() ?? "";
      const company = request.companyName?.toLowerCase() ?? "";
      const matchesSearch = !term || team.includes(term) || company.includes(term);

      const matchesStatus =
          statusFilter === "ALL" ? true : (request.state || "").toUpperCase() === statusFilter;

      const matchesResponsible =
          responsibleFilter === "ALL"
              ? true
              : String(request.responsibleAdminId || "") === responsibleFilter;

      return matchesSearch && matchesStatus && matchesResponsible;
    });
  }, [workRequests, searchTerm, statusFilter, responsibleFilter]);

  const handleAssignAdmin = (requestId) => {
    setSelectedRequestId(requestId);
    setAssignError(null);
    setIsAssignModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsAssignModalOpen(false);
    setSelectedRequestId(null);
    setAssignError(null);
  };

  const handleAssignAdminConfirm = async (admin) => {
    if (!selectedRequestId || !admin) return;

    setAssignError(null);
    setIsAssigning(true);

    try {
      const updated = await teamRequestsAPI.assignToAdmin(selectedRequestId, admin.id);
      setWorkRequests((prev) => prev.map((request) => (request.id === updated.id ? updated : request)));
      handleCloseModal();
    } catch (err) {
      setAssignError(err.message || "Erro inesperado ao atribuir administrador.");
    } finally {
      setIsAssigning(false);
    }
  };

  const resolveAdminName = (adminId) => {
    if (!adminId) return null;
    const admin = adminOptions.find((a) => a.id === adminId);
    return admin ? admin.name : null;
  };

  const handleSelectResponsible = (adminId, adminName) => {
    setResponsibleFilter(String(adminId));
    setResponsibleQuery(adminName);
    setResponsibleOpen(false);
  };

  return (
      <section className="space-y-6">
        <header>
          <h1 className="text-3xl md:text-4xl font-extrabold text-primary">Gestao de Trabalho</h1>
          <p className="text-body text-base-content/70 mt-2">Configure fluxos de trabalho, cargos e equipes empresariais.</p>
        </header>

        {requestsError && (
            <div className="alert alert-error shadow">
              <span>{requestsError}</span>
            </div>
        )}
        {adminError && (
            <div className="alert alert-warning shadow">
              <span>{adminError}</span>
            </div>
        )}

        <section className="bg-base-100 border border-base-200 rounded-3xl shadow-xl p-8 space-y-6 md:p-10">
          <header className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <h2 className="text-3xl md:text-4xl font-extrabold text-primary">Requisicoes</h2>

            <div className="flex flex-col gap-3 md:flex-row md:items-center md:gap-4 w-full md:w-auto md:justify-end">

              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-base-content">Status:</span>
                <select
                    className="select select-bordered select-sm w-full md:w-40 text-sm"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                >
                  {STATUS_FILTERS.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                  ))}
                </select>
              </div>

              <div className="flex items-center gap-2 relative w-full md:w-60">
                <span className="text-sm font-medium text-base-content">Responsável:</span>
                <div className="relative flex-1">
                  <button
                      type="button"
                      className="input input-bordered input-sm w-full text-sm flex items-center justify-between"
                      onClick={() => setResponsibleOpen((prev) => !prev)}
                  >
                    <span className="truncate">{responsibleQuery || "Todos"}</span>
                    <i className="bi bi-chevron-down" aria-hidden="true" />
                  </button>
                  {responsibleOpen && (
                      <div className="absolute z-40 mt-1 w-full bg-base-100 border border-base-200 rounded-xl shadow max-h-60 overflow-auto text-sm">
                        <div className="p-2">
                          <input
                              type="text"
                              className="input input-bordered input-sm w-full text-sm"
                              placeholder="Pesquisar..."
                              value={responsibleQuery}
                              onChange={(e) => {
                                setResponsibleQuery(e.target.value);
                                setResponsibleFilter("ALL");
                              }}
                              autoFocus
                          />
                        </div>
                        <ul>
                          <li>
                            <button
                                type="button"
                                className="w-full text-left px-3 py-2 hover:bg-base-200"
                                onClick={() => {
                                  setResponsibleFilter("ALL");
                                  setResponsibleQuery("");
                                  setResponsibleOpen(false);
                                }}
                            >
                              Todos
                            </button>
                          </li>
                          {filteredResponsibleOptions.map((admin) => (
                              <li key={admin.id}>
                                <button
                                    type="button"
                                    className="w-full text-left px-3 py-2 hover:bg-base-200"
                                    onClick={() => handleSelectResponsible(admin.id, admin.name)}
                                >
                                  {admin.name}
                                </button>
                              </li>
                          ))}
                        </ul>
                      </div>
                  )}
                </div>
              </div>

              <div className="flex items-center gap-2">
                <label className="input input-bordered flex items-center gap-2 w-full md:w-64">
                  <input
                      type="search"
                      className="grow text-sm"
                      placeholder="Equipe ou empresa"
                      value={searchTerm}
                      onChange={(event) => setSearchTerm(event.target.value)}
                  />
                  <span className="btn btn-ghost btn-circle btn-sm pointer-events-none">
                  <i className="bi bi-search" aria-hidden="true" />
                </span>
                </label>
              </div>
            </div>
          </header>

          {isLoadingRequests ? (
              <div className="py-6 text-center text-base-content/70">Carregando requisições...</div>
          ) : filteredRequests.length === 0 ? (
              <div className="alert alert-info shadow-md">
                <span className="text-body">Nenhuma requisicao encontrada.</span>
              </div>
          ) : (
              <div className="space-y-4">
                {filteredRequests.map((request) => (
                    <WorkRequestCard
                        key={request.id}
                        company={{ name: request.companyName }}
                        teamName={request.teamName}
                        description={request.description}
                        state={request.state}
                        responsibleAdminName={resolveAdminName(request.responsibleAdminId)}
                        startDate={request.startDate}
                        endDate={request.endDate}
                        createdAt={request.createdAt}
                        onAssignAdmin={() => handleAssignAdmin(request.id)}
                    />
                ))}
              </div>
          )}
        </section>

        <AssignAdminModal
            open={isAssignModalOpen}
            onClose={handleCloseModal}
            onAssign={handleAssignAdminConfirm}
            adminList={adminOptions}
            isLoading={isLoadingAdmins}
            isBusy={isAssigning}
            errorMessage={assignError}
        />
      </section>
  );
}
