import { useEffect, useRef, useState } from "react";
import {
  updateAppHomeSection,
  reorderAppHomeSections,
  createAppMetric,
  updateAppMetric,
  deleteAppMetric,
  toggleAppMetric,
  reorderAppMetrics,
} from "../../../api/site/siteManagement.js";
import Modal from "/src/components/ui/Modal/Modal.jsx";
import { useSuperAdminData } from "./SuperAdminDataContext.jsx";
import { moveItemInList, sortByOrder } from "./VariableManagement/utils.js";

const APP_SECTION_TYPES = {
  hero: "HERO",
  weeklyTip: "WEEKLY_TIP",
  news: "NEWS",
};

const SECTION_LABELS = {
  HERO: "Hero autenticado",
  METRICS: "Métricas",
  WEEKLY_TIP: "Dica da semana",
  NEWS: "Noticias recentes",
};

export default function AppHomeManager({ onUnauthorized }) {
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
  const [metricModal, setMetricModal] = useState({ open: false, mode: "create", record: null });
  const [metricForm, setMetricForm] = useState(defaultMetricForm());
  const [metricSaving, setMetricSaving] = useState(false);
  const initialLoad = useRef(false);
  useEffect(() => {
    if (loaded || initialLoad.current) return;
    initialLoad.current = true;
    refreshAppHome().catch((err) => {
      if (err?.status === 401) {
        onUnauthorized?.();
      }
    });
  }, [loaded, refreshAppHome, onUnauthorized]);

  const retryAppHomeConfig = () => {
    refreshAppHome({ force: true }).catch((err) => {
      if (err?.status === 401) {
        onUnauthorized?.();
      }
    });
  };

  useEffect(() => {
    if (!config) return;
    const sections = config.sections ?? [];
    setForms({
      hero: mapAppSectionForm(sections.find((section) => section.type === APP_SECTION_TYPES.hero)),
      weeklyTip: mapAppSectionForm(
        sections.find((section) => section.type === APP_SECTION_TYPES.weeklyTip)
      ),
      news: mapAppSectionForm(sections.find((section) => section.type === APP_SECTION_TYPES.news)),
    });
  }, [config]);

  const sections = config?.sections ?? [];
  const metrics = config?.metrics ?? [];

  const handleSectionFieldChange = (key, field, value) => {
    setForms((prev) => ({
      ...prev,
      [key]: { ...prev[key], [field]: value },
    }));
  };

  const handleSectionSubmit = async (event, key) => {
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
      setBanner({ type: "success", message: "Secção guardada com sucesso." });
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "Não foi possível guardar a secção.",
      });
    } finally {
      setSavingSections((prev) => ({ ...prev, [key]: false }));
    }
  };

  const handleSectionMove = async (id, direction) => {
    const next = moveItemInList(sections, id, direction);
    if (!next) return;
    const previous = sections;
    setAppHomeConfig((prev) => ({ ...prev, sections: next }));
    setBanner(null);
    try {
      await reorderAppHomeSections(next.map((section) => section.id));
      setBanner({ type: "success", message: "Ordem atualizada com sucesso." });
    } catch (err) {
      setAppHomeConfig((prev) => ({ ...prev, sections: previous }));
      setBanner({
        type: "error",
        message: err.message || "Não foi possível reordenar as secções.",
      });
    }
  };

  const handleSectionToggle = async (section) => {
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
      });
      setAppHomeConfig((prev) => ({
        ...prev,
        sections: prev.sections.map((item) => (item.id === updated.id ? updated : item)),
      }));
      setForms((prev) => ({ ...prev, [key]: mapAppSectionForm(updated) }));
      setBanner({ type: "success", message: "Visibilidade atualizada." });
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "Não foi possível atualizar a secção.",
      });
    }
  };

  const openMetricModal = (mode, record = null) => {
    setMetricModal({ open: true, mode, record });
    setMetricForm(record ? mapMetricForm(record) : defaultMetricForm());
    setBanner(null);
  };

  const closeMetricModal = () => {
    setMetricModal({ open: false, mode: "create", record: null });
    setMetricForm(defaultMetricForm());
  };

  const handleMetricFieldChange = (field, value) => {
    setMetricForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleMetricSubmit = async (event) => {
    event.preventDefault();
    setMetricSaving(true);
    setBanner(null);
    try {
      const payload = {
        label: metricForm.label,
        value: metricForm.value,
        description: metricForm.description,
        active: metricForm.active,
      };
      let result;
      if (metricModal.mode === "edit" && metricModal.record) {
        result = await updateAppMetric(metricModal.record.id, payload);
        setAppHomeConfig((prev) => ({
          ...prev,
          metrics: prev.metrics.map((item) => (item.id === result.id ? result : item)),
        }));
      } else {
        result = await createAppMetric(payload);
        setAppHomeConfig((prev) => ({
          ...prev,
          metrics: sortByOrder([...(prev.metrics ?? []), result]),
        }));
      }
      setBanner({ type: "success", message: "Métrica guardada com sucesso." });
      closeMetricModal();
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "Não foi possível guardar a métrica.",
      });
    } finally {
      setMetricSaving(false);
    }
  };

  const handleMetricDelete = async () => {
    if (metricModal.mode !== "edit" || !metricModal.record) return;
    setMetricSaving(true);
    setBanner(null);
    try {
      await deleteAppMetric(metricModal.record.id);
      setAppHomeConfig((prev) => ({
        ...prev,
        metrics: prev.metrics.filter((item) => item.id !== metricModal.record.id),
      }));
      setBanner({ type: "success", message: "MA©trica removida." });
      closeMetricModal();
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "Não foi possível remover a métrica.",
      });
    } finally {
      setMetricSaving(false);
    }
  };

  const handleMetricMove = async (id, direction) => {
    const next = moveItemInList(metrics, id, direction);
    if (!next) return;
    const previous = metrics;
    setAppHomeConfig((prev) => ({ ...prev, metrics: next }));
    setBanner(null);
    try {
      await reorderAppMetrics(next.map((metric) => metric.id));
      setBanner({ type: "success", message: "Métricas reordenadas." });
    } catch (err) {
      setAppHomeConfig((prev) => ({ ...prev, metrics: previous }));
      setBanner({
        type: "error",
        message: err.message || "Não foi possível reordenar as métricas.",
      });
    }
  };

  const handleMetricToggle = async (metric) => {
    setBanner(null);
    try {
      const updated = await toggleAppMetric(metric.id, !metric.active);
      setAppHomeConfig((prev) => ({
        ...prev,
        metrics: prev.metrics.map((item) => (item.id === updated.id ? updated : item)),
      }));
      setBanner({ type: "success", message: "Visibilidade atualizada." });
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "Não foi possí­vel alterar a métrica.",
      });
    }
  };

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
        <button type="button" className="btn btn-primary" onClick={retryAppHomeConfig}>
          Tentar novamente
        </button>
      </div>
    );
  }

  if (!config) return null;

  return (
    <div className="space-y-10">
      {banner && (
        <div
          className={`alert ${banner.type === "error" ? "alert-error" : "alert-success"} shadow flex justify-between`}
        >
          <span>{banner.message}</span>
          <button type="button" className="btn btn-ghost btn-xs" onClick={() => setBanner(null)}>
            Fechar
          </button>
        </div>
      )}

      <AppHeroSection
        form={forms.hero}
        saving={savingSections.hero}
        onFieldChange={(field, value) => handleSectionFieldChange("hero", field, value)}
        onSubmit={(event) => handleSectionSubmit(event, "hero")}
      />

      <AppWeeklyTipSection
        form={forms.weeklyTip}
        saving={savingSections.weeklyTip}
        onFieldChange={(field, value) => handleSectionFieldChange("weeklyTip", field, value)}
        onSubmit={(event) => handleSectionSubmit(event, "weeklyTip")}
      />

      <AppNewsSection
        form={forms.news}
        section={sections.find((sectionItem) => sectionItem.type === APP_SECTION_TYPES.news)}
        saving={savingSections.news}
        onFieldChange={(field, value) => handleSectionFieldChange("news", field, value)}
        onSubmit={(event) => handleSectionSubmit(event, "news")}
      />

      <AppSectionOrderCard
        sections={sections}
        onMove={handleSectionMove}
        onToggle={handleSectionToggle}
      />

      <AppMetricsList
        metrics={metrics}
        onCreate={() => openMetricModal("create")}
        onEdit={(metric) => openMetricModal("edit", metric)}
        onMove={handleMetricMove}
        onToggle={handleMetricToggle}
      />

      <MetricModal
        state={metricModal}
        form={metricForm}
        saving={metricSaving}
        onClose={closeMetricModal}
        onChange={handleMetricFieldChange}
        onSubmit={handleMetricSubmit}
        onDelete={handleMetricDelete}
      />
    </div>
  );
}

