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
import { useNavigate } from "react-router-dom";
import {
  createActivitySector,
  createCompetence,
  createFunction,
  createGeoArea,
  createIndustry,
  createPartner,
  deleteActivitySector,
  deleteCompetence,
  deleteFunction,
  deleteGeoArea,
  deleteIndustry,
  deletePartner,
  reorderIndustries,
  reorderPartners,
  reorderSections,
  updateIndustry,
  updatePartner,
  updateSection,
  uploadSiteImage,
} from "../../../../api/site/siteManagement.js";
import { clearTokens } from "../../../../auth/tokenStorage.js";
import { useSuperAdminData } from "../SuperAdminDataContext.jsx";
import { moveItemInList, sortByName, sortByOrder } from "./utils.js";
import { DEFAULT_GLOBAL_OPTIONS, EMPTY_FORMS, OPTION_LABELS, VIEW_TABS } from "./constants.js";

const VariableManagementContext = createContext(null);

export function VariableManagementProvider({ children }) {
  const navigate = useNavigate();
  const mountedRef = useRef(false);
  const {
    site: {
      homepage: {
        data: config,
        setData: setConfig,
        loading,
        loaded: configLoaded,
        error: loadError,
        refresh: refreshConfig,
      },
    },
  } = useSuperAdminData();
  const {
    site: {
      globalOptions: {
        data: globalOptions = DEFAULT_GLOBAL_OPTIONS,
        loading: globalOptionsLoading,
        loaded: globalOptionsLoaded,
        error: globalOptionsError,
        refresh: refreshGlobalOptions,
        setData: setGlobalOptions,
      },
    },
  } = useSuperAdminData();

  const [activeView, setActiveView] = useState(VIEW_TABS[0].id);
  const [manageModal, setManageModal] = useState({
    open: false,
    type: null,
    search: "",
  });
  const [deleteModal, setDeleteModal] = useState({
    open: false,
    type: null,
    record: null,
    password: "",
    saving: false,
    error: null,
  });
  const [optionsLoading, setOptionsLoading] = useState(false);
  const [optionsError, setOptionsError] = useState(null);
  const [optionModal, setOptionModal] = useState({
    open: false,
    type: null,
    name: "",
    saving: false,
  });
  const [globalOptionsErrorDismissed, setGlobalOptionsErrorDismissed] = useState(false);

  const [banner, setBanner] = useState(null);
  const [heroForm, setHeroForm] = useState(null);
  const [savingHero, setSavingHero] = useState(false);

  const [modalState, setModalState] = useState({
    open: false,
    entity: null,
    mode: "create",
    record: null,
  });
  const [modalForm, setModalForm] = useState(null);
  const [modalSaving, setModalSaving] = useState(false);

  const handleUnauthorized = useCallback(() => {
    clearTokens();
    navigate("/admin", { replace: true });
  }, [navigate]);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  const initialConfigLoad = useRef(false);
  useEffect(() => {
    if (configLoaded || initialConfigLoad.current) return;
    initialConfigLoad.current = true;
    refreshConfig().catch((err) => {
      if (err?.status === 401) {
        handleUnauthorized();
      }
    });
  }, [configLoaded, refreshConfig, handleUnauthorized]);

  const retryHomepageConfig = useCallback(() => {
    refreshConfig({ force: true }).catch((err) => {
      if (err?.status === 401) {
        handleUnauthorized();
      }
    });
  }, [refreshConfig, handleUnauthorized]);

  useEffect(() => {
    if (globalOptionsError) {
      setGlobalOptionsErrorDismissed(false);
    }
  }, [globalOptionsError]);

  const loadGlobalOptions = useCallback(async () => {
    if (globalOptionsLoading || globalOptionsLoaded) return;
    setOptionsLoading(true);
    setOptionsError(null);
    try {
      await refreshGlobalOptions();
    } catch (err) {
      if (err?.status === 401) {
        handleUnauthorized();
        return;
      }
      setOptionsError(err.message || "Não foi possível carregar as opções globais.");
    } finally {
      if (mountedRef.current) setOptionsLoading(false);
    }
  }, [
    globalOptionsLoading,
    globalOptionsLoaded,
    refreshGlobalOptions,
    handleUnauthorized,
    setOptionsError,
    setOptionsLoading,
  ]);

  useEffect(() => {
    if (activeView === "globalVars" && !globalOptionsLoaded) {
      loadGlobalOptions();
    }
  }, [activeView, globalOptionsLoaded, loadGlobalOptions]);

  const effectiveGlobalOptionsError = globalOptionsErrorDismissed ? null : globalOptionsError;
  const combinedOptionsError = optionsError || effectiveGlobalOptionsError;
  const isGlobalOptionsLoading = optionsLoading || globalOptionsLoading;

  const handleOptionsErrorClose = useCallback(() => {
    if (optionsError) {
      setOptionsError(null);
    } else {
      setGlobalOptionsErrorDismissed(true);
    }
  }, [optionsError]);

  const heroSection = useMemo(
    () => config?.sections?.find((section) => section.type === "HERO"),
    [config]
  );

  useEffect(() => {
    if (!heroSection) return;
    setHeroForm({
      title: heroSection.title ?? "",
      subtitle: heroSection.subtitle ?? "",
      primaryCtaLabel: heroSection.primaryCtaLabel ?? "",
      primaryCtaUrl: heroSection.primaryCtaUrl ?? "",
      secondaryCtaLabel: heroSection.secondaryCtaLabel ?? "",
      secondaryCtaUrl: heroSection.secondaryCtaUrl ?? "",
      active: Boolean(heroSection.active),
    });
  }, [heroSection]);

  const handleHeroFieldChange = useCallback((field, value) => {
    setHeroForm((prev) => ({ ...prev, [field]: value }));
  }, []);

  const handleHeroSubmit = useCallback(
    async (event) => {
      event.preventDefault();
      if (!heroSection || !heroForm) return;
      setSavingHero(true);
      setBanner(null);
      try {
        const updated = await updateSection(heroSection.id, heroForm);
        setConfig((prev) => ({
          ...prev,
          sections: prev.sections.map((section) => (section.id === updated.id ? updated : section)),
        }));
        setBanner({ type: "success", message: "Hero atualizado com sucesso." });
      } catch (err) {
        setBanner({
          type: "error",
          message: err.message || "NÆo foi poss¡vel guardar o hero.",
        });
      } finally {
        setSavingHero(false);
      }
    },
    [heroSection, heroForm, setBanner, setConfig, setSavingHero]
  );

  const handleSectionMove = useCallback(
    async (id, direction) => {
      if (!config) return;
      const next = moveItemInList(config.sections, id, direction);
      if (!next) return;

      const previous = config.sections;
      setConfig((prev) => ({ ...prev, sections: next }));
      setBanner(null);

      try {
        await reorderSections(next.map((section) => section.id));
        setBanner({ type: "success", message: "Ordem das sec‡äes atualizada." });
      } catch (err) {
        setConfig((prev) => ({ ...prev, sections: previous }));
        setBanner({
          type: "error",
          message: err.message || "NÆo foi poss¡vel reordenar as sec‡äes.",
        });
      }
    },
    [config, setBanner, setConfig]
  );

  const handleSectionToggle = useCallback(
    async (section) => {
      setBanner(null);
      try {
        const updated = await updateSection(section.id, {
          title: section.title,
          subtitle: section.subtitle,
          primaryCtaLabel: section.primaryCtaLabel,
          primaryCtaUrl: section.primaryCtaUrl,
          secondaryCtaLabel: section.secondaryCtaLabel,
          secondaryCtaUrl: section.secondaryCtaUrl,
          active: !section.active,
        });
        setConfig((prev) => ({
          ...prev,
          sections: prev.sections.map((item) => (item.id === updated.id ? updated : item)),
        }));
        setBanner({
          type: "success",
          message: `Seção ${updated.active ? "ativada" : "ocultada"} com sucesso.`,
        });
      } catch (err) {
        setBanner({
          type: "error",
          message: err.message || "NÆo foi poss¡vel atualizar a sec‡Æo.",
        });
      }
    },
    [setBanner, setConfig]
  );

  const handleReorderList = useCallback(
    async (key, apiFn, id, direction) => {
      const list = config?.[key];
      if (!Array.isArray(list)) return;

      const next = moveItemInList(list, id, direction);
      if (!next) return;

      const previous = list;
      setConfig((prev) => ({ ...prev, [key]: next }));
      setBanner(null);
      try {
        await apiFn(next.map((item) => item.id));
        setBanner({ type: "success", message: "Ordena‡Æo atualizada." });
      } catch (err) {
        setConfig((prev) => ({ ...prev, [key]: previous }));
        setBanner({
          type: "error",
          message: err.message || "NÆo foi poss¡vel reordenar a lista.",
        });
      }
    },
    [config, setBanner, setConfig]
  );

  const handleIndustryMove = useCallback(
    async (id, direction) => handleReorderList("industries", reorderIndustries, id, direction),
    [handleReorderList]
  );

  const handlePartnerMove = useCallback(
    async (id, direction) => handleReorderList("partners", reorderPartners, id, direction),
    [handleReorderList]
  );

  const handleDeleteIndustry = useCallback(
    async (record, { confirmDeletion = true } = {}) => {
      if (!record) return false;
      if (confirmDeletion && !window.confirm(`Eliminar a ind£stria "${record.name}"?`)) {
        return false;
      }
      setBanner(null);
      try {
        await deleteIndustry(record.id);
        setConfig((prev) => ({
          ...prev,
          industries: prev.industries.filter((item) => item.id !== record.id),
        }));
        setBanner({ type: "success", message: "Ind£stria removida com sucesso." });
        return true;
      } catch (err) {
        if (err?.status === 401) {
          handleUnauthorized();
          return false;
        }
        setBanner({
          type: "error",
          message: err.message || "NÆo foi poss¡vel eliminar a ind£stria.",
        });
        return false;
      }
    },
    [handleUnauthorized, setBanner, setConfig]
  );

  const handleDeletePartner = useCallback(
    async (record, { confirmDeletion = true } = {}) => {
      if (!record) return false;
      if (confirmDeletion && !window.confirm(`Eliminar o parceiro "${record.name}"?`)) {
        return false;
      }
      setBanner(null);
      try {
        await deletePartner(record.id);
        setConfig((prev) => ({
          ...prev,
          partners: prev.partners.filter((item) => item.id !== record.id),
        }));
        setBanner({ type: "success", message: "Parceiro removido com sucesso." });
        return true;
      } catch (err) {
        if (err?.status === 401) {
          handleUnauthorized();
          return false;
        }
        setBanner({
          type: "error",
          message: err.message || "NÆo foi poss¡vel eliminar o parceiro.",
        });
        return false;
      }
    },
    [handleUnauthorized, setBanner, setConfig]
  );

  const openOptionModal = useCallback((type) => {
    setOptionModal({ open: true, type, name: "", saving: false });
  }, []);

  const closeOptionModal = useCallback(() => {
    setOptionModal({ open: false, type: null, name: "", saving: false });
  }, []);

  const createOption = useCallback((type, name) => {
    const payload = { name };
    switch (type) {
      case "functions":
        return createFunction(payload);
      case "competences":
        return createCompetence(payload);
      case "geoAreas":
        return createGeoArea(payload);
      case "activitySectors":
        return createActivitySector(payload);
      default:
        return Promise.reject(new Error("Tipo desconhecido."));
    }
  }, []);

  const deleteOption = useCallback((type, id) => {
    switch (type) {
      case "functions":
        return deleteFunction(id);
      case "competences":
        return deleteCompetence(id);
      case "geoAreas":
        return deleteGeoArea(id);
      case "activitySectors":
        return deleteActivitySector(id);
      default:
        return Promise.reject(new Error("Tipo desconhecido."));
    }
  }, []);

  const handleOptionSubmit = useCallback(
    async (event) => {
      event.preventDefault();
      if (!optionModal.type) return;
      const trimmed = optionModal.name.trim();
      if (!trimmed) {
        setOptionsError("Informe o nome.");
        return;
      }
      setOptionModal((prev) => ({ ...prev, saving: true }));
      setOptionsError(null);
      try {
        const created = await createOption(optionModal.type, trimmed);
        setGlobalOptions((prev) => ({
          ...prev,
          [optionModal.type]: sortByName([...prev[optionModal.type], created]),
        }));
        closeOptionModal();
      } catch (err) {
        if (err?.status === 401) {
          handleUnauthorized();
          return;
        }
        setOptionsError(err.message || "Não foi possível criar o registo.");
        setOptionModal((prev) => ({ ...prev, saving: false }));
      }
    },
    [optionModal, closeOptionModal, createOption, handleUnauthorized, setGlobalOptions, setOptionModal, setOptionsError]
  );

  const handleDeleteOption = useCallback(
    async (type, record) => {
      if (!type || !record) return;
      try {
        await deleteOption(type, record.id);
        setGlobalOptions((prev) => ({
          ...prev,
          [type]: prev[type].filter((item) => item.id !== record.id),
        }));
      } catch (err) {
        if (err?.status === 401) {
          handleUnauthorized();
          return;
        }
        throw err;
      }
    },
    [deleteOption, handleUnauthorized, setGlobalOptions]
  );

  const openDeleteModal = useCallback((type, record) => {
    setDeleteModal({
      open: true,
      type,
      record,
      password: "",
      saving: false,
      error: null,
    });
  }, []);

  const closeDeleteModal = useCallback(() => {
    setDeleteModal({
      open: false,
      type: null,
      record: null,
      password: "",
      saving: false,
      error: null,
    });
  }, []);

  const confirmDeleteOption = useCallback(
    async (event) => {
      event.preventDefault();
      if (!deleteModal.password.trim()) {
        setDeleteModal((prev) => ({ ...prev, error: "Informe a password do super admin." }));
        return;
      }
      setDeleteModal((prev) => ({ ...prev, saving: true, error: null }));
      try {
        await handleDeleteOption(deleteModal.type, deleteModal.record);
        closeDeleteModal();
      } catch (err) {
        setDeleteModal((prev) => ({
          ...prev,
          saving: false,
          error: err.message || "Não foi possível eliminar o registo.",
        }));
      }
    },
    [deleteModal, handleDeleteOption, closeDeleteModal, setDeleteModal]
  );

  const getModalForm = useCallback((entity, record) => {
    if (!record) {
      return { ...EMPTY_FORMS[entity] };
    }
    if (entity === "industry") {
      return {
        name: record.name ?? "",
        description: record.description ?? "",
        imageUrl: record.imageUrl ?? "",
        linkUrl: record.linkUrl ?? "",
        active: Boolean(record.active),
      };
    }
    return {
      name: record.name ?? "",
      description: record.description ?? "",
      imageUrl: record.imageUrl ?? "",
      websiteUrl: record.websiteUrl ?? "",
      active: Boolean(record.active),
    };
  }, []);

  const openModal = useCallback(
    (entity, record = null) => {
      const mode = record ? "edit" : "create";
      setModalState({ open: true, entity, mode, record });
      setModalForm(getModalForm(entity, record));
    },
    [getModalForm]
  );

  const closeModal = useCallback(() => {
    setModalState({ open: false, entity: null, mode: "create", record: null });
    setModalForm(null);
    setModalSaving(false);
  }, []);

  const handleModalFieldChange = useCallback((field, value) => {
    setModalForm((prev) => ({ ...prev, [field]: value }));
  }, []);

  const handleModalSubmit = useCallback(
    async (event) => {
      event.preventDefault();
      if (!modalState.open || !modalState.entity || !modalForm) return;

      setModalSaving(true);
      setBanner(null);

      const { entity, mode, record } = modalState;
      try {
        if (entity === "industry") {
          const payload = {
            name: modalForm.name?.trim() ?? "",
            description: modalForm.description ?? record?.description ?? "",
            imageUrl: modalForm.imageUrl,
            linkUrl: modalForm.linkUrl ?? record?.linkUrl ?? "",
            active: Boolean(modalForm.active),
          };
          const result =
            mode === "create"
              ? await createIndustry(payload)
              : await updateIndustry(record.id, payload);
          setConfig((prev) => ({
            ...prev,
            industries: sortByOrder(
              mode === "create"
                ? [...prev.industries, result]
                : prev.industries.map((item) => (item.id === result.id ? result : item))
            ),
          }));
        } else {
          const payload = {
            name: modalForm.name?.trim() ?? "",
            description: modalForm.description ?? "",
            imageUrl: modalForm.imageUrl,
            websiteUrl: modalForm.websiteUrl ?? "",
            active: Boolean(modalForm.active),
          };
          const result =
            mode === "create"
              ? await createPartner(payload)
              : await updatePartner(record.id, payload);
          setConfig((prev) => ({
            ...prev,
            partners: sortByOrder(
              mode === "create"
                ? [...prev.partners, result]
                : prev.partners.map((item) => (item.id === result.id ? result : item))
            ),
          }));
        }
        closeModal();
        setBanner({ type: "success", message: "Conte£do guardado com sucesso." });
      } catch (err) {
        setBanner({
          type: "error",
          message: err.message || "NÆo foi poss¡vel guardar os dados.",
        });
        setModalSaving(false);
      }
    },
    [modalForm, modalState, closeModal, setBanner, setConfig, setModalSaving]
  );

  const handleModalDelete = useCallback(async () => {
    if (modalState.mode !== "edit" || !modalState.record) return;
    const fn = modalState.entity === "industry" ? handleDeleteIndustry : handleDeletePartner;
    const success = await fn(modalState.record);
    if (success) {
      closeModal();
    }
  }, [modalState, handleDeleteIndustry, handleDeletePartner, closeModal]);

  const value = useMemo(
    () => ({
      activeView,
      setActiveView,
      banner,
      setBanner,
      config,
      configLoaded,
      loading,
      loadError,
      retryHomepageConfig,
      heroForm,
      savingHero,
      handleHeroFieldChange,
      handleHeroSubmit,
      handleSectionMove,
      handleSectionToggle,
      handleIndustryMove,
      handlePartnerMove,
      openModal,
      closeModal,
      modalState,
      modalForm,
      modalSaving,
      handleModalFieldChange,
      handleModalSubmit,
      handleModalDelete,
      handleUnauthorized,
      globalOptions,
      globalOptionsLoaded,
      combinedOptionsError,
      isGlobalOptionsLoading,
      handleOptionsErrorClose,
      manageModal,
      setManageModal,
      optionModal,
      openOptionModal,
      closeOptionModal,
      setOptionModal,
      handleOptionSubmit,
      optionLabels: OPTION_LABELS,
      deleteModal,
      setDeleteModal,
      openDeleteModal,
      closeDeleteModal,
      confirmDeleteOption,
      uploadSiteImage,
    }),
    [
      activeView,
      banner,
      closeDeleteModal,
      closeModal,
      closeOptionModal,
      combinedOptionsError,
      config,
      configLoaded,
      confirmDeleteOption,
      deleteModal,
      globalOptions,
      globalOptionsLoaded,
      handleHeroFieldChange,
      handleHeroSubmit,
      handleIndustryMove,
      handleModalDelete,
      handleModalFieldChange,
      handleModalSubmit,
      handleUnauthorized,
      handleOptionSubmit,
      handleOptionsErrorClose,
      handlePartnerMove,
      handleSectionMove,
      handleSectionToggle,
      heroForm,
      isGlobalOptionsLoading,
      loadError,
      loading,
      manageModal,
      modalForm,
      modalSaving,
      modalState,
      openDeleteModal,
      openModal,
      openOptionModal,
      optionModal,
      retryHomepageConfig,
      savingHero,
      setActiveView,
      setBanner,
      setManageModal,
      setModalSaving,
      setDeleteModal,
      setOptionModal,
      uploadSiteImage,
    ]
  );

  return <VariableManagementContext.Provider value={value}>{children}</VariableManagementContext.Provider>;
}

VariableManagementProvider.propTypes = {
  children: PropTypes.node,
};

export function useVariableManagement() {
  const context = useContext(VariableManagementContext);
  if (!context) {
    throw new Error("useVariableManagement deve ser usado dentro de VariableManagementProvider");
  }
  return context;
}
