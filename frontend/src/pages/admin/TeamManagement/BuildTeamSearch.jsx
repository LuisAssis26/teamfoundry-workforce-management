import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import AdminNavbar from "../../../components/sections/AdminNavbar.jsx";
import EmployeeCard from "./components/EmployeeCard.jsx";
import MultiSelectDropdown from "../../../components/ui/Dropdown/MultiSelectDropdown.jsx";
import { searchCandidates } from "../../../api/admin/candidates.js";
import { sendInvites, listInvitedIds, listAcceptedIds } from "../../../api/admin/invitations.js";
import { useAdminData } from "./AdminDataContext.jsx";

export default function BuildTeamSearch() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const teamId = searchParams.get("team");
    const role = searchParams.get("role") || "";

    const {
        requests: {
            details: { refresh: refreshDetails },
        },
        options: {
            data: optionsData,
            error: optionsError,
            refresh: refreshOptions,
        },
    } = useAdminData();

    const geoOptions = optionsData.geoAreas ?? [];
    const skillOptions = optionsData.competences ?? [];
    const functionOptions = optionsData.functions ?? [];

    const [geoSelected, setGeoSelected] = useState([]);
    const [skillsSelected, setSkillsSelected] = useState([]);
    const [preferredRolesSelected, setPreferredRolesSelected] = useState([]);

    const [teamInfo, setTeamInfo] = useState(null);
    const [isLoadingTeam, setIsLoadingTeam] = useState(true);
    const [teamError, setTeamError] = useState("");

    const [candidates, setCandidates] = useState([]);
    const [isSearching, setIsSearching] = useState(false);
    const [searchError, setSearchError] = useState("");

    const [selectedIds, setSelectedIds] = useState([]);
    const [acceptedIds, setAcceptedIds] = useState([]);
    const [inviteFeedback, setInviteFeedback] = useState("");
    const [inviteError, setInviteError] = useState("");
    const [isInviting, setIsInviting] = useState(false);
    const isComplete = teamInfo?.state === "COMPLETE";

    useEffect(() => {
        let canceled = false;

        async function loadTeam() {
            if (!teamId) {
                setTeamError("Selecione uma requisicao antes de montar a equipa.");
                setIsLoadingTeam(false);
                return;
            }

            setIsLoadingTeam(true);
            setTeamError("");
            try {
                const details = await refreshDetails(teamId);
                if (!canceled) setTeamInfo(details);
            } catch (err) {
                if (!canceled) setTeamError(err.message || "Erro ao carregar dados da equipa.");
            } finally {
                if (!canceled) setIsLoadingTeam(false);
            }
        }

        loadTeam();
        return () => {
            canceled = true;
        };
    }, [teamId, refreshDetails]);

    useEffect(() => {
        refreshOptions().catch(() => {});
    }, [refreshOptions]);

    useEffect(() => {
        let canceled = false;

        async function runSearch() {
            if (!teamId) return;
            setIsSearching(true);
            setSearchError("");
            try {
                const data = await searchCandidates({
                    role,
                    areas: geoSelected,
                    skills: skillsSelected,
                    preferredRoles: preferredRolesSelected,
                });
                if (!canceled) setCandidates(data);
            } catch (err) {
                if (!canceled) setSearchError(err.message || "Erro ao carregar candidatos.");
            } finally {
                if (!canceled) setIsSearching(false);
            }
        }

        runSearch();
        return () => {
            canceled = true;
        };
    }, [teamId, role, geoSelected, skillsSelected, preferredRolesSelected]);

    useEffect(() => {
        let canceled = false;

        async function loadInvitedAccepted() {
            if (!teamId || !role) return;
            try {
                const [invited, accepted] = await Promise.all([
                    listInvitedIds(teamId, role),
                    listAcceptedIds(teamId),
                ]);
                if (!canceled) {
                    setSelectedIds(invited);
                    setAcceptedIds(accepted);
                }
            } catch {
                /* silencioso */
            }
        }

        loadInvitedAccepted();
        return () => {
            canceled = true;
        };
    }, [teamId, role]);

    const mappedCandidates = useMemo(() => {
        return candidates.map((c) => {
            const fullName = [c.firstName, c.lastName].filter(Boolean).join(" ").trim() || "Sem nome";
            const preferredArea = c.areas?.[0] || "-";
            const skills = c.skills?.length ? c.skills : [];
            const experiences = Array.isArray(c.experiences) ? c.experiences : [];
            const accepted = acceptedIds.includes(c.id);
            const selected = selectedIds.includes(c.id);
            return {
                id: c.id,
                name: fullName,
                role: c.role || "Sem funcao",
                city: preferredArea,
                skills,
                experiences,
                accepted,
                selected,
            };
        });
    }, [candidates, selectedIds, acceptedIds]);

    const toggleSelect = (id, accepted) => {
        if (accepted || isComplete) return;
        setSelectedIds((prev) => (prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]));
    };

    const handleSendInvites = async () => {
        if (!teamId || !role || selectedIds.length === 0) return;
        setInviteError("");
        setInviteFeedback("");
        setIsInviting(true);
        try {
            const resp = await sendInvites(teamId, role, selectedIds);
            const created = resp?.invitesCreated ?? selectedIds.length;
            setInviteFeedback(`Convites enviados: ${created}.`);
        } catch (err) {
            setInviteError(err.message || "Erro ao enviar convites.");
        } finally {
            setIsInviting(false);
        }
    };

    return (
        <div className="min-h-screen bg-base-200">
            <AdminNavbar />
            <main className="flex justify-center px-4 pb-16 pt-8 sm:px-6 lg:px-8">
                <div className="w-full max-w-6xl space-y-6 rounded-2xl bg-[#F5F5F5] p-6 text-[#1F2959] shadow">
                    <button
                        type="button"
                        className="inline-flex items-center gap-2 text-sm font-semibold text-[#1F2959] transition hover:text-[#0f1635]"
                        onClick={() => navigate(`/admin/team-management/requests?team=${teamId ?? ""}`)}
                    >
                        <span aria-hidden="true">&larr;</span>
                        Voltar
                    </button>

                    {teamError ? (
                        <div className="alert alert-error shadow">
                            <span>{teamError}</span>
                        </div>
                    ) : isLoadingTeam ? (
                        <div className="rounded-2xl border border-[#E5E7EB] bg-white p-8 text-center text-base-content/70 shadow">
                            Carregando dados da equipa...
                        </div>
                    ) : (
                        <>
                            <HeroHeader teamName={teamInfo?.teamName || "Equipa"} role={role} />

                            {(optionsError || searchError) && (
                                <div className="alert alert-warning shadow">
                                    <span>{optionsError || searchError}</span>
                                </div>
                            )}
                            {inviteFeedback && (
                                <div className="alert alert-success shadow">
                                    <span>{inviteFeedback}</span>
                                </div>
                            )}
                            {inviteError && (
                                <div className="alert alert-error shadow">
                                    <span>{inviteError}</span>
                                </div>
                            )}

                            <div className="flex flex-col gap-6 lg:flex-row">
                                <FiltersPanel
                                    companyName={teamInfo?.companyName || "Empresa"}
                                    role={role || "Funcao"}
                                    geoOptions={geoOptions}
                                    geoSelected={geoSelected}
                                    skillOptions={skillOptions}
                                    skillsSelected={skillsSelected}
                                    functionOptions={functionOptions}
                                    preferredRolesSelected={preferredRolesSelected}
                                    onGeoChange={setGeoSelected}
                                    onSkillsChange={setSkillsSelected}
                                    onPreferredRolesChange={setPreferredRolesSelected}
                                />
                                <CandidatesPanel
                                    employees={mappedCandidates}
                                    isLoading={isSearching}
                                    selectedIds={selectedIds}
                                    onToggle={toggleSelect}
                                    onSendInvites={handleSendInvites}
                                    isInviting={isInviting}
                                    disabled={isComplete}
                                />
                            </div>
                        </>
                    )}
                </div>
            </main>
        </div>
    );
}

