import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import PropTypes from "prop-types";
import { fetchEmployeeProfile } from "../../../api/profile/employeeProfile.js";
import { fetchEmployeePreferences } from "../../../api/profile/employeePreferences.js";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

// Contexto central do perfil de colaborador (dados base + caches de tabs).
const EmployeeProfileContext = createContext(null);

export function EmployeeProfileProvider({ children }) {
  // Mantém cache local do perfil e dos dados das tabs, evitando refetch a cada navegação.
  const [profile, setProfile] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(false);
  const [profileError, setProfileError] = useState(null);
  const [personalData, setPersonalData] = useState(null);
  const [preferencesData, setPreferencesData] = useState(null);
  const [preferencesLoaded, setPreferencesLoaded] = useState(false);
  const [profileOptionsData, setProfileOptionsData] = useState(null);
  const [educationData, setEducationData] = useState(null);
  const [jobsData, setJobsData] = useState(null);
  const [offersData, setOffersData] = useState(null);
  const [offersLoaded, setOffersLoaded] = useState(false);
  const [documentsData, setDocumentsData] = useState(null);
  const [documentsLoaded, setDocumentsLoaded] = useState(false);
  const { isAuthenticated, userType } = useAuthContext();

  const refreshProfile = useCallback(async () => {
    // Recarrega o perfil se existir sessão; caso contrário, limpa qualquer cache.
    if (!isAuthenticated || userType !== "EMPLOYEE") {
      setProfile(null);
      setOffersData(null);
      setOffersLoaded(false);
      setDocumentsData(null);
      setDocumentsLoaded(false);
      return null;
    }
    setLoadingProfile(true);
    try {
      const data = await fetchEmployeeProfile();
      setProfile(data);
      setProfileError(null);
      return data;
    } catch (error) {
      setProfileError(error.message || "Não foi possível carregar o perfil.");
      return null;
    } finally {
      setLoadingProfile(false);
    }
  }, [isAuthenticated, userType]);

  useEffect(() => {
    // Busca inicial do perfil logo que o provider monta.
    if (userType === "EMPLOYEE") {
      refreshProfile();
    } else {
      setProfile(null);
      setLoadingProfile(false);
    }
  }, [refreshProfile, userType]);

  const refreshPreferencesData = useCallback(async () => {
    if (!isAuthenticated) {
      setPreferencesData(null);
      setPreferencesLoaded(false);
      return null;
    }
    if (preferencesData && preferencesLoaded) return preferencesData;
    const data = await fetchEmployeePreferences();
    setPreferencesData(data);
    setPreferencesLoaded(true);
    return data;
  }, [isAuthenticated, preferencesData, preferencesLoaded]);

  const value = useMemo(
    () => ({
      profile,
      loadingProfile,
      profileError,
      refreshProfile,
      setProfile,
      personalData,
      setPersonalData,
      preferencesData,
      setPreferencesData,
      preferencesLoaded,
      setPreferencesLoaded,
      refreshPreferencesData,
      educationData,
      setEducationData,
      jobsData,
      setJobsData,
      profileOptionsData,
      setProfileOptionsData,
      offersData,
      setOffersData,
      offersLoaded,
      setOffersLoaded,
      documentsData,
      setDocumentsData,
      documentsLoaded,
      setDocumentsLoaded,
    }),
    [
      profile,
      loadingProfile,
      profileError,
      refreshProfile,
      personalData,
      preferencesData,
      preferencesLoaded,
      refreshPreferencesData,
      educationData,
      jobsData,
      profileOptionsData,
      offersData,
      offersLoaded,
      documentsData,
      documentsLoaded,
    ]
  );

  return <EmployeeProfileContext.Provider value={value}>{children}</EmployeeProfileContext.Provider>;
}

EmployeeProfileProvider.propTypes = {
  children: PropTypes.node,
};

export function useEmployeeProfile() {
  // Hook de conveniência para aceder ao contexto já validado.
  const ctx = useContext(EmployeeProfileContext);
  if (!ctx) {
    throw new Error("useEmployeeProfile deve ser usado dentro de EmployeeProfileProvider");
  }
  return ctx;
}
