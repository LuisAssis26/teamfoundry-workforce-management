import { createContext, useCallback, useContext, useMemo, useState } from "react";
import PropTypes from "prop-types";
import { apiFetch } from "../../../api/auth/client.js";
import { teamRequestsAPI } from "../../../api/admin/teamRequests.js";
import {
  fetchActivitySectors,
  fetchAppHomeConfig,
  fetchCompetences,
  fetchFunctions,
  fetchGeoAreas,
  fetchHomepageConfig,
  fetchUnifiedHome,
  fetchWeeklyTipsAdmin,
} from "../../../api/site/siteManagement.js";
import {
  normalizeAppHomeConfig,
  normalizeHomepageConfig,
  sortByName,
  sortWeeklyTips,
} from "./VariableManagement/utils.js";

const SuperAdminDataContext = createContext(null);

const normalizeAdminRole = (role) => {
  if (!role) return "admin";
  const lower = String(role).toLowerCase();
  return lower === "superadmin" ? "super-admin" : lower;
};

export function SuperAdminDataProvider({ children }) {
  // Credentials - companies awaiting approval
  const [companyCredentials, setCompanyCredentials] = useState([]);
  const [companyCredentialsLoaded, setCompanyCredentialsLoaded] = useState(false);
  const [companyCredentialsLoading, setCompanyCredentialsLoading] = useState(false);
  const [companyCredentialsError, setCompanyCredentialsError] = useState(null);

  // Credentials - admin accounts
  const [adminCredentials, setAdminCredentials] = useState([]);
  const [adminCredentialsLoaded, setAdminCredentialsLoaded] = useState(false);
  const [adminCredentialsLoading, setAdminCredentialsLoading] = useState(false);
  const [adminCredentialsError, setAdminCredentialsError] = useState(null);

  // Staffing data
  const [workRequests, setWorkRequests] = useState([]);
  const [workRequestsLoaded, setWorkRequestsLoaded] = useState(false);
  const [workRequestsLoading, setWorkRequestsLoading] = useState(false);
  const [workRequestsError, setWorkRequestsError] = useState(null);

  const [adminOptions, setAdminOptions] = useState([]);
  const [adminOptionsLoaded, setAdminOptionsLoaded] = useState(false);
  const [adminOptionsLoading, setAdminOptionsLoading] = useState(false);
  const [adminOptionsError, setAdminOptionsError] = useState(null);

  // Homepage configuration for public site
  const [homepageConfig, setHomepageConfig] = useState(null);
  const [homepageConfigLoaded, setHomepageConfigLoaded] = useState(false);
  const [homepageConfigLoading, setHomepageConfigLoading] = useState(false);
  const [homepageConfigError, setHomepageConfigError] = useState(null);

  // App home configuration (authenticated landing)
  const [appHomeConfig, setAppHomeConfig] = useState(null);
  const [appHomeConfigLoaded, setAppHomeConfigLoaded] = useState(false);
  const [appHomeConfigLoading, setAppHomeConfigLoading] = useState(false);
  const [appHomeConfigError, setAppHomeConfigError] = useState(null);

  // Unified home (public + authenticated) for admin
  const [homeConfig, setHomeConfig] = useState(null);
  const [homeConfigLoaded, setHomeConfigLoaded] = useState(false);
  const [homeConfigLoading, setHomeConfigLoading] = useState(false);
  const [homeConfigError, setHomeConfigError] = useState(null);

  // Weekly tips management data
  const [weeklyTips, setWeeklyTips] = useState([]);
  const [weeklyTipsLoaded, setWeeklyTipsLoaded] = useState(false);
  const [weeklyTipsLoading, setWeeklyTipsLoading] = useState(false);
  const [weeklyTipsError, setWeeklyTipsError] = useState(null);

  // Global options used across site (functions, competences, geo areas, activity sectors)
  const [globalOptions, setGlobalOptions] = useState({
    functions: [],
    competences: [],
    geoAreas: [],
    activitySectors: [],
  });
  const [globalOptionsLoaded, setGlobalOptionsLoaded] = useState(false);
  const [globalOptionsLoading, setGlobalOptionsLoading] = useState(false);
  const [globalOptionsError, setGlobalOptionsError] = useState(null);

  const loadCompanyCredentials = useCallback(
    async ({ force = false } = {}) => {
      if (companyCredentialsLoading) return companyCredentials;
      if (companyCredentialsLoaded && !force) return companyCredentials;
      setCompanyCredentialsLoading(true);
      setCompanyCredentialsError(null);
      try {
        const resp = await apiFetch("/api/super-admin/credentials/companies");
        if (!resp.ok) throw new Error("Falha ao carregar credenciais empresariais.");
        const data = await resp.json();
        const list = Array.isArray(data) ? data : [];
        setCompanyCredentials(list);
        setCompanyCredentialsLoaded(true);
        return list;
      } catch (error) {
        setCompanyCredentialsError(error.message || "Erro inesperado ao listar credenciais.");
        throw error;
      } finally {
        setCompanyCredentialsLoading(false);
      }
    },
    [companyCredentials, companyCredentialsLoaded, companyCredentialsLoading]
  );

  const loadAdminCredentials = useCallback(
    async ({ force = false } = {}) => {
      if (adminCredentialsLoading) return adminCredentials;
      if (adminCredentialsLoaded && !force) return adminCredentials;
      setAdminCredentialsLoading(true);
      setAdminCredentialsError(null);
      try {
        const resp = await apiFetch("/api/super-admin/credentials/admins");
        if (!resp.ok) throw new Error("Falha ao carregar credenciais administrativas.");
        const data = await resp.json();
        const list = Array.isArray(data)
          ? data.map((admin) => ({
              ...admin,
              role: normalizeAdminRole(admin.role),
            }))
          : [];
        setAdminCredentials(list);
        setAdminCredentialsLoaded(true);
        return list;
      } catch (error) {
        setAdminCredentialsError(error.message || "Erro inesperado ao listar administradores.");
        throw error;
      } finally {
        setAdminCredentialsLoading(false);
      }
    },
    [adminCredentials, adminCredentialsLoaded, adminCredentialsLoading]
  );

  const loadWorkRequests = useCallback(
    async ({ force = false } = {}) => {
      if (workRequestsLoading) return workRequests;
      if (workRequestsLoaded && !force) return workRequests;
      setWorkRequestsLoading(true);
      setWorkRequestsError(null);
      try {
        const data = await teamRequestsAPI.getSuperAdminList();
        const list = Array.isArray(data) ? data : [];
        setWorkRequests(list);
        setWorkRequestsLoaded(true);
        return list;
      } catch (error) {
        setWorkRequestsError(error.message || "Erro ao carregar requisicoes.");
        throw error;
      } finally {
        setWorkRequestsLoading(false);
      }
    },
    [workRequests, workRequestsLoaded, workRequestsLoading]
  );

  const loadAdminOptions = useCallback(
    async ({ force = false } = {}) => {
      if (adminOptionsLoading) return adminOptions;
      if (adminOptionsLoaded && !force) return adminOptions;
      setAdminOptionsLoading(true);
      setAdminOptionsError(null);
      try {
        const options = await teamRequestsAPI.getAdminOptions();
        const list = Array.isArray(options)
          ? options.map((item) => ({
              id: item.id,
              name: item.username,
              requestCount: item.requestCount,
            }))
          : [];
        setAdminOptions(list);
        setAdminOptionsLoaded(true);
        return list;
      } catch (error) {
        setAdminOptionsError(error.message || "Erro ao carregar administradores.");
        throw error;
      } finally {
        setAdminOptionsLoading(false);
      }
    },
    [adminOptions, adminOptionsLoaded, adminOptionsLoading]
  );

  const loadHomepageConfig = useCallback(
    async ({ force = false } = {}) => {
      if (homepageConfigLoading) return homepageConfig;
      if (homepageConfigLoaded && !force) return homepageConfig;
      setHomepageConfigLoading(true);
      setHomepageConfigError(null);
      try {
        const payload = await fetchHomepageConfig();
        const normalized = normalizeHomepageConfig(payload);
        setHomepageConfig(normalized);
        setHomepageConfigLoaded(true);
        return normalized;
      } catch (error) {
        setHomepageConfigError(error.message || "Nao foi possivel carregar as configuracoes.");
        throw error;
      } finally {
        setHomepageConfigLoading(false);
      }
    },
    [homepageConfig, homepageConfigLoaded, homepageConfigLoading]
  );

  const loadAppHomeConfig = useCallback(
    async ({ force = false } = {}) => {
      if (appHomeConfigLoading) return appHomeConfig;
      if (appHomeConfigLoaded && !force) return appHomeConfig;
      setAppHomeConfigLoading(true);
      setAppHomeConfigError(null);
      try {
        const payload = await fetchAppHomeConfig();
        const normalized = normalizeAppHomeConfig(payload);
        setAppHomeConfig(normalized);
        setAppHomeConfigLoaded(true);
        return normalized;
      } catch (error) {
        setAppHomeConfigError(error.message || "Nao foi possivel carregar a home autenticada.");
        throw error;
      } finally {
        setAppHomeConfigLoading(false);
      }
    },
    [appHomeConfig, appHomeConfigLoaded, appHomeConfigLoading]
  );

  const loadHomeConfig = useCallback(
    async ({ force = false } = {}) => {
      if (homeConfigLoading) return homeConfig;
      if (homeConfigLoaded && !force) return homeConfig;
      setHomeConfigLoading(true);
      setHomeConfigError(null);
      try {
        const payload = await fetchUnifiedHome();
        setHomeConfig(payload);
        setHomeConfigLoaded(true);
        return payload;
      } catch (error) {
        setHomeConfigError(error.message || "Nao foi possivel carregar a home unificada.");
        throw error;
      } finally {
        setHomeConfigLoading(false);
      }
    },
    [homeConfig, homeConfigLoaded, homeConfigLoading]
  );

  const loadWeeklyTips = useCallback(
    async ({ force = false } = {}) => {
      if (weeklyTipsLoading) return weeklyTips;
      if (weeklyTipsLoaded && !force) return weeklyTips;
      setWeeklyTipsLoading(true);
      setWeeklyTipsError(null);
      try {
        const payload = await fetchWeeklyTipsAdmin();
        const sorted = sortWeeklyTips(payload ?? []);
        setWeeklyTips(sorted);
        setWeeklyTipsLoaded(true);
        return sorted;
      } catch (error) {
        setWeeklyTipsError(error.message || "Nao foi possivel carregar as dicas da semana.");
        throw error;
      } finally {
        setWeeklyTipsLoading(false);
      }
    },
    [weeklyTips, weeklyTipsLoaded, weeklyTipsLoading]
  );

  const loadGlobalOptions = useCallback(
    async ({ force = false } = {}) => {
      if (globalOptionsLoading) return globalOptions;
      if (globalOptionsLoaded && !force) return globalOptions;
      setGlobalOptionsLoading(true);
      setGlobalOptionsError(null);
      try {
        const [functions, competences, geoAreas, activitySectors] = await Promise.all([
          fetchFunctions(),
          fetchCompetences(),
          fetchGeoAreas(),
          fetchActivitySectors(),
        ]);
        const payload = {
          functions: sortByName(functions ?? []),
          competences: sortByName(competences ?? []),
          geoAreas: sortByName(geoAreas ?? []),
          activitySectors: sortByName(activitySectors ?? []),
        };
        setGlobalOptions(payload);
        setGlobalOptionsLoaded(true);
        return payload;
      } catch (error) {
        setGlobalOptionsError(error.message || "Nao foi possivel carregar as opcoes globais.");
        throw error;
      } finally {
        setGlobalOptionsLoading(false);
      }
    },
    [globalOptions, globalOptionsLoaded, globalOptionsLoading]
  );

  const value = useMemo(
    () => ({
      credentials: {
        companies: {
          data: companyCredentials,
          loading: companyCredentialsLoading,
          loaded: companyCredentialsLoaded,
          error: companyCredentialsError,
          refresh: loadCompanyCredentials,
          setData: setCompanyCredentials,
        },
        admins: {
          data: adminCredentials,
          loading: adminCredentialsLoading,
          loaded: adminCredentialsLoaded,
          error: adminCredentialsError,
          refresh: loadAdminCredentials,
          setData: setAdminCredentials,
        },
      },
      staffing: {
        requests: {
          data: workRequests,
          loading: workRequestsLoading,
          loaded: workRequestsLoaded,
          error: workRequestsError,
          refresh: loadWorkRequests,
          setData: setWorkRequests,
        },
        adminOptions: {
          data: adminOptions,
          loading: adminOptionsLoading,
          loaded: adminOptionsLoaded,
          error: adminOptionsError,
          refresh: loadAdminOptions,
          setData: setAdminOptions,
        },
      },
      site: {
        homepage: {
          data: homepageConfig,
          loading: homepageConfigLoading,
          loaded: homepageConfigLoaded,
          error: homepageConfigError,
          refresh: loadHomepageConfig,
          setData: setHomepageConfig,
        },
        appHome: {
          data: appHomeConfig,
          loading: appHomeConfigLoading,
          loaded: appHomeConfigLoaded,
          error: appHomeConfigError,
          refresh: loadAppHomeConfig,
          setData: setAppHomeConfig,
        },
        home: {
          data: homeConfig,
          loading: homeConfigLoading,
          loaded: homeConfigLoaded,
          error: homeConfigError,
          refresh: loadHomeConfig,
          setData: setHomeConfig,
        },
        weeklyTips: {
          data: weeklyTips,
          loading: weeklyTipsLoading,
          loaded: weeklyTipsLoaded,
          error: weeklyTipsError,
          refresh: loadWeeklyTips,
          setData: setWeeklyTips,
        },
        globalOptions: {
          data: globalOptions,
          loading: globalOptionsLoading,
          loaded: globalOptionsLoaded,
          error: globalOptionsError,
          refresh: loadGlobalOptions,
          setData: setGlobalOptions,
        },
      },
    }),
    [
      adminCredentials,
      adminCredentialsError,
      adminCredentialsLoaded,
      adminCredentialsLoading,
      adminOptions,
      adminOptionsError,
      adminOptionsLoaded,
      adminOptionsLoading,
      companyCredentials,
      companyCredentialsError,
      companyCredentialsLoaded,
      companyCredentialsLoading,
      appHomeConfig,
      appHomeConfigError,
      appHomeConfigLoaded,
      appHomeConfigLoading,
      globalOptions,
      globalOptionsError,
      globalOptionsLoaded,
      globalOptionsLoading,
      homepageConfig,
      homepageConfigError,
      homepageConfigLoaded,
      homepageConfigLoading,
      homeConfig,
      homeConfigError,
      homeConfigLoaded,
      homeConfigLoading,
      loadGlobalOptions,
      loadAppHomeConfig,
      loadAdminCredentials,
      loadAdminOptions,
      loadCompanyCredentials,
      loadHomepageConfig,
      loadHomeConfig,
      loadWeeklyTips,
      loadWorkRequests,
      weeklyTips,
      weeklyTipsError,
      weeklyTipsLoaded,
      weeklyTipsLoading,
      workRequests,
      workRequestsError,
      workRequestsLoaded,
      workRequestsLoading,
    ]
  );

  return <SuperAdminDataContext.Provider value={value}>{children}</SuperAdminDataContext.Provider>;
}

SuperAdminDataProvider.propTypes = {
  children: PropTypes.node,
};

export function useSuperAdminData() {
  const ctx = useContext(SuperAdminDataContext);
  if (!ctx) {
    throw new Error("useSuperAdminData deve ser usado dentro de SuperAdminDataProvider");
  }
  return ctx;
}