function HeroHeader({ teamName, role }) {
    return (
        <header className="space-y-1">
            <p className="text-sm text-[#1F2959]/80">Selecionar funcionarios</p>
            <h1 className="text-3xl font-bold leading-tight text-[#1F2959]">
                {teamName} {role ? `- ${role}` : ""}
            </h1>
        </header>
    );
}

function FiltersPanel({
                          companyName,
                          role,
                                  geoOptions,
                                  geoSelected,
                                  skillOptions,
                                  skillsSelected,
                                  functionOptions,
                                  preferredRolesSelected,
                                  onGeoChange,
                                  onSkillsChange,
                                  onPreferredRolesChange,
                              }) {
    return (
        <aside className="w-full rounded-2xl bg-white p-6 shadow-md lg:w-80">
            <div className="space-y-4">
                <FilterTitle label="Empresa" value={companyName} />
                <FilterTitle label="Funcao" value={role} />
            </div>

            <div className="mt-6 space-y-6">
                <MultiSelectDropdown
                    label="Area(s) Geografica(s)"
                    options={geoOptions}
                    selectedOptions={geoSelected}
                    onChange={onGeoChange}
                    placeholder="Selecione area(s)"
                />
                <MultiSelectDropdown
                    label="Competencias"
                    options={skillOptions}
                    selectedOptions={skillsSelected}
                    onChange={onSkillsChange}
                    placeholder="Selecione competencias"
                />
                <MultiSelectDropdown
                    label="Funcao preferencial"
                    options={functionOptions}
                    selectedOptions={preferredRolesSelected}
                    onChange={onPreferredRolesChange}
                    placeholder="Selecione funcao(oes)"
                />
            </div>
        </aside>
    );
}

function FilterTitle({ label, value }) {
    return (
        <div className="rounded-xl bg-[#F0F0F0] px-4 py-2">
            <p className="text-lg font-semibold">
                {label}: <span className="text-[#111827]">{value}</span>
            </p>
        </div>
    );
}

function CandidatesPanel({ employees, isLoading, selectedIds, onToggle, onSendInvites, isInviting, disabled }) {
    return (
        <section className="flex-1 rounded-2xl border border-[#111827]/20 bg-white p-4 shadow-inner space-y-4">
            <div className="flex items-center justify-between">
                <span className="text-sm text-base-content/70">Selecionados: {selectedIds.length}</span>
                <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    disabled={disabled || isLoading || isInviting || selectedIds.length === 0}
                    onClick={onSendInvites}
                >
                    {isInviting ? "Enviando..." : disabled ? "Concluida" : "Enviar convites"}
                </button>
            </div>

            {isLoading ? (
                <div className="py-8 text-center text-base-content/70">Carregando candidatos...</div>
            ) : employees.length === 0 ? (
                <div className="py-8 text-center text-base-content/70">Nenhum candidato encontrado.</div>
            ) : (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                    {employees.map((employee) => (
                        <EmployeeCard
                            key={employee.id}
                            {...employee}
                            onSelect={() => onToggle(employee.id, employee.accepted)}
                        />
                    ))}
                </div>
            )}
        </section>
    );
}
