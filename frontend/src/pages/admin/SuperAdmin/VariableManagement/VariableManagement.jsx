import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  createIndustry,
  createPartner,
  reorderIndustries,
  reorderPartners,
  reorderSections,
  updateIndustry,
  updatePartner,
  updateSection,
  uploadSiteImage,
  deleteIndustry,
  deletePartner,
  createFunction,
  deleteFunction,
  createCompetence,
  deleteCompetence,
  createGeoArea,
  deleteGeoArea,
  createActivitySector,
  deleteActivitySector,
} from "../../../../api/site/siteManagement.js";
import Modal from "../../../../components/ui/Modal/Modal.jsx";
import DropZone from "../../../../components/ui/Upload/DropZone.jsx";
import { clearTokens } from "../../../../auth/tokenStorage.js";
import AppHomeManager from "../AppHomeManager.jsx";
import WeeklyTipsManager from "../WeeklyTipsManager.jsx";
import { useSuperAdminData } from "../SuperAdminDataContext.jsx";
import { moveItemInList, sortByName, sortByOrder } from "./utils.js";

const SECTION_LABELS = {
  HERO: "Hero (topo)",
  INDUSTRIES: "Indústrias",
  PARTNERS: "Parceiros",
};

const VIEW_TABS = [
  {
    id: "publicHome",
    label: "Home pública",
    description: "Configura a landing page visivel antes do login.",
  },
  {
    id: "appHome",
    label: "Home autenticada",
    description: "Conteudo mostrado quando o utilizador ja fez login.",
  },
  {
    id: "weeklyTips",
    label: "Dicas da semana",
    description: "Sugestoes rapidas para destacar na plataforma.",
  },
  {
    id: "globalVars",
    label: "Variáveis globais",
    description: "Texto e links reutilizados em varias paginas.",
  }
];

const EMPTY_FORMS = {
  industry: {
    name: "",
    description: "",
    imageUrl: "",
    linkUrl: "",
    active: true,
  },
  partner: {
    name: "",
    description: "",
    imageUrl: "",
    websiteUrl: "",
    active: true,
  },
};