function AppHeroSection({ form, saving, onFieldChange, onSubmit }) {
  if (!form) return null;
  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-6">
        <div>
          <h2 className="card-title text-3xl">Hero</h2>
        </div>

        <form className="flex flex-col gap-6" onSubmit={onSubmit}>
          {/* Título – linha própria */}
          <FieldGroup
            label="Título"
            value={form.title}
            onChange={(value) => onFieldChange("title", value)}
            placeholder="Olá João!"
          />

          {/* Subtítulo – linha própria */}
          <label className="form-control">
            <span className="label-text font-semibold">Subtítulo</span>
            <textarea
              className="textarea textarea-bordered min-h-[120px]"
              value={form.subtitle}
              onChange={(e) => onFieldChange("subtitle", e.target.value)}
            />
          </label>

          {/* Conteúdo complementar – linha própria */}
          <label className="form-control">
            <span className="label-text font-semibold">Conteúdo complementar</span>
            <textarea
              className="textarea textarea-bordered min-h-[120px]"
              value={form.content}
              onChange={(e) => onFieldChange("content", e.target.value)}
            />
          </label>

          {/* Texto + URL do botão na MESMA linha */}
          <div className="flex flex-col gap-4 md:flex-row">
            <FieldGroup
              label="Texto do botão"
              value={form.primaryCtaLabel}
              onChange={(value) => onFieldChange("primaryCtaLabel", value)}
              placeholder="Atualizar perfil"
              className="flex-1"
            />
            <FieldGroup
              label="URL do botão"
              value={form.primaryCtaUrl}
              onChange={(value) => onFieldChange("primaryCtaUrl", value)}
              placeholder="/candidato/dados-pessoais"
              className="flex-1"
            />
          </div>

          {/* Slider de visibilidade – linha própria */}
          <label className="label cursor-pointer gap-3">
            <input
              type="checkbox"
              className="toggle toggle-primary"
              checked={form.active}
              onChange={(e) => onFieldChange("active", e.target.checked)}
            />
            <span className="label-text">Mostrar secção</span>
          </label>

          {/* Botão guardar – linha própria */}
          <div className="flex justify-end">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? (
                <>
                  <span className="loading loading-spinner loading-sm" />
                  A guardar...
                </>
              ) : (
                "Guardar alterações"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}


function AppWeeklyTipSection({ form, saving, onFieldChange, onSubmit }) {
  if (!form) return null;
  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-6">
        <div>
          <h2 className="card-title text-3xl">Dica da semana</h2>
        </div>
        <form className="space-y-4" onSubmit={onSubmit}>
          {/* Título da secção na home autenticada */}
          <FieldGroup
            label="Titulo"
            value={form.title}
            onChange={(value) => onFieldChange("title", value)}
            placeholder="Dica da semana"
          />

          {/* Texto + URL do botão na mesma linha */}
          <div className="flex flex-col gap-4 md:flex-row">
            <FieldGroup
              label="Texto do botão"
              value={form.primaryCtaLabel}
              onChange={(value) => onFieldChange("primaryCtaLabel", value)}
              placeholder="Ver mais"
              className="flex-1"
            />
            <FieldGroup
              label="URL do botão"
              value={form.primaryCtaUrl}
              onChange={(value) => onFieldChange("primaryCtaUrl", value)}
              placeholder="/dicas"
              className="flex-1"
            />
          </div>

          {/* Visibilidade da secção */}
          <label className="label cursor-pointer gap-3">
            <input
              type="checkbox"
              className="toggle toggle-primary"
              checked={form.active}
              onChange={(e) => onFieldChange("active", e.target.checked)}
            />
            <span className="label-text">Mostrar secção</span>
          </label>

          <div className="flex justify-end">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? (
                <>
                  <span className="loading loading-spinner loading-sm" />
                  A guardar...
                </>
              ) : (
                "Guardar alterações"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function AppNewsSection({ form, section, saving, onFieldChange, onSubmit }) {
  if (!form) return null;

  const selectedItems = Math.min(
    Math.max(Number(form.apiMaxItems || section?.apiMaxItems || 3), 1),
    6
  );
  const previewArticles = section?.newsArticles ?? [];
  const handleCountChange = (value) => {
    onFieldChange("apiMaxItems", String(value));
  };

  const formatPreviewDate = (value) => {
    if (!value) return "Data indisponivel";
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return "Data indisponivel";
    }
    return parsed.toLocaleDateString("pt-PT", { day: "2-digit", month: "short" });
  };

  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-6">
        <div>
          <h2 className="card-title text-3xl">Noticias da NewsAPI</h2>
          <p className="text-base-content/70">
            As manchetes sao sincronizadas automaticamente. Ajuste apenas quantos cards deseja
            mostrar (maximo de 6)
          </p>
        </div>
        <form className="space-y-6" onSubmit={onSubmit}>
          <div className="rounded-2xl border border-base-200 bg-base-100 p-4 space-y-3">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-base-content">Quantidade de notícias</span>
              <span className="text-sm text-base-content/70">
                {selectedItems} {selectedItems === 1 ? "noticia" : "noticias"}
              </span>
            </div>
            <input
              type="range"
              min="1"
              max="6"
              value={selectedItems}
              className="range range-primary"
              onChange={(event) => handleCountChange(event.target.value)}
            />
            <div className="flex justify-between text-xs text-base-content/70 px-1">
              <span>Min. 1</span>
              <span>Max. 6</span>
            </div>
          </div>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-base-content">Pre-visualização</span>
              <span className="badge badge-ghost">
                {previewArticles.length ? `${previewArticles.length} artigos` : "Sem dados"}
              </span>
            </div>
            {previewArticles.length ? (
              <div className="grid gap-3 md:grid-cols-2">
                {previewArticles.slice(0, 4).map((article) => (
                  <article
                    key={article.url}
                    className="rounded-2xl border border-base-300 bg-base-100 p-4 space-y-2"
                  >
                    <p className="text-xs uppercase tracking-wide text-primary/70">
                      {article.sourceName ?? "Fonte externa"}
                    </p>
                    <p className="font-semibold text-base-content line-clamp-2">{article.title}</p>
                    <p className="text-xs text-base-content/60">{formatPreviewDate(article.publishedAt)}</p>
                  </article>
                ))}
              </div>
            ) : (
              <div className="rounded-2xl border border-dashed border-base-300 bg-base-100 p-4 text-sm text-base-content/70">
                Ainda nao recebemos artigos da API. Guarde a configuracao e verifique se a chave NEWSAPI foi definida no backend.
              </div>
            )}
          </div>
          <div className="flex justify-end">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? (
                <>
                  <span className="loading loading-spinner loading-sm" />
                  A guardar...
                </>
              ) : (
                "Guardar alterações"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function AppSectionOrderCard({ sections, onMove, onToggle }) {
  const [dragId, setDragId] = useState(null);
  const [dragIndex, setDragIndex] = useState(null);
  const [overIndex, setOverIndex] = useState(null);
  const [clickedId, setClickedId] = useState(null);

  if (!sections.length) return null;

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
            Defina a sequência como cada bloco aparece para os utilizadores autenticados.
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
                  <p className="font-semibold">{SECTION_LABELS[section.type] ?? section.type}</p>
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

function AppMetricsList({ metrics, onCreate, onEdit, onMove, onToggle }) {
  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 className="card-title text-2xl">Métrcias destacadas</h2>
            <p className="text-base-content/70">Registe manualmente os números mostrados na Home.</p>
          </div>
          <button type="button" className="btn btn-primary" onClick={onCreate}>
            Adicionar métrica
          </button>
        </div>
        {metrics.length ? (
          <div className="space-y-4">
            {metrics.map((metric, index) => (
              <article
                key={metric.id}
                className="rounded-2xl border border-base-200 p-5 flex flex-col gap-4"
              >
                <div className="flex flex-wrap items-center justify-between gap-4">
                  <div>
                    <h3 className="text-xl font-semibold">{metric.label}</h3>
                    <p className="text-3xl font-bold text-primary">{metric.value}</p>
                    {metric.description && (
                      <p className="text-sm text-base-content/70 mt-1">{metric.description}</p>
                    )}
                  </div>
                  <span className={`badge ${metric.active ? "badge-success" : "badge-ghost"}`}>
                    {metric.active ? "Visí­vel" : "Oculta"}
                  </span>
                </div>
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      className="btn btn-sm btn-outline"
                      onClick={() => onEdit(metric)}
                    >
                      Editar
                    </button>
                    <button
                      type="button"
                      className="btn btn-sm btn-ghost"
                      onClick={() => onToggle(metric)}
                    >
                      {metric.active ? "Ocultar" : "Mostrar"}
                    </button>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-base-content/60">
                    <button
                      type="button"
                      className="btn btn-xs btn-ghost"
                      onClick={() => onMove(metric.id, "up")}
                      disabled={index === 0}
                    >
                      <i className="bi bi-arrow-up" />
                    </button>
                    <button
                      type="button"
                      className="btn btn-xs btn-ghost"
                      onClick={() => onMove(metric.id, "down")}
                      disabled={index === metrics.length - 1}
                    >
                      <i className="bi bi-arrow-down" />
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="rounded-2xl border border-dashed border-base-300 p-10 text-center">
            <p className="text-base-content/60">Ainda não existem métricas configuradas.</p>
          </div>
        )}
      </div>
    </div>
  );
}

function MetricModal({ state, form, saving, onClose, onChange, onSubmit, onDelete }) {
  if (!state.open) return null;
  const isEdit = state.mode === "edit";
  return (
    <Modal
      open
      title={isEdit ? "Editar métrica" : "Adicionar métrica"}
      onClose={onClose}
      actions={
        <>
          {isEdit && (
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
          <button type="submit" form="metric-form" className="btn btn-primary" disabled={saving}>
            {saving ? (
              <>
                <span className="loading loading-spinner loading-sm" />
                A guardar...
              </>
            ) : (
              "Guardar"
            )}
          </button>
        </>
      }
    >
      <form id="metric-form" className="space-y-4" onSubmit={onSubmit}>
        <FieldGroup
          label="Nome"
          value={form.label}
          onChange={(value) => onChange("label", value)}
          placeholder="Equipas concluA­das"
        />
        <FieldGroup
          label="Valor"
          value={form.value}
          onChange={(value) => onChange("value", value)}
          placeholder="8"
        />
        <label className="form-control">
          <span className="label-text font-semibold">Descrição</span>
          <textarea
            className="textarea textarea-bordered min-h-[100px]"
            value={form.description}
            onChange={(e) => onChange("description", e.target.value)}
          />
        </label>
        <label className="label cursor-pointer gap-3">
          <input
            type="checkbox"
            className="toggle toggle-primary"
            checked={form.active}
            onChange={(e) => onChange("active", e.target.checked)}
          />
          <span className="label-text">Mostrar na Home autenticada</span>
        </label>
      </form>
    </Modal>
  );
}

function FieldGroup({ label, value, onChange, placeholder, className }) {
  return (
    <label className={`form-control w-full${className ? ` ${className}` : ""}`}>
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
  };
}

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
  };
}

function defaultMetricForm() {
  return {
    label: "",
    value: "",
    description: "",
    active: true,
  };
}

function mapMetricForm(metric) {
  if (!metric) {
    return defaultMetricForm();
  }
  return {
    label: metric.label ?? "",
    value: metric.value ?? "",
    description: metric.description ?? "",
    active: Boolean(metric.active),
  };
}

function sectionKeyFromType(type) {
  return Object.entries(APP_SECTION_TYPES).find(([, value]) => value === type)?.[0] ?? null;
}
