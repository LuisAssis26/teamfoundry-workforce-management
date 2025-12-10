import { useEffect, useMemo, useRef, useState } from "react";
import TeamManagementCard from "./components/TeamManagementCard.jsx";
import AdminNavbar from "../../../components/sections/AdminNavbar.jsx";
import { useAdminData } from "../AdminDataContext.jsx";

const STATUS_OPTIONS = [
  { value: "ALL", label: "Todos" },
  { value: "PENDING", label: "Pendente" },
  { value: "ACCEPTED", label: "Aceite" },
  { value: "COMPLETE", label: "Concluida" },
  { value: "INCOMPLETE", label: "Incompleta" },
  { value: "REJECTED", label: "Rejeitada" },
];

const DATE_ORDER_OPTIONS = [
  { value: "DESC", label: "Mais recentes" },
  { value: "ASC", label: "Mais antigos" },
];

const WORKFORCE_ORDER_OPTIONS = [
  { value: "DESC", label: "Maior MdO" },
  { value: "ASC", label: "Menor MdO" },
];

export default function TeamManagement() {
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [dateOrder, setDateOrder] = useState("DESC");
  const [workforceOrder, setWorkforceOrder] = useState("DESC");
  const [searchTerm, setSearchTerm] = useState("");

  const {
    requests: {
      assigned: {
        data: requests = [],
        loading: isLoading,
        error,
        refresh: refreshAssignedRequests,
      },
    },
  } = useAdminData();

  const initialLoad = useRef(false);
  useEffect(() => {
    if (initialLoad.current) return;
    initialLoad.current = true;
    refreshAssignedRequests().catch(() => {});
  }, [refreshAssignedRequests]);

  const filteredRequests = useMemo(() => {
    const list = [...requests];
    const byStatus =
        statusFilter === "ALL" ? list : list.filter((request) => request.state === statusFilter);

    const sorted = byStatus.sort((a, b) => {
      if (workforceOrder) {
        const diff =
            workforceOrder === "ASC"
                ? (a.workforceNeeded ?? 0) - (b.workforceNeeded ?? 0)
                : (b.workforceNeeded ?? 0) - (a.workforceNeeded ?? 0);
        if (diff !== 0) return diff;
      }
      const dateA = new Date(a.createdAt ?? 0).getTime();
      const dateB = new Date(b.createdAt ?? 0).getTime();
      return dateOrder === "ASC" ? dateA - dateB : dateB - dateA;
    });

    const term = searchTerm.trim().toLowerCase();
    const bySearch = term
        ? sorted.filter((request) => {
          const team = request.teamName?.toLowerCase() ?? "";
          const company = request.companyName?.toLowerCase() ?? "";
          return team.includes(term) || company.includes(term);
        })
        : sorted;

    return bySearch.map((request) => ({
      id: request.id,
      company: request.companyName ?? "N/A",
      email: request.companyEmail ?? "N/A",
      phone: request.companyPhone ?? "N/A",
      workforce:
          request.workforceNeeded > 0
              ? `${request.workforceNeeded} funcionarios`
              : "Sem requisicoes de funcionarios",
      status: request.state,
    }));
  }, [requests, statusFilter, dateOrder, workforceOrder, searchTerm]);

  const filterControls = [
    {
      id: "status",
      label: "Status",
      value: statusFilter,
      options: STATUS_OPTIONS,
      onChange: (e) => setStatusFilter(e.target.value),
    },
    {
      id: "date",
      label: "Data",
      value: dateOrder,
      options: DATE_ORDER_OPTIONS,
      onChange: (e) => setDateOrder(e.target.value),
    },
    {
      id: "workforce",
      label: "MdO",
      value: workforceOrder,
      options: WORKFORCE_ORDER_OPTIONS,
      onChange: (e) => setWorkforceOrder(e.target.value),
    },
  ];

  return (
      <div className="min-h-screen bg-base-200">
        <AdminNavbar />
        <main className="flex justify-center px-6 pb-16 pt-10">
          <div className="w-full max-w-6xl">
            <div className="flex flex-col gap-6 rounded-2xl bg-[#F0F0F0] p-8 shadow">
              <header className="flex flex-wrap items-center justify-center">
                <h1 className="text-3xl font-bold text-[#1F2959]">Equipas</h1>
              </header>

              <section className="flex flex-wrap gap-6 justify-center">

                {filterControls.map((control) => (
                    <div
                        key={control.id}
                        className="flex items-center justify-between gap-2 rounded-2xl  bg-[#F0F0F0] px-3 py-2 w-full max-w-[250px]"
                    >
                      <span className="text-lg font-medium text-[#2C3A74]">{control.label}:</span>
                      <select
                          value={control.value}
                          onChange={control.onChange}
                          className="select select-ghost bg-[#F0F0F0] text-sm text-[#1F2959]"
                      >
                        {control.options.map((option) => (
                            <option key={option.value} value={option.value}>
                              {option.label}
                            </option>
                        ))}
                      </select>
                    </div>
                ))}

                <div className="flex items-center gap-2 w-full md:w-auto">
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
              </section>

              {error && (
                  <div className="alert alert-error shadow">
                    <span>{error}</span>
                  </div>
              )}

              {isLoading ? (
                  <div className="py-10 text-center text-base-content/70">
                    Carregando requisicoes atribuidas...
                  </div>
              ) : filteredRequests.length === 0 ? (
                  <div className="alert alert-info shadow">
                    <span>Nenhuma requisicao encontrada com os filtros selecionados.</span>
                  </div>
              ) : (
                  <section className="flex flex-col gap-4">
                    {filteredRequests.map((team) => (
                        <TeamManagementCard key={team.id} {...team} />
                    ))}
                  </section>
              )}
            </div>
          </div>
        </main>
      </div>
  );
}
