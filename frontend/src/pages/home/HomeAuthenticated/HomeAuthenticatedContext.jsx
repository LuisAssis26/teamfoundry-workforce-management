import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import PropTypes from "prop-types";
import { fetchAppHomePublic, fetchWeeklyTipsPage } from "../../../api/site/siteManagement.js";
import { fetchEmployeeProfileSummary } from "../../../api/profile/employeeProfile.js";
import { useAuthContext } from "../../../auth/AuthContext.jsx";
import { useEmployeeProfile } from "../../profile/Employee/EmployeeProfileContext.jsx";
import { APP_HOME_SECTION_TYPES, FALLBACK_NEWS_PLACEHOLDERS, FALLBACK_WEEKLY_TIP } from "./constants.js";

const HomeAuthenticatedContext = createContext(null);

export function HomeAuthenticatedProvider({ children }) {
  const { logout, isAuthenticated } = useAuthContext();
  const { profile, loadingProfile, refreshProfile } = useEmployeeProfile();

  const [homeContent, setHomeContent] = useState(null);
  const [contentLoading, setContentLoading] = useState(true);
  const [contentError, setContentError] = useState(null);

  const [weeklyTipsData, setWeeklyTipsData] = useState(null);
  const [weeklyTipsError, setWeeklyTipsError] = useState(null);

  const [profileSummary, setProfileSummary] = useState(null);
  const [profileSummaryLoading, setProfileSummaryLoading] = useState(false);

  useEffect(() => {
    if (!profile && !loadingProfile) {
      refreshProfile();
    }
  }, [profile, loadingProfile, refreshProfile]);

  useEffect(() => {
    let active = true;
    setContentLoading(true);
    fetchAppHomePublic()
      .then((data) => {
        if (!active) return;
        setHomeContent(data);
        setContentError(null);
        const news = data?.sections?.find((section) => section.type === APP_HOME_SECTION_TYPES.news);
        // eslint-disable-next-line no-console
        console.info("[HomeAuthenticated] Conteudo carregado. Secoes:", data?.sections?.length ?? 0, "| Noticias recebidas:", Array.isArray(news?.newsArticles) ? news.newsArticles.length : 0);
      })
      .catch((err) => {
        if (!active) return;
        setContentError(err.message || "Nao foi possivel carregar a home autenticada.");
        // eslint-disable-next-line no-console
        console.error("[HomeAuthenticated] Erro ao carregar conteudo:", err);
      })
      .finally(() => {
        if (active) setContentLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    fetchWeeklyTipsPage()
      .then((data) => {
        if (!active) return;
        setWeeklyTipsData(data);
        setWeeklyTipsError(null);
      })
      .catch((err) => {
        if (!active) return;
        setWeeklyTipsError(err.message || "Nao foi possivel carregar as dicas da semana.");
      });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    setProfileSummaryLoading(true);
    fetchEmployeeProfileSummary()
      .then((data) => {
        if (!active) return;
        setProfileSummary(data);
      })
      .catch(() => {
        if (!active) return;
        setProfileSummary(null);
      })
      .finally(() => {
        if (active) setProfileSummaryLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const heroSection = useMemo(
    () => homeContent?.sections?.find((section) => section.type === APP_HOME_SECTION_TYPES.hero),
    [homeContent]
  );
  const weeklyTipSection = useMemo(
    () => homeContent?.sections?.find((section) => section.type === APP_HOME_SECTION_TYPES.weeklyTip),
    [homeContent]
  );
  const newsSection = useMemo(
    () => homeContent?.sections?.find((section) => section.type === APP_HOME_SECTION_TYPES.news),
    [homeContent]
  );
  const newsArticles = useMemo(
    () => (Array.isArray(newsSection?.newsArticles) ? newsSection.newsArticles : []),
    [newsSection]
  );

  useEffect(() => {
    if (contentLoading) return;
    // eslint-disable-next-line no-console
    console.info("[HomeAuthenticated] Noticias prontas para render:", newsArticles.length, "| Section ativa:", !!newsSection);
  }, [contentLoading, newsArticles.length, newsSection]);

  const displayName = useMemo(() => {
    if (profile?.firstName) {
      return `${profile.firstName}${profile.lastName ? ` ${profile.lastName}` : ""}`;
    }
    return "Utilizador";
  }, [profile]);

  const value = useMemo(
    () => ({
      // auth
      isAuthenticated,
      logout,

      // profile
      profile,
      loadingProfile,
      refreshProfile,
      profileSummary,
      profileSummaryLoading,
      displayName,

      // content
      homeContent,
      contentLoading,
      contentError,
      weeklyTipsData,
      weeklyTipsError,
      heroSection,
      weeklyTipSection,
      newsSection,
      newsArticles,
      fallbackWeeklyTip: FALLBACK_WEEKLY_TIP,
      fallbackNews: FALLBACK_NEWS_PLACEHOLDERS,
    }),
    [
      contentError,
      contentLoading,
      displayName,
      heroSection,
      homeContent,
      isAuthenticated,
      loadingProfile,
      logout,
      newsArticles,
      newsSection,
      profile,
      profileSummary,
      profileSummaryLoading,
      refreshProfile,
      weeklyTipSection,
      weeklyTipsData,
      weeklyTipsError,
    ]
  );

  return (
    <HomeAuthenticatedContext.Provider value={value}>
      {children}
    </HomeAuthenticatedContext.Provider>
  );
}

HomeAuthenticatedProvider.propTypes = {
  children: PropTypes.node,
};

export function useHomeAuthenticated() {
  const ctx = useContext(HomeAuthenticatedContext);
  if (!ctx) throw new Error("useHomeAuthenticated deve ser usado dentro de HomeAuthenticatedProvider");
  return ctx;
}
