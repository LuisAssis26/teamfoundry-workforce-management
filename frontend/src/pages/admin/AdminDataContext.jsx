import { createContext, useCallback, useContext, useMemo, useState } from "react";
import PropTypes from "prop-types";
import { useAuthContext } from "../../auth/AuthContext.jsx";
import { teamRequestsAPI } from "../../api/admin/teamRequests.js";
import { fetchGeoAreas, fetchCompetences, fetchFunctions } from "../../api/site/siteManagement.js";

const AdminDataContext = createContext(null);

export function AdminDataProvider({ children }) {
    const { isAuthenticated } = useAuthContext();

    const [assignedRequests, setAssignedRequests] = useState([]);
    const [assignedRequestsLoading, setAssignedRequestsLoading] = useState(false);
    const [assignedRequestsLoaded, setAssignedRequestsLoaded] = useState(false);
    const [assignedRequestsError, setAssignedRequestsError] = useState(null);

    const [requestDetailsById, setRequestDetailsById] = useState({});
    const [requestDetailsLoading, setRequestDetailsLoading] = useState({});
    const [requestDetailsError, setRequestDetailsError] = useState(null);

    const [rolesByRequestId, setRolesByRequestId] = useState({});
    const [rolesLoading, setRolesLoading] = useState({});
    const [rolesError, setRolesError] = useState(null);

    const [options, setOptions] = useState({ geoAreas: [], competences: [], functions: [] });
    const [optionsLoading, setOptionsLoading] = useState(false);
    const [optionsLoaded, setOptionsLoaded] = useState(false);
    const [optionsError, setOptionsError] = useState(null);

    const refreshAssignedRequests = useCallback(
        async ({ force = false } = {}) => {
            if (!isAuthenticated) {
                setAssignedRequests([]);
                setAssignedRequestsLoaded(false);
                return [];
            }
            if (assignedRequestsLoading) return assignedRequests;
            if (assignedRequestsLoaded && !force) return assignedRequests;

            setAssignedRequestsLoading(true);
            setAssignedRequestsError(null);
            try {
                const data = await teamRequestsAPI.getAssignedToMe();
                setAssignedRequests(data);
                setAssignedRequestsLoaded(true);
                return data;
            } catch (error) {
                setAssignedRequestsError(error.message || "Nao foi possivel carregar as requisicoes.");
                throw error;
            } finally {
                setAssignedRequestsLoading(false);
            }
        },
        [isAuthenticated, assignedRequests, assignedRequestsLoaded, assignedRequestsLoading]
    );

    const refreshRequestDetails = useCallback(
        async (requestId, { force = false } = {}) => {
            if (!requestId || !isAuthenticated) return null;
            const cached = requestDetailsById[requestId];
            if (cached && !force) return cached;
            if (requestDetailsLoading[requestId]) return cached ?? null;

            setRequestDetailsLoading((prev) => ({ ...prev, [requestId]: true }));
            setRequestDetailsError(null);
            try {
                const data = await teamRequestsAPI.getAssignedRequest(requestId);
                setRequestDetailsById((prev) => ({ ...prev, [requestId]: data }));
                return data;
            } catch (error) {
                setRequestDetailsError(error.message || "Erro ao carregar detalhes da requisicao.");
                throw error;
            } finally {
                setRequestDetailsLoading((prev) => ({ ...prev, [requestId]: false }));
            }
        },
        [isAuthenticated, requestDetailsById, requestDetailsLoading]
    );

    const refreshRoleSummaries = useCallback(
        async (requestId, { force = false } = {}) => {
            if (!requestId || !isAuthenticated) return [];
            const cached = rolesByRequestId[requestId];
            if (cached && !force) return cached;
            if (rolesLoading[requestId]) return cached ?? [];

            setRolesLoading((prev) => ({ ...prev, [requestId]: true }));
            setRolesError(null);
            try {
                const data = await teamRequestsAPI.getRoleSummaries(requestId);
                setRolesByRequestId((prev) => ({ ...prev, [requestId]: data }));
                return data;
            } catch (error) {
                setRolesError(error.message || "Erro ao carregar funcoes da equipa.");
                throw error;
            } finally {
                setRolesLoading((prev) => ({ ...prev, [requestId]: false }));
            }
        },
        [isAuthenticated, rolesByRequestId, rolesLoading]
    );

    const refreshOptions = useCallback(
        async ({ force = false } = {}) => {
            if (optionsLoading) return options;
            if (optionsLoaded && !force) return options;

            setOptionsLoading(true);
            setOptionsError(null);
            try {
                const [geoAreas, competences, functions] = await Promise.all([
                    fetchGeoAreas(),
                    fetchCompetences(),
                    fetchFunctions(),
                ]);
                const payload = {
                    geoAreas: Array.isArray(geoAreas) ? geoAreas.map((item) => item.name).filter(Boolean) : [],
                    competences: Array.isArray(competences)
                        ? competences.map((item) => item.name).filter(Boolean)
                        : [],
                    functions: Array.isArray(functions) ? functions.map((item) => item.name).filter(Boolean) : [],
                };
                setOptions(payload);
                setOptionsLoaded(true);
                return payload;
            } catch (error) {
                setOptionsError(error.message || "Erro ao carregar opcoes.");
                throw error;
            } finally {
                setOptionsLoading(false);
            }
        },
        [options, optionsLoaded, optionsLoading]
    );

    const value = useMemo(
        () => ({
            requests: {
                assigned: {
                    data: assignedRequests,
                    loading: assignedRequestsLoading,
                    loaded: assignedRequestsLoaded,
                    error: assignedRequestsError,
                    refresh: refreshAssignedRequests,
                    setData: setAssignedRequests,
                },
                details: {
                    data: requestDetailsById,
                    loading: requestDetailsLoading,
                    error: requestDetailsError,
                    refresh: refreshRequestDetails,
                    setData: setRequestDetailsById,
                },
                roles: {
                    data: rolesByRequestId,
                    loading: rolesLoading,
                    error: rolesError,
                    refresh: refreshRoleSummaries,
                    setData: setRolesByRequestId,
                },
            },
            options: {
                data: options,
                loading: optionsLoading,
                loaded: optionsLoaded,
                error: optionsError,
                refresh: refreshOptions,
                setData: setOptions,
            },
        }),
        [
            assignedRequests,
            assignedRequestsError,
            assignedRequestsLoaded,
            assignedRequestsLoading,
            options,
            optionsError,
            optionsLoaded,
            optionsLoading,
            refreshAssignedRequests,
            refreshOptions,
            refreshRequestDetails,
            refreshRoleSummaries,
            requestDetailsById,
            requestDetailsError,
            requestDetailsLoading,
            rolesByRequestId,
            rolesError,
            rolesLoading,
        ]
    );

    return <AdminDataContext.Provider value={value}>{children}</AdminDataContext.Provider>;
}

AdminDataProvider.propTypes = {
    children: PropTypes.node,
};

export function useAdminData() {
    const ctx = useContext(AdminDataContext);
    if (!ctx) {
        throw new Error("useAdminData deve ser usado dentro de AdminDataProvider");
    }
    return ctx;
}