export default function VariableManagement() {
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
  const [activeView, setActiveView] = useState("publicHome");

  const {
    site: {
      globalOptions: {
        data: globalOptions = {
          functions: [],
          competences: [],
          geoAreas: [],
          activitySectors: [],
        },
        loading: globalOptionsLoading,
        loaded: globalOptionsLoaded,
        error: globalOptionsError,
        refresh: refreshGlobalOptions,
        setData: setGlobalOptions,
      },
    },
  } = useSuperAdminData();
  const optionLabels = {
    functions: "função",
    competences: "competência",
    geoAreas: "área geográfica",
    activitySectors: "setor de atividade",
  };
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
  const [optionModal, setOptionModal] = useState({ open: false, type: null, name: "", saving: false });
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
      setOptionsError(err.message || "Nao foi possivel carregar as opcoes globais.");
    } finally {
      if (mountedRef.current) setOptionsLoading(false);
    }
  }, [globalOptionsLoading, globalOptionsLoaded, refreshGlobalOptions, handleUnauthorized]);

  useEffect(() => {
    if (activeView === "globalVars" && !globalOptionsLoaded) {
      loadGlobalOptions();
    }
  }, [activeView, globalOptionsLoaded, loadGlobalOptions]);

  const effectiveGlobalOptionsError = globalOptionsErrorDismissed ? null : globalOptionsError;
  const combinedOptionsError = optionsError || effectiveGlobalOptionsError;
  const isGlobalOptionsLoading = optionsLoading || globalOptionsLoading;

  const handleOptionsErrorClose = () => {
    if (optionsError) {
      setOptionsError(null);
    } else {
      setGlobalOptionsErrorDismissed(true);
    }
  };

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

  const handleHeroFieldChange = (field, value) => {
    setHeroForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleHeroSubmit = async (event) => {
    event.preventDefault();
    if (!heroSection || !heroForm) return;
    setSavingHero(true);
    setBanner(null);
    try {
      const updated = await updateSection(heroSection.id, heroForm);
      setConfig((prev) => ({
        ...prev,
        sections: prev.sections.map((section) =>
          section.id === updated.id ? updated : section
        ),
      }));
      setBanner({ type: "success", message: "Hero atualizado com sucesso." });
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "Não foi possível guardar o hero.",
      });
    } finally {
      setSavingHero(false);
    }
  };

  const handleSectionMove = async (id, direction) => {
    if (!config) return;
    const next = moveItemInList(config.sections, id, direction);
    if (!next) return;

    const previous = config.sections;
    setConfig((prev) => ({ ...prev, sections: next }));
    setBanner(null);

    try {
      await reorderSections(next.map((section) => section.id));
      setBanner({ type: "success", message: "Ordem das secções atualizada." });
    } catch (err) {
      setConfig((prev) => ({ ...prev, sections: previous }));
      setBanner({
        type: "error",
        message: err.message || "Não foi possível reordenar as secções.",
      });
    }
  };

  const handleSectionToggle = async (section) => {
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
        message: `SecÃ§Ã£o ${updated.active ? "ativada" : "ocultada"} com sucesso.`,
      });
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "Não foi possível atualizar a secção.",
      });
    }
  };

  const handleIndustryMove = async (id, direction) =>
    handleReorderList("industries", reorderIndustries, id, direction);
  const handlePartnerMove = async (id, direction) =>
    handleReorderList("partners", reorderPartners, id, direction);

  const handleReorderList = async (key, apiFn, id, direction) => {
    const list = config?.[key];
    if (!Array.isArray(list)) return;

    const next = moveItemInList(list, id, direction);
    if (!next) return;

    const previous = list;
    setConfig((prev) => ({ ...prev, [key]: next }));
    setBanner(null);
    try {
      await apiFn(next.map((item) => item.id));
      setBanner({ type: "success", message: "Ordenação atualizada." });
    } catch (err) {
      setConfig((prev) => ({ ...prev, [key]: previous }));
      setBanner({
        type: "error",
        message: err.message || "Não foi possível reordenar a lista.",
      });
    }
  };

  const handleDeleteIndustry = async (record, { confirmDeletion = true } = {}) => {
    if (!record) return false;
    if (confirmDeletion && !window.confirm(`Eliminar a indústria "${record.name}"?`)) {
      return false;
    }
    setBanner(null);
    try {
      await deleteIndustry(record.id);
      setConfig((prev) => ({
        ...prev,
        industries: prev.industries.filter((item) => item.id !== record.id),
      }));
      setBanner({ type: "success", message: "Indústria removida com sucesso." });
      return true;
    } catch (err) {
      if (err?.status === 401) {
        handleUnauthorized();
        return false;
      }
      setBanner({
        type: "error",
        message: err.message || "Não foi possível eliminar a indústria.",
      });
      return false;
    }
  };

  const handleDeletePartner = async (record, { confirmDeletion = true } = {}) => {
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
        message: err.message || "Não foi possível eliminar o parceiro.",
      });
      return false;
    }
  };

  const openOptionModal = (type) => {
    setOptionModal({ open: true, type, name: "", saving: false });
  };


  const closeOptionModal = () => {
    setOptionModal({ open: false, type: null, name: "", saving: false });
  };

  const handleOptionSubmit = async (event) => {
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
      setOptionsError(err.message || "Nao foi possivel criar o registo.");
      setOptionModal((prev) => ({ ...prev, saving: false }));
    }
  };

  const handleDeleteOption = async (type, record) => {
    if (!type || !record) return;
    // A confirmacao real e feita via modal de password.
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
  };

  const createOption = (type, name) => {
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
  };

  const deleteOption = (type, id) => {
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
  };

  const openDeleteModal = (type, record) => {
    setDeleteModal({
      open: true,
      type,
      record,
      password: "",
      saving: false,
      error: null,
    });
  };

  const closeDeleteModal = () => {
    setDeleteModal({
      open: false,
      type: null,
      record: null,
      password: "",
      saving: false,
      error: null,
    });
  };

  const confirmDeleteOption = async (event) => {
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
        error: err.message || "Nao foi possivel eliminar o registo.",
      }));
    }
  };

  const openModal = (entity, record = null) => {
    const mode = record ? "edit" : "create";
    setModalState({ open: true, entity, mode, record });
    setModalForm(getModalForm(entity, record));
  };

  const closeModal = () => {
    setModalState({ open: false, entity: null, mode: "create", record: null });
    setModalForm(null);
    setModalSaving(false);
  };

  const handleModalFieldChange = (field, value) => {
    setModalForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleModalSubmit = async (event) => {
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
      setBanner({ type: "success", message: "Conteúdo guardado com sucesso." });
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "Não foi possível guardar os dados.",
      });
      setModalSaving(false);
    }
  };

  const handleModalDelete = async () => {
    if (modalState.mode !== "edit" || !modalState.record) return;
    const fn = modalState.entity === "industry" ? handleDeleteIndustry : handleDeletePartner;
    const success = await fn(modalState.record);
    if (success) {
      closeModal();
    }
  };

  const renderContent = () => {
    if (loading) {
      return (
        <div className="flex min-h-[50vh] items-center justify-center">
          <span className="loading loading-spinner loading-lg text-primary" />
        </div>
      );
    }

    if (loadError && !config) {
      return (
        <div className="flex flex-col items-center gap-4 py-20">
          <p className="text-lg text-base-content/70">{loadError}</p>
          <button type="button" className="btn btn-primary" onClick={retryHomepageConfig}>
            Tentar novamente
          </button>
        </div>
      );
    }

    if (!config) return null;

    if (activeView === "appHome") {
      return <AppHomeManager onUnauthorized={handleUnauthorized} />;
    }

    if (activeView === "weeklyTips") {
      return <WeeklyTipsManager onUnauthorized={handleUnauthorized} />;
    }

    if (activeView === "globalVars") {
      return renderGlobalVariables();
    }

    if (activeView === "otherPages") {
      return (
        <TabPlaceholder
          title="Outras paginas"
          description="Gestao futura de paginas adicionais. Em breve."
        />
      );
    }

    return renderPublicHome();
  };

  function renderGlobalVariables() {
    return (
      <div className="space-y-10 pt-4">
        {combinedOptionsError && (
          <div className="alert alert-error shadow flex justify-between">
            <span>{combinedOptionsError}</span>
            <button
              type="button"
              className="btn btn-ghost btn-xs"
              onClick={handleOptionsErrorClose}
            >
              Fechar
            </button>
          </div>
        )}


          <div className="space-y-6">
            {isGlobalOptionsLoading ? (
              <div className="flex min-h-[200px] items-center justify-center">
                <span className="loading loading-spinner loading-lg text-primary" />
              </div>
            ) : (
              <div className="space-y-6">
                <section className="space-y-3 card bg-base-100 shadow-lg p-4">
                  <div className="flex items-center gap-2">
                    <h3 className="text-2xl font-semibold">Funcionário</h3>
                    <span className="badge badge-ghost text-xs">Funções, Competências, Áreas Geográficas</span>
                  </div>
                  <div className="space-y-3">
                    {renderOptionCard("functions", "Funções", true)}
                    {renderOptionCard("competences", "Competências", true)}
                    {renderOptionCard("geoAreas", "Áreas geográficas", true)}
                  </div>
                </section>
                <section className="space-y-3 card bg-base-100 shadow-lg p-4">
                  <div className="flex items-center gap-2">
                    <h3 className="text-2xl font-semibold">Empresa</h3>
                    <span className="badge badge-ghost text-xs">Setores de atividade</span>
                  </div>
                  <div className="space-y-3">
                    {renderOptionCard("activitySectors", "Setores de atividade", true)}
                  </div>
                </section>
              </div>
            )}
          </div>

        {manageModal.open && (
          <Modal
            open
            title={`Gerir ${manageTitle(manageModal.type)}`}
            onClose={() => setManageModal({ open: false, type: null, search: "" })}
            actions={
              <div className="flex justify-center w-full mt-4">
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={() => openOptionModal(manageModal.type)}
                >
                  Adicionar
                </button>
              </div>
            }
          >
            <div className="space-y-5 min-h-[360px]">
              <div className="flex flex-wrap items-center gap-3">
                <span className="label-text font-semibold">Pesquisar</span>
                <input
                  type="text"
                  className="input input-bordered flex-1 min-w-[220px]"
                  placeholder="Digite para filtrar"
                  value={manageModal.search}
                  onChange={(e) =>
                    setManageModal((prev) => ({ ...prev, search: e.target.value }))
                  }
                />
              </div>
              <div className="max-h-[320px] overflow-auto space-y-2 pr-1">
                {filteredManageItems(manageModal.type, globalOptions, manageModal.search).map((item) => (
                  <div
                    key={item.id}
                    className="flex items-center justify-between rounded-xl border border-base-200 bg-base-100 px-3 py-2"
                  >
                    <span className="font-medium">{item.name}</span>
                    <button
                      type="button"
                      className="btn btn-xs btn-outline btn-error btn-square transition-all duration-150 hover:scale-105"
                      title="Apagar"
                      onClick={() => openDeleteModal(manageModal.type, item)}
                    >
                      <i className="bi bi-x-lg text-error" />
                    </button>
                  </div>
                ))}
                {filteredManageItems(manageModal.type, globalOptions, manageModal.search).length === 0 && (
                  <p className="text-sm text-base-content/60 text-center py-6">
                    Nenhum item encontrado.
                  </p>
                )}
              </div>
              <div className="pt-1" />
            </div>
          </Modal>
        )}

        {optionModal.open && (
          <Modal
            open
            title={`Adicionar ${optionLabels[optionModal.type] || "item"}`}
            onClose={closeOptionModal}
            actions={
              <>
                <button type="button" className="btn btn-ghost" onClick={closeOptionModal}>
                  Cancelar
                </button>
                <button
                  type="submit"
                  form="function-form"
                  className="btn btn-primary"
                  disabled={optionModal.saving}
                >
                  {optionModal.saving ? (
                    <>
                      <span className="loading loading-spinner loading-sm" />
                      A guardar!
                    </>
                  ) : (
                    "Adicionar"
                  )}
                </button>
              </>
            }
          >
            <form id="function-form" className="space-y-4" onSubmit={handleOptionSubmit}>
              <div className="form-control w-full">
                <label htmlFor="option-name" className="label-text font-semibold mb-1">
                  Nome da {optionLabels[optionModal.type] || "opção"}
                </label>
                <input
                  id="option-name"
                  type="text"
                  className="input input-bordered w-full"
                  value={optionModal.name}
                  onChange={(e) => setOptionModal((prev) => ({ ...prev, name: e.target.value }))}
                  placeholder="Ex.: Soldador"
                  required
                />
              </div>
              <p className="text-sm text-base-content/60">
                Insira o nome exatamente como deseja que apareca para os utilizadores.
              </p>
            </form>
          </Modal>
        )}

        {deleteModal.open && (
          <Modal
            open
            title="Confirmar apagamento"
            onClose={closeDeleteModal}
            actions={
              <>
                <button type="button" className="btn btn-ghost" onClick={closeDeleteModal} disabled={deleteModal.saving}>
                  Cancelar
                </button>
                <button
                  type="submit"
                  form="delete-form"
                  className="btn btn-outline btn-error"
                  disabled={deleteModal.saving}
                >
                  {deleteModal.saving ? (
                    <>
                      <span className="loading loading-spinner loading-sm" />
                      A apagar...
                    </>
                  ) : (
                    "Apagar"
                  )}
                </button>
              </>
            }
          >
            <form id="delete-form" className="space-y-4" onSubmit={confirmDeleteOption}>
              <p className="text-base-content/80">
                Para apagar <strong>{deleteModal.record?.name}</strong>, digite a password do super admin.
              </p>
              <label className="form-control">
                <span className="label-text font-semibold">Password do super admin</span>
                <input
                  type="password"
                  className="input input-bordered"
                  value={deleteModal.password}
                  onChange={(e) => setDeleteModal((prev) => ({ ...prev, password: e.target.value }))}
                  required
                />
              </label>
              {deleteModal.error && <p className="text-sm text-error">{deleteModal.error}</p>}
            </form>
          </Modal>
        )}
      </div>
    );
  }

