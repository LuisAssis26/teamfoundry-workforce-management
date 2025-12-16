import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { searchCandidates } from "../../../../api/admin/candidates.js";
import { sendInvites, listInvitedIds, listAcceptedIds } from "../../../../api/admin/invitations.js";
import { useAdminData } from "../AdminDataContext.jsx";
import BackButton from "../../../../components/ui/Button/BackButton.jsx";
import HeroHeader from "./components/HeroHeader.jsx";
import FiltersPanel from "./components/FiltersPanel.jsx";
import CandidatesPanel from "./components/CandidatesPanel.jsx";

export default function BuildTeamSearch() {
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
    const statusOptions = [
        { value: "NO_PROPOSAL", label: "Sem proposta" },
        { value: "INVITED", label: "Enviado" },
        { value: "ACCEPTED", label: "Aceite" },
    ];

    const [geoSelected, setGeoSelected] = useState([]);
    const [skillsSelected, setSkillsSelected] = useState([]);
    const [preferredRolesSelected, setPreferredRolesSelected] = useState([]);
    const [statusSelected, setStatusSelected] = useState([]);

    const [teamInfo, setTeamInfo] = useState(null);
    const [isLoadingTeam, setIsLoadingTeam] = useState(true);
    const [teamError, setTeamError] = useState("");

    const [candidates, setCandidates] = useState([]);
    const [isSearching, setIsSearching] = useState(false);
    const [searchError, setSearchError] = useState("");

    const [selectedIds, setSelectedIds] = useState([]);
    const [invitedIds, setInvitedIds] = useState([]);
    const [acceptedIds, setAcceptedIds] = useState([]);
    const [inviteFeedback, setInviteFeedback] = useState("");
    const [inviteError, setInviteError] = useState("");
    const [isInviting, setIsInviting] = useState(false);
    const [page, setPage] = useState(1);
    const PAGE_SIZE = 6;
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
                    statuses: statusSelected,
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
    }, [teamId, role, geoSelected, skillsSelected, preferredRolesSelected, statusSelected]);

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
                    const acceptedSafe = accepted ?? [];
                    const invitedSafe = (invited ?? []).filter((id) => !acceptedSafe.includes(id));
                    setInvitedIds(invitedSafe);
                    setSelectedIds([]); // não marcar como selecionado automaticamente
                    setAcceptedIds(acceptedSafe);
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
        const rawBase = import.meta.env.VITE_CLOUDINARY_BASE_URL || "";
        const cloudBase = rawBase && !rawBase.endsWith("/") ? `${rawBase}/` : rawBase;
        return candidates.map((c) => {
            const fullName = [c.firstName, c.lastName].filter(Boolean).join(" ").trim() || "Sem nome";
            const preferredArea = c.areas?.[0] || "-";
            const skills = c.skills?.length ? c.skills : [];
            const experiences = Array.isArray(c.experiences) ? c.experiences : [];
            const profileId = c.profilePicturePublicId;
            const photoUrl = profileId
                ? `${cloudBase}${profileId}`
                : c.photoUrl || c.photo || c.imageUrl || null;
            const accepted = acceptedIds.includes(c.id);
            const selected = selectedIds.includes(c.id);
            const invited = invitedIds.includes(c.id); // já possui convite enviado
            return {
                id: c.id,
                name: fullName,
                role: c.role || "Sem funcao",
                city: preferredArea,
                skills,
                experiences,
                photoUrl,
                accepted,
                selected,
                invited,
            };
        });
    }, [candidates, selectedIds, invitedIds, acceptedIds]);

    const filteredByStatus = useMemo(() => {
        if (!statusSelected || statusSelected.length === 0) return mappedCandidates;
        const selectedSet = new Set(statusSelected);
        return mappedCandidates.filter((candidate) => {
            const status = candidate.accepted
                ? "ACCEPTED"
                : candidate.invited
                    ? "INVITED"
                    : "NO_PROPOSAL";
            return selectedSet.has(status);
        });
    }, [mappedCandidates, statusSelected]);

    const totalPages = Math.max(1, Math.ceil(filteredByStatus.length / PAGE_SIZE));

    useEffect(() => {
        setPage(1);
    }, [geoSelected, skillsSelected, preferredRolesSelected, statusSelected, role, filteredByStatus.length]);

    useEffect(() => {
        if (page > totalPages) setPage(totalPages);
    }, [page, totalPages]);

    useEffect(() => {
        // se algum candidato passar para aceito, remove de convidados para não marcar como "Enviado"
        setInvitedIds((prev) => prev.filter((id) => !acceptedIds.includes(id)));
    }, [acceptedIds]);

    const paginatedCandidates = useMemo(() => {
        const start = (page - 1) * PAGE_SIZE;
        return filteredByStatus.slice(start, start + PAGE_SIZE);
    }, [filteredByStatus, page]);

    const toggleSelect = (id, accepted, invited) => {
        if (accepted || invited || isComplete) return;
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
            setInvitedIds((prev) => [...new Set([...prev, ...selectedIds])]);
            setSelectedIds([]);
        } catch (err) {
            setInviteError(err.message || "Erro ao enviar convites.");
        } finally {
            setIsInviting(false);
        }
    };

    return (
        <div className="min-h-screen bg-base-200">
            <main className="flex justify-center px-4 pb-16 pt-8 sm:px-6 lg:px-8">
                <div className="w-full max-w-6xl space-y-6">
                    <BackButton to={`/admin/team-management/requests?team=${teamId ?? ""}`} />

                    {teamError ? (
                        <div className="alert alert-error shadow">
                            <span>{teamError}</span>
                        </div>
                    ) : isLoadingTeam ? (
                        <div className="rounded-2xl border border-base-200 bg-base-100 p-8 text-center text-base-content/70 shadow">
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
                                statusOptions={statusOptions}
                                statusSelected={statusSelected}
                                onGeoChange={setGeoSelected}
                                onSkillsChange={setSkillsSelected}
                                onPreferredRolesChange={setPreferredRolesSelected}
                                onStatusChange={setStatusSelected}
                            />
                                <CandidatesPanel
                                    employees={paginatedCandidates}
                                    isLoading={isSearching}
                                    selectedIds={selectedIds}
                                    onToggle={toggleSelect}
                                    onSendInvites={handleSendInvites}
                                    isInviting={isInviting}
                                    disabled={isComplete}
                                    page={page}
                                    totalPages={totalPages}
                                    onPageChange={setPage}
                                />
                            </div>
                        </>
                    )}
                </div>
            </main>
        </div>
    );
}
