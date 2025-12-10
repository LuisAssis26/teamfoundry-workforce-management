import { useEffect, useMemo, useRef, useState } from "react";
import WorkRequestCard from "./components/WorkRequestCard.jsx";
import AssignAdminModal from "./components/AssignAdminModal.jsx";
import { teamRequestsAPI } from "../../../../api/admin/teamRequests.js";
import { useSuperAdminData } from "../SuperAdminDataContext.jsx";
import { DropdownChevron, DropdownPanel, DROPDOWN_TRIGGER_CLASS } from "../../../../components/ui/Dropdown/Dropdown.jsx";
import SearchBar from "../../../../components/ui/Input/SearchBar.jsx";

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
  const [statusOpen, setStatusOpen] = useState(false);
  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [selectedRequestId, setSelectedRequestId] = useState(null);
  const statusRef = useRef(null);
  const responsibleRef = useRef(null);

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
    const list = term ? adminOptions.filter((a) => a.name.toLowerCase().includes(term)) : adminOptions;
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
      // Garantir que os contadores de requisições por admin reflitam a atribuição mais recente
      refreshRequests({ force: true }).catch(() => {});
      refreshAdminOptions({ force: true }).catch(() => {});
      handleCloseModal();
    } catch (err) {
      setAssignError(err.message || "Erro inesperado ao atribuir administrador.");
    } finally {
      setIsAssigning(false);
    }
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (statusRef.current && !statusRef.current.contains(event.target)) {
        setStatusOpen(false);
      }
      if (responsibleRef.current && !responsibleRef.current.contains(event.target)) {
        setResponsibleOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

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

            <div className="flex w-full flex-col gap-3 md:w-auto md:flex-row md:flex-wrap md:items-end">
              <div className="form-control w-full md:w-64 shrink-0" ref={statusRef}>
                <label className="label">
                  <span className="label-text font-medium text-base-content">Status</span>
                </label>
                <div className="relative">
                  <button
                      type="button"
                      className={`${DROPDOWN_TRIGGER_CLASS} h-10 text-sm`}
                      onClick={() => {
                        setStatusOpen((prev) => !prev);
                        setResponsibleOpen(false);
                      }}
                  >
                    <span className="truncate">
                      {STATUS_FILTERS.find((opt) => opt.value === statusFilter)?.label || "Todas"}
                    </span>
                    <DropdownChevron open={statusOpen} />
                  </button>
                  <DropdownPanel
                      open={statusOpen}
                      options={STATUS_FILTERS}
                      getOptionLabel={(opt) => opt.label}
                      getOptionValue={(opt) => opt.value}
                      onSelect={(val) => {
                        setStatusFilter(val);
                        setStatusOpen(false);
                      }}
                      className="w-full"
                  />
                </div>
              </div>

              <div className="form-control w-full md:w-64 shrink-0" ref={responsibleRef}>
                <label className="label">
                  <span className="label-text font-medium text-base-content">Responsável</span>
                </label>
                <div className="relative">
                  <button
                      type="button"
                      className={`${DROPDOWN_TRIGGER_CLASS} h-10 text-sm`}
                      onClick={() => {
                        setResponsibleOpen((prev) => !prev);
                        setStatusOpen(false);
                      }}
                  >
                    <span className="truncate">{responsibleQuery || "Todos"}</span>
                    <DropdownChevron open={responsibleOpen} />
                  </button>
                  <DropdownPanel
                      open={responsibleOpen}
                      options={[{ id: "ALL", name: "Todos" }, ...filteredResponsibleOptions]}
                      getOptionLabel={(opt) => opt.name}
                      getOptionValue={(opt) => opt.id}
                      onSelect={(val) => {
                        if (val === "ALL") {
                          setResponsibleFilter("ALL");
                          setResponsibleQuery("");
                        } else {
                          const admin = adminOptions.find((a) => String(a.id) === String(val));
                          handleSelectResponsible(val, admin?.name || "");
                        }
                        setResponsibleOpen(false);
                      }}
                      showSearch
                      searchQuery={responsibleQuery}
                      onSearchChange={(value) => {
                        setResponsibleQuery(value);
                        setResponsibleFilter("ALL");
                      }}
                      className="w-full"
                  />
                </div>
              </div>

              <div className="form-control w-full md:w-64 shrink-0">
                <SearchBar
                    value={searchTerm}
                    onChange={(event) => setSearchTerm(event.target.value)}
                    placeholder="Equipe ou empresa"
                    className="w-full"
                    size="md"
                />
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