function renderOptionCard(type, title, fullWidth = false) {
  return (
    <div className={`rounded-2xl border border-base-300 bg-base-100 p-4 space-y-3 shadow-sm ${fullWidth ? "w-full" : ""}`}>
      <div className="flex items-center justify-between gap-3">
        <div>
          <h3 className="text-xl font-semibold">{title}</h3>
          <p className="text-base text-base-content/70">Gerir {title.toLowerCase()} disponiveis.</p>
        </div>
        <button
          type="button"
          className="btn btn-sm btn-primary"
          onClick={() => setManageModal({ open: true, type, search: "" })}
        >
          Gerir
        </button>
      </div>
    </div>
  );
}

  function renderPublicHome() {
    return (
      <div className="space-y-10">
        {banner && (
          <div
            className={`alert ${
              banner.type === "error" ? "alert-error" : "alert-success"
            } shadow flex justify-between`}
          >
            <span>{banner.message}</span>
            <button
              type="button"
              className="btn btn-ghost btn-xs"
              onClick={() => setBanner(null)}
            >
              Fechar
            </button>
          </div>
        )}

        <div className="card bg-base-100 shadow-xl">
          <div className="card-body space-y-6">
            <div>
              <p className="text-sm uppercase tracking-[0.3em] text-primary/70">
              </p>
              <h2 className="card-title text-3xl">Hero e Redirecionamento</h2>
              <p className="text-base-content/70">
              </p>
            </div>
            {heroForm && (
              <form className="grid gap-4 md:grid-cols-2" onSubmit={handleHeroSubmit}>
                <label className="form-control md:col-span-2">
                  <span className="label-text font-semibold">Titulo</span>
                  <input
                    type="text"
                    className="input input-bordered"
                    required
                    value={heroForm.title}
                    onChange={(e) => handleHeroFieldChange("title", e.target.value)}
                  />
                </label>

                <label className="form-control md:col-span-2">
                  <span className="label-text font-semibold">Subtitulo</span>
                  <textarea
                    className="textarea textarea-bordered min-h-[120px]"
                    value={heroForm.subtitle}
                    onChange={(e) => handleHeroFieldChange("subtitle", e.target.value)}
                  />
                </label>

                <FieldGroup
                  label="Texto do botao"
                  value={heroForm.primaryCtaLabel}
                  onChange={(value) => handleHeroFieldChange("primaryCtaLabel", value)}
                  placeholder="Ex.: Quero trabalhar"
                />
                <FieldGroup
                  label="URL do botao principal"
                  value={heroForm.primaryCtaUrl}
                  onChange={(value) => handleHeroFieldChange("primaryCtaUrl", value)}
                  placeholder="/login"
                />
                <FieldGroup
                  label="Texto do botao"
                  value={heroForm.secondaryCtaLabel}
                  onChange={(value) =>
                    handleHeroFieldChange("secondaryCtaLabel", value)
                  }
                  placeholder="Sou empresa"
                />
                <FieldGroup
                  label="URL do botao secundario"
                  value={heroForm.secondaryCtaUrl}
                  onChange={(value) => handleHeroFieldChange("secondaryCtaUrl", value)}
                  placeholder="/company-register"
                />

                <label className="label cursor-pointer md:col-span-2 justify-start gap-3">
                  <input
                    type="checkbox"
                    className="toggle toggle-primary"
                    checked={heroForm.active}
                    onChange={(e) => handleHeroFieldChange("active", e.target.checked)}
                  />
                  <span className="label-text">Mostrar esta secao no site publico </span>
                </label>

                <div className="md:col-span-2 flex gap-3 justify-end">
                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={savingHero}
                  >
                    {savingHero ? (
                      <>
                        <span className="loading loading-spinner loading-sm" />
                        A guardar!
                      </>
                    ) : (
                      "Guardar alterações"
                    )}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>

        <SectionOrderCard
          sections={config.sections}
          onMove={handleSectionMove}
          onToggle={handleSectionToggle}
        />

        <ShowcaseList
          title="Industrias em destaque"
          description="Controle quais setores aparecem e a ordem apresentada na home page."
          type="industry"
          items={config.industries}
          onCreate={() => openModal("industry")}
          onEdit={(record) => openModal("industry", record)}
          onMove={handleIndustryMove}
        />

        <ShowcaseList
          title="Parceiros principais"
          description="Atualize os parceiros apresentados e o destaque dado a cada um."
          type="partner"
          items={config.partners}
          onCreate={() => openModal("partner")}
          onEdit={(record) => openModal("partner", record)}
          onMove={handlePartnerMove}
        />
      </div>
    );
  }

  return (
    <section className="space-y-12 pt-8">
      <header>
        <p className="text-sm uppercase tracking-[0.35em] text-primary/80">
          Gestão do site
        </p>
        <h1 className="text-4xl font-extrabold text-primary">Configurações do site</h1>
      </header>

      <nav className="tabs tabs-boxed bg-base-100 shadow-sm w-full md:w-fit">
        {VIEW_TABS.map((tab) => (
          <button
            key={tab.id}
            type="button"
            className={`tab ${activeView === tab.id ? "tab-active" : ""}`}
            onClick={() => setActiveView(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {renderContent()}

      <ShowcaseModal
        state={modalState}
        form={modalForm}
        saving={modalSaving}
        onClose={closeModal}
        onChange={handleModalFieldChange}
        onSubmit={handleModalSubmit}
        onDelete={handleModalDelete}
      />
    </section>
  );
}

function TabPlaceholder({ title, description }) {
  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-2">
        <h2 className="card-title text-2xl">{title}</h2>
        <p className="text-base-content/70">{description}</p>
      </div>
    </div>
  );
}

function FieldGroup({ label, value, onChange, placeholder }) {
  return (
    <label className="form-control">
      <span className="label-text font-semibold">{label}</span>
      <input
        type="text"
        className="input input-bordered"
        value={value}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
      />
    </label>
  );
}

function SectionOrderCard({ sections = [], onMove, onToggle }) {
  const [dragId, setDragId] = useState(null);
  const [dragIndex, setDragIndex] = useState(null);
  const [overIndex, setOverIndex] = useState(null);
  const [clickedId, setClickedId] = useState(null);

  const handleDragStart = (sectionId, index) => {
    setDragId(sectionId);
    setDragIndex(index);
  };

  const handleDragOver = (event, index) => {
    event.preventDefault();
    if (overIndex !== index) setOverIndex(index);
  };

  const handleDrop = async (index) => {
    if (
      dragId == null ||
      dragIndex == null ||
      dragIndex === index ||
      typeof onMove !== "function"
    ) {
      setDragId(null);
      setDragIndex(null);
      setOverIndex(null);
      return;
    }

    const direction = dragIndex < index ? "down" : "up";
    const steps = Math.abs(index - dragIndex);

    // usa a lógica existente de mover secções
    for (let i = 0; i < steps; i += 1) {
      // eslint-disable-next-line no-await-in-loop
      await onMove(dragId, direction);
    }

    setDragId(null);
    setDragIndex(null);
    setOverIndex(null);
  };

  const handleDragEnd = () => {
    setDragId(null);
    setDragIndex(null);
    setOverIndex(null);
  };

  const handleArrowClick = async (sectionId, direction) => {
    if (typeof onMove !== "function") return;
    setClickedId(sectionId);
    try {
      await onMove(sectionId, direction);
    } finally {
      setTimeout(() => {
        setClickedId((current) => (current === sectionId ? null : current));
      }, 180);
    }
  };

  const itemClasses = (index, sectionId) => {
    let extra = "transition-transform duration-150 ease-out";

    if (dragId === sectionId) {
      extra += " ring-2 ring-primary/70 shadow-lg scale-[1.02]";
    } else if (clickedId === sectionId) {
      extra += " ring-2 ring-primary/60 shadow-md scale-[1.01]";
    } else if (overIndex === index && dragId != null) {
      extra += " bg-base-200/80";
    }

    return `flex items-center justify-between gap-4 rounded-2xl border border-base-300 bg-base-100 px-4 py-3 ${extra}`;
  };

  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-4">
        <div className="flex flex-col gap-1">
          <h2 className="card-title text-2xl">Ordem das secções</h2>
          <p className="text-base-content/70">
            Defina a sequência com que cada bloco aparece para os visitantes.
          </p>
        </div>
        <ol className="space-y-3">
          {sections.map((section, index) => (
            <li
              key={section.id}
              draggable
              onDragStart={() => handleDragStart(section.id, index)}
              onDragOver={(event) => handleDragOver(event, index)}
              onDrop={() => handleDrop(index)}
              onDragEnd={handleDragEnd}
              className={itemClasses(index, section.id)}
            >
              <div className="flex items-center gap-3">
                <span className="font-semibold text-primary cursor-grab select-none">
                  {index + 1}.
                </span>
                <div>
                  <p className="font-semibold">
                    {SECTION_LABELS[section.type] ?? section.type}
                  </p>
                  <p className="text-sm text-base-content/70">{section.title}</p>
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-3">
                {typeof onToggle === "function" && (
                  <label className="label cursor-pointer gap-3">
                    <span className="text-sm text-base-content/70">
                      {section.active ? "Visível" : "Oculta"}
                    </span>
                    <input
                      type="checkbox"
                      className="toggle toggle-primary"
                      checked={section.active}
                      onChange={() => onToggle(section)}
                    />
                  </label>
                )}
                <button
                  type="button"
                  className="btn btn-sm btn-ghost"
                  onClick={() => handleArrowClick(section.id, "up")}
                  disabled={index === 0}
                >
                  <i className="bi bi-arrow-up" />
                </button>
                <button
                  type="button"
                  className="btn btn-sm btn-ghost"
                  onClick={() => handleArrowClick(section.id, "down")}
                  disabled={index === sections.length - 1}
                >
                  <i className="bi bi-arrow-down" />
                </button>
              </div>
            </li>
          ))}
        </ol>
      </div>
    </div>
  );
}

function ShowcaseList({
  title,
  description,
  items,
  onCreate,
  onEdit,
  onMove,
  type,
}) {
  const isIndustry = type === "industry";
  const safeItems = Array.isArray(items) ? items : [];
  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 className="card-title text-2xl">{title}</h2>
            <p className="text-base-content/70">{description}</p>
          </div>
          <button type="button" className="btn btn-primary" onClick={onCreate}>
            Adicionar {isIndustry ? "industria" : "parceiro"}
          </button>
        </div>
        {safeItems.length ? (
          <div className="space-y-4">
            {safeItems.map((item, index) => (
              <article
                key={item.id}
                className="rounded-2xl border border-base-200 p-5 flex flex-col gap-4"
              >
                <div className="flex flex-col gap-4 md:flex-row md:items-center md:gap-6">
                  <div className="w-full md:w-48 h-32 rounded-2xl overflow-hidden bg-base-200 border border-base-300">
                    <ShowcasePreview src={item.imageUrl} alt={item.name} />
                  </div>
                  <div className="flex-1 space-y-2">
                    <div className="flex flex-wrap items-center gap-3">
                      <h3 className="text-xl font-semibold">{item.name}</h3>
                      <span
                        className={`badge ${
                          item.active ? "badge-success" : "badge-ghost"
                        } uppercase`}
                      >
                        {item.active ? "Visi­vel" : "Oculto"}
                      </span>
                      <div className="flex items-center gap-2 text-xs text-base-content/60">
                        <button
                          type="button"
                          className="btn btn-xs btn-ghost"
                          onClick={() => onMove(item.id, "up")}
                          disabled={index === 0}
                          aria-label="Subir item"
                        >
                          <i className="bi bi-arrow-up" />
                        </button>
                        <button
                          type="button"
                          className="btn btn-xs btn-ghost"
                          onClick={() => onMove(item.id, "down")}
                          disabled={index === safeItems.length - 1}
                          aria-label="Descer item"
                        >
                          <i className="bi bi-arrow-down" />
                        </button>
                      </div>
                    </div>
                    {item.description && (
                      <p className="text-sm text-base-content/70 leading-relaxed">
                        {item.description}
                      </p>
                    )}
                  </div>
                </div>
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      className="btn btn-sm btn-outline"
                      onClick={() => onEdit(item)}
                    >
                      Editar
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="rounded-2xl border border-dashed border-base-300 p-10 text-center">
            <p className="text-base-content/60">
              Ainda não existem {isIndustry ? "indÃºstrias" : "parceiros"} configurados.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

function ShowcaseModal({ state, form, saving, onClose, onChange, onSubmit, onDelete }) {
  const [uploadingImage, setUploadingImage] = useState(false);
  const [uploadError, setUploadError] = useState(null);
  if (!state.open || !form) return null;
  const isIndustry = state.entity === "industry";

  const handleImageUpload = async (file) => {
    if (!file) return;
    setUploadError(null);
    setUploadingImage(true);
    try {
      const result = await uploadSiteImage(file);
      onChange("imageUrl", result.url);
    } catch (err) {
      setUploadError(err.message || "Não foi possivel carregar a imagem.");
    } finally {
      setUploadingImage(false);
    }
  };

  return (
    <Modal
      open
      title={
        state.mode === "create"
          ? `Adicionar ${isIndustry ? "industria" : "parceiro"}`
          : `Editar ${isIndustry ? "industria" : "parceiro"}`
      }
      onClose={onClose}
      actions={
        <>
          {state.mode === "edit" && (
            <button
              type="button"
              className="btn btn-error btn-outline mr-auto"
              onClick={onDelete}
              disabled={saving}
            >
              Eliminar
            </button>
          )}
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Cancelar
          </button>
          <button
            type="submit"
            form="showcase-form"
            className="btn btn-primary"
            disabled={saving}
          >
            {saving ? (
              <>
                <span className="loading loading-spinner loading-sm" />
                A guardar!
              </>
            ) : (
              "Guardar"
            )}
          </button>
        </>
      }
    >
      <form id="showcase-form" className="space-y-5" onSubmit={onSubmit}>
        <div className="grid gap-4 md:grid-cols-2">
          <label className="form-control">
            <span className="label-text font-semibold">Nome</span>
            <input
              type="text"
              className="input input-bordered"
              required
              value={form.name}
              onChange={(e) => onChange("name", e.target.value)}
            />
          </label>
          <div className="rounded-2xl border border-base-300 bg-base-100/80 px-4 py-3 flex items-center justify-between gap-4">
            <div>
              <p className="text-sm font-semibold text-base-content/80">Visibilidade</p>
              <p className="text-xs text-base-content/60">
                Controle se aparece na paigina inicial
              </p>
            </div>
            <label className="label cursor-pointer gap-3">
              <span className="text-sm">{form.active ? "Online" : "Oculto"}</span>
              <input
                type="checkbox"
                className="toggle toggle-primary"
                checked={form.active}
                onChange={(e) => onChange("active", e.target.checked)}
              />
            </label>
          </div>
        </div>

        {!isIndustry && (
          <label className="form-control">
            <span className="label-text font-semibold">Descrição</span>
            <textarea
              className="textarea textarea-bordered min-h-[120px]"
              required
              value={form.description}
              onChange={(e) => onChange("description", e.target.value)}
            />
          </label>
        )}

        <div className="rounded-3xl border border-dashed border-base-300 bg-base-100/80 p-4 space-y-4">
          <div className="flex flex-col gap-4 md:flex-row">
            <div className="flex-1 space-y-3">
              <DropZone
                label="Imagem (arraste ou clique para carregar)"
                onSelect={handleImageUpload}
              />
              {uploadingImage && (
                <div className="text-sm text-primary flex items-center gap-2">
                  <span className="loading loading-spinner loading-xs" />
                  A enviar imagem!
                </div>
              )}
              {uploadError && <p className="text-sm text-error">{uploadError}</p>}
              <label className="form-control">
                <span className="label-text text-sm text-base-content/70">
                  Ou cole um URL de imagem
                </span>
                <input
                  type="text"
                  className="input input-bordered"
                  required
                  value={form.imageUrl}
                  onChange={(e) => onChange("imageUrl", e.target.value)}
                  placeholder="https://..."
                />
              </label>
            </div>
            <div className="flex-1 rounded-2xl border border-base-300 bg-base-200 h-48 overflow-hidden">
              <ShowcasePreview src={form.imageUrl} alt={form.name} />
            </div>
          </div>
        </div>

        {isIndustry ? (
          <>
            <input type="hidden" value={form.description ?? ""} readOnly />
            <input type="hidden" value={form.linkUrl ?? ""} readOnly />
          </>
        ) : (
          <input type="hidden" value={form.websiteUrl ?? ""} readOnly />
        )}
      </form>
    </Modal>
  );
}

function manageTitle(type) {
  switch (type) {
    case "functions":
      return "Funções";
    case "competences":
      return "Competências";
    case "geoAreas":
      return "Áreas geográficas";
    case "activitySectors":
      return "Setores de atividade";
    default:
      return "Itens";
  }
}

function filteredManageItems(type, options, search) {
  const list = (options && options[type]) || [];
  const query = (search || "").toLowerCase().trim();
  if (!query) return list;
  return list.filter((item) => (item.name || "").toLowerCase().includes(query));
}

function getModalForm(entity, record) {
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
}

function ShowcasePreview({ src, alt }) {
  if (!src) {
    return (
      <div className="h-full w-full flex items-center justify-center bg-base-200 text-base-content/40 text-xs">
        Sem imagem
      </div>
    );
  }
  return <img src={src} alt={alt} className="h-full w-full object-cover" loading="lazy" />;
}

