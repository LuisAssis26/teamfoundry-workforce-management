import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import PropTypes from "prop-types";
import { updateAppHomeSection, reorderAppHomeSections } from "../../../../api/site/siteManagement.js";
import { useSuperAdminData } from "../SuperAdminDataContext.jsx";
import { moveItemInList } from "../VariableManagement/utils.js";
import { APP_SECTION_LABELS, APP_SECTION_TYPES } from "./constants.js";

const AppHomeContext = createContext(null);

function defaultAppSectionForm() {
  return {
    title: "",
    subtitle: "",
    content: "",
    primaryCtaLabel: "",
    primaryCtaUrl: "",
    active: true,
    apiEnabled: false,
    apiUrl: "",
    apiMaxItems: "",
    greetingPrefix: "Ola",
    profileBarVisible: true,
    labelCurrentCompany: "Empresa atual",
    labelOffers: "Ofertas disponiveis",
  };
}

function mapAppSectionForm(section) {
  if (!section) {
    return defaultAppSectionForm();
  }
  return {
    title: section.title ?? "",
    subtitle: section.subtitle ?? "",
    content: section.content ?? "",
    primaryCtaLabel: section.primaryCtaLabel ?? "",
    primaryCtaUrl: section.primaryCtaUrl ?? "",
    active: Boolean(section.active),
    apiEnabled: Boolean(section.apiEnabled),
    apiUrl: section.apiUrl ?? "",
    apiMaxItems: section.apiMaxItems ?? "",
    greetingPrefix: section.greetingPrefix ?? "Ola",
    profileBarVisible: section.profileBarVisible ?? true,
    labelCurrentCompany: section.labelCurrentCompany ?? "Empresa atual",
    labelOffers: section.labelOffers ?? "Ofertas disponiveis",
  };
}

function buildSectionPayload(form) {
  return {
    title: form.title,
    subtitle: form.subtitle,
    content: form.content,
    primaryCtaLabel: form.primaryCtaLabel,
    primaryCtaUrl: form.primaryCtaUrl,
    active: form.active,
    apiEnabled: Boolean(form.apiEnabled),
    apiUrl: form.apiUrl || null,
    apiMaxItems: form.apiMaxItems ? Number(form.apiMaxItems) : null,
    greetingPrefix: form.greetingPrefix || null,
    profileBarVisible: form.profileBarVisible,
    labelCurrentCompany: form.labelCurrentCompany || null,
    labelOffers: form.labelOffers || null,
  };
}

const sectionKeyFromType = (type) =>
  Object.entries(APP_SECTION_TYPES).find(([, value]) => value === type)?.[0] ?? null;

export function AppHomeProvider({ children, onUnauthorized }) {
  const {
    site: {
      appHome: {
        data: config,
        loading,
        loaded,
        error: loadError,
        refresh: refreshAppHome,
        setData: setAppHomeConfig,
      },
    },
  } = useSuperAdminData();

  const allowedTypes = useMemo(() => Object.values(APP_SECTION_TYPES), []);
  const [banner, setBanner] = useState(null);
  const [forms, setForms] = useState({
    hero: defaultAppSectionForm(),
    weeklyTip: defaultAppSectionForm(),
    news: defaultAppSectionForm(),
  });
  const [savingSections, setSavingSections] = useState({
    hero: false,
    weeklyTip: false,
    news: false,
  });
  const initialLoad = useRef(false);

  useEffect(() => {
    if (loaded || initialLoad.current) return;
    initialLoad.current = true;
    refreshAppHome().catch((err) => {
      if (err?.status === 401) onUnauthorized?.();
    });
  }, [loaded, refreshAppHome, onUnauthorized]);

  const retryAppHomeConfig = useCallback(() => {
    refreshAppHome({ force: true }).catch((err) => {
      if (err?.status === 401) onUnauthorized?.();
    });
  }, [refreshAppHome, onUnauthorized]);

  const sections = useMemo(
    () => (config?.sections ?? []).filter((section) => allowedTypes.includes(section.type)),
    [config, allowedTypes]
  );

  useEffect(() => {
    if (!config) return;
    setForms({
      hero: mapAppSectionForm(sections.find((section) => section.type === APP_SECTION_TYPES.hero)),
      weeklyTip: mapAppSectionForm(sections.find((section) => section.type === APP_SECTION_TYPES.weeklyTip)),
      news: mapAppSectionForm(sections.find((section) => section.type === APP_SECTION_TYPES.news)),
    });
  }, [config, sections]);

  const handleSectionFieldChange = useCallback((key, field, value) => {
    setForms((prev) => ({
      ...prev,
      [key]: { ...prev[key], [field]: value },
    }));
  }, []);

  const handleSectionSubmit = useCallback(
    async (event, key) => {
      event.preventDefault();
      const section = sections.find((item) => item.type === APP_SECTION_TYPES[key]);
      const form = forms[key];
      if (!section || !form) return;
      setSavingSections((prev) => ({ ...prev, [key]: true }));
      setBanner(null);
      try {
        const updated = await updateAppHomeSection(section.id, buildSectionPayload(form));
        setAppHomeConfig((prev) => ({
          ...prev,
          sections: prev.sections.map((item) => (item.id === updated.id ? updated : item)),
        }));
        setForms((prev) => ({ ...prev, [key]: mapAppSectionForm(updated) }));
        setBanner({ type: "success", message: "Secao guardada com sucesso." });
      } catch (err) {
        if (err?.status === 401) {
          onUnauthorized?.();
        }
        setBanner({ type: "error", message: err.message || "Nao foi possivel guardar a secao." });
      } finally {
        setSavingSections((prev) => ({ ...prev, [key]: false }));
      }
    },
    [forms, sections, onUnauthorized, setAppHomeConfig]
  );

  const handleSectionMove = useCallback(
    async (id, direction) => {
      const next = moveItemInList(sections, id, direction);
      if (!next) return;
      const previous = sections;
      setAppHomeConfig((prev) => ({ ...prev, sections: next }));
      setBanner(null);
      try {
        await reorderAppHomeSections(next.map((section) => section.id));
        setBanner({ type: "success", message: "Ordem atualizada com sucesso." });
      } catch (err) {
        if (err?.status === 401) {
          onUnauthorized?.();
        }
        setAppHomeConfig((prev) => ({ ...prev, sections: previous }));
        setBanner({ type: "error", message: err.message || "Nao foi possivel reordenar as secoes." });
      }
    },
    [sections, setAppHomeConfig, onUnauthorized]
  );

  const handleSectionToggle = useCallback(
    async (section) => {
      const key = sectionKeyFromType(section.type);
      if (!key) return;
      setBanner(null);
      try {
        const updated = await updateAppHomeSection(section.id, {
          title: section.title,
          subtitle: section.subtitle,
          content: section.content,
          primaryCtaLabel: section.primaryCtaLabel,
          primaryCtaUrl: section.primaryCtaUrl,
          active: !section.active,
          apiEnabled: section.apiEnabled,
          apiUrl: section.apiUrl,
          apiMaxItems: section.apiMaxItems,
          greetingPrefix: section.greetingPrefix,
          profileBarVisible: section.profileBarVisible,
          labelCurrentCompany: section.labelCurrentCompany,
          labelOffers: section.labelOffers,
        });
        setAppHomeConfig((prev) => ({
          ...prev,
          sections: prev.sections.map((item) => (item.id === updated.id ? updated : item)),
        }));
        setForms((prev) => ({ ...prev, [key]: mapAppSectionForm(updated) }));
        setBanner({ type: "success", message: "Visibilidade atualizada." });
      } catch (err) {
        if (err?.status === 401) {
          onUnauthorized?.();
        }
        setBanner({ type: "error", message: err.message || "Nao foi possivel atualizar a secao." });
      }
    },
    [setAppHomeConfig, onUnauthorized]
  );

  const value = useMemo(
    () => ({
      banner,
      setBanner,
      forms,
      savingSections,
      sections,
      config,
      loading,
      loaded,
      loadError,
      APP_SECTION_LABELS,
      handleSectionFieldChange,
      handleSectionSubmit,
      handleSectionMove,
      handleSectionToggle,
      retryAppHomeConfig,
    }),
    [
      banner,
      forms,
      savingSections,
      sections,
      config,
      loading,
      loaded,
      loadError,
      handleSectionFieldChange,
      handleSectionSubmit,
      handleSectionMove,
      handleSectionToggle,
      retryAppHomeConfig,
    ]
  );

  return <AppHomeContext.Provider value={value}>{children}</AppHomeContext.Provider>;
}

AppHomeProvider.propTypes = {
  children: PropTypes.node,
  onUnauthorized: PropTypes.func,
};

AppHomeProvider.defaultProps = {
  children: null,
  onUnauthorized: null,
};

export function useAppHome() {
  const ctx = useContext(AppHomeContext);
  if (!ctx) {
    throw new Error("useAppHome deve ser usado dentro de AppHomeProvider");
  }
  return ctx;
}
