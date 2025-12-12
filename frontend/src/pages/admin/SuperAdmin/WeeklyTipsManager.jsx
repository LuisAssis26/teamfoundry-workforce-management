import { useEffect, useRef, useState } from "react";
import {
  createWeeklyTip,
  updateWeeklyTip,
  deleteWeeklyTip,
  toggleWeeklyTipVisibility,
  markWeeklyTipFeatured,
  reorderWeeklyTips,
} from "../../../api/site/siteManagement.js";
import Modal from "/src/components/ui/Modal/Modal.jsx";
import { useSuperAdminData } from "./SuperAdminDataContext.jsx";
import { moveItemInList, sortWeeklyTips } from "./VariableManagement/utils.js";

const EMPTY_FORM = {
  category: "",
  title: "",
  description: "",
  publishedAt: new Date().toISOString().slice(0, 10),
  featured: false,
  active: true,
};

export default function WeeklyTipsManager({ onUnauthorized }) {
  const {
    site: {
      weeklyTips: {
        data: tips = [],
        loading,
        loaded,
        error: loadError,
        refresh: refreshWeeklyTips,
        setData: setTipsData,
      },
    },
  } = useSuperAdminData();
  const tipsList = Array.isArray(tips) ? tips : [];
  const [banner, setBanner] = useState(null);

  const [modalState, setModalState] = useState({
    open: false,
    mode: "create",
    record: null,
  });
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  const initialLoad = useRef(false);
  useEffect(() => {
    if (loaded || initialLoad.current) return;
    initialLoad.current = true;
    refreshWeeklyTips().catch((err) => {
      if (err?.status === 401) {
        onUnauthorized?.();
      }
    });
  }, [loaded, refreshWeeklyTips, onUnauthorized]);

  const retryWeeklyTips = () => {
    refreshWeeklyTips({ force: true }).catch((err) => {
      if (err?.status === 401) {
        onUnauthorized?.();
      }
    });
  };

  const openModal = (mode, record = null) => {
    setModalState({ open: true, mode, record });
    setForm(record ? mapForm(record) : { ...EMPTY_FORM });
    setBanner(null);
  };

  const closeModal = () => {
    setModalState({ open: false, mode: "create", record: null });
    setForm({ ...EMPTY_FORM });
  };

  const handleFieldChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setBanner(null);
    try {
      const payload = {
        category: form.category,
        title: form.title,
        description: form.description,
        publishedAt: form.publishedAt,
        featured: form.featured,
        active: form.active,
      };
      let result;
      if (modalState.mode === "edit" && modalState.record) {
        result = await updateWeeklyTip(modalState.record.id, payload);
        setTipsData((prev) => {
          const list = Array.isArray(prev) ? prev : [];
          return sortWeeklyTips(list.map((item) => (item.id === result.id ? result : item)));
        });
      } else {
        result = await createWeeklyTip(payload);
        setTipsData((prev) => sortWeeklyTips([...(prev ?? []), result]));
      }
      setBanner({ type: "success", message: "Dica guardada com sucesso." });
      closeModal();
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "N�o foi poss�vel guardar a dica.",
      });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (modalState.mode !== "edit" || !modalState.record) return;
    if (!window.confirm("Eliminar esta dica de forma permanente?")) return;
    setSaving(true);
    setBanner(null);
    try {
      await deleteWeeklyTip(modalState.record.id);
      setTipsData((prev) => {
        const list = Array.isArray(prev) ? prev : [];
        return list.filter((item) => item.id !== modalState.record.id);
      });
      setBanner({ type: "success", message: "Dica removida." });
      closeModal();
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "N�o foi poss�vel eliminar a dica.",
      });
    } finally {
      setSaving(false);
    }
  };

  const handleToggleVisibility = async (record) => {
    setBanner(null);
    try {
      const updated = await toggleWeeklyTipVisibility(record.id, !record.active);
      setTipsData((prev) => {
        const list = Array.isArray(prev) ? prev : [];
        return list.map((item) => (item.id === updated.id ? updated : item));
      });
      setBanner({
        type: "success",
        message: `Dica ${updated.active ? "ativada" : "ocultada"} com sucesso.`,
      });
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "N�o foi poss�vel atualizar a visibilidade.",
      });
    }
  };

  const handleMarkFeatured = async (record) => {
    setBanner(null);
    try {
      const updated = await markWeeklyTipFeatured(record.id);
      setTipsData((prev) => {
        const list = Array.isArray(prev) ? prev : [];
        return list.map((item) => ({
          ...item,
          featured: item.id === updated.id,
        }));
      });
      setBanner({ type: "success", message: "Dica marcada como destaque da semana." });
    } catch (err) {
      setBanner({
        type: "error",
        message: err.message || "N�o foi poss�vel definir a dica da semana.",
      });
    }
  };

  const handleMove = async (id, direction) => {
    const next = moveItemInList(tipsList, id, direction);
    if (!next) return;
    const previous = tipsList;
    setTipsData(next);
    setBanner(null);
    try {
      await reorderWeeklyTips(next.map((item) => item.id));
      setBanner({ type: "success", message: "Ordem das dicas atualizada." });
    } catch (err) {
      setTipsData(previous);
      setBanner({
        type: "error",
        message: err.message || "N�o foi poss�vel reordenar as dicas.",
      });
    }
  };

  return (
    <section className="space-y-6">
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
        <div className="card-body space-y-4">
          <div className="flex items-center justify-between gap-4">
            <div>
              <h2 className="card-title text-2xl">Dicas da semana</h2>
              <p className="text-sm text-base-content/70">
                Cria, organiza e escolhe qual a dica em destaque na home
                autenticada e na pagina de dicas.
              </p>
            </div>
            <button
              type="button"
              className="btn btn-primary btn-sm"
              onClick={() => openModal("create")}
            >
              Nova dica
            </button>
          </div>

          {loadError && !loading && (
            <div className="alert alert-warning">
              <span>{loadError}</span>
              <button
                type="button"
                className="btn btn-sm"
                onClick={retryWeeklyTips}
              >
                Tentar novamente
              </button>
            </div>
          )}

          {loading ? (
            <div className="flex items-center justify-center py-10">
              <span className="loading loading-spinner loading-lg text-primary" />
            </div>
          ) : tipsList.length === 0 ? (
            <p className="text-sm text-base-content/70">
              Ainda n�o existem dicas configuradas. Cria a primeira para a
              destacar aos utilizadores.
            </p>
          ) : (
            <TipsTable
              tips={tipsList}
              onEdit={(record) => openModal("edit", record)}
              onMove={handleMove}
              onToggle={handleToggleVisibility}
              onMarkFeatured={handleMarkFeatured}
            />
          )}
        </div>
      </div>

      <WeeklyTipModal
        state={modalState}
        form={form}
        saving={saving}
        onChange={handleFieldChange}
        onClose={closeModal}
        onSubmit={handleSubmit}
        onDelete={handleDelete}
      />
    </section>
  );
}

function TipsTable({ tips, onEdit, onMove, onToggle, onMarkFeatured }) {
  return (
    <div className="overflow-x-auto">
      <table className="table table-zebra">
        <thead>
          <tr className="text-xs uppercase tracking-wide text-base-content/60">
            <th />
            <th>Categoria</th>
            <th>Titulo</th>
            <th>Data</th>
            <th>Estado</th>
            <th>Destaque</th>
            <th className="text-right">Acoes</th>
          </tr>
        </thead>
        <tbody>
          {tips.map((tip, index) => (
            <tr key={tip.id}>
              <td className="w-0">
                <div className="flex flex-col gap-1">
                  <button
                    type="button"
                    className="btn btn-ghost btn-xs"
                    onClick={() => onMove(tip.id, "up")}
                    disabled={index === 0}
                  >
                    <i className="bi bi-arrow-up" aria-hidden="true" />
                  </button>
                  <button
                    type="button"
                    className="btn btn-ghost btn-xs"
                    onClick={() => onMove(tip.id, "down")}
                    disabled={index === tips.length - 1}
                  >
                    <i className="bi bi-arrow-down" aria-hidden="true" />
                  </button>
                </div>
              </td>
              <td className="whitespace-nowrap text-sm">{tip.category}</td>
              <td className="text-sm font-semibold text-base-content">
                {tip.title}
              </td>
              <td className="text-xs text-base-content/70">
                {formatDate(tip.publishedAt)}
              </td>
              <td>
                <span
                  className={`badge badge-sm ${
                    tip.active ? "badge-success" : "badge-ghost"
                  }`}
                >
                  {tip.active ? "Online" : "Oculta"}
                </span>
              </td>
              <td>
                {tip.featured ? (
                  <span className="badge badge-sm badge-primary">Dica da semana</span>
                ) : (
                  <button
                    type="button"
                    className="btn btn-ghost btn-xs"
                    onClick={() => onMarkFeatured(tip)}
                  >
                    Definir como destaque
                  </button>
                )}
              </td>
              <td className="text-right">
                <div className="flex justify-end gap-2">
                  <button
                    type="button"
                    className="btn btn-ghost btn-xs"
                    onClick={() => onToggle(tip)}
                  >
                    {tip.active ? "Ocultar" : "Ativar"}
                  </button>
                  <button
                    type="button"
                    className="btn btn-ghost btn-xs"
                    onClick={() => onEdit(tip)}
                  >
                    Editar
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function WeeklyTipModal({ state, form, saving, onChange, onSubmit, onClose, onDelete }) {
  const isEdit = state.mode === "edit" && state.record;

  return (
    <Modal
      open={state.open}
      onClose={saving ? null : onClose}
      title={isEdit ? "Editar dica" : "Nova dica"}
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
          <button
            type="button"
            className="btn btn-ghost"
            onClick={onClose}
            disabled={saving}
          >
            Cancelar
          </button>
          <button type="submit" form="weekly-tip-form" className="btn btn-primary" disabled={saving}>
            {saving && <span className="loading loading-spinner loading-xs mr-2" />}
            Guardar
          </button>
        </>
      }
    >
      <form id="weekly-tip-form" className="space-y-4" onSubmit={onSubmit}>
        <div className="grid gap-4 md:grid-cols-[1.5fr_1fr]">
          <label className="form-control">
            <span className="label-text font-semibold">Categoria</span>
            <input
              type="text"
              className="input input-bordered"
              required
              maxLength={80}
              value={form.category}
              onChange={(e) => onChange("category", e.target.value)}
            />
          </label>
          <label className="form-control">
            <span className="label-text font-semibold">Data</span>
            <input
              type="date"
              className="input input-bordered"
              required
              value={form.publishedAt}
              onChange={(e) => onChange("publishedAt", e.target.value)}
            />
          </label>
        </div>

        <label className="form-control">
          <span className="label-text font-semibold">Titulo</span>
          <input
            type="text"
            className="input input-bordered"
            required
            maxLength={160}
            value={form.title}
            onChange={(e) => onChange("title", e.target.value)}
          />
        </label>

        <label className="form-control">
            <br /><span className="label-text font-semibold">Descrição</span>
            <textarea
              className="textarea textarea-bordered min-h-[140px]"
              required
              maxLength={2000}
              value={form.description}
              onChange={(e) => onChange("description", e.target.value)}
            />
          </label>

        <div className="flex flex-wrap items-center gap-6">
          <label className="label cursor-pointer gap-3">
            <span className="label-text">Dica ativa</span>
            <input
              type="checkbox"
              className="toggle toggle-primary"
              checked={form.active}
              onChange={(e) => onChange("active", e.target.checked)}
            />
          </label>
          <label className="label cursor-pointer gap-3">
            <span className="label-text">Marcar como dica da semana</span>
            <input
              type="checkbox"
              className="toggle toggle-primary"
              checked={form.featured}
              onChange={(e) => onChange("featured", e.target.checked)}
            />
          </label>
        </div>
      </form>
    </Modal>
  );
}

function mapForm(record) {
  return {
    category: record.category ?? "",
    title: record.title ?? "",
    description: record.description ?? "",
    publishedAt: record.publishedAt ?? new Date().toISOString().slice(0, 10),
    featured: Boolean(record.featured),
    active: Boolean(record.active),
  };
}

function formatDate(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return date.toLocaleDateString("pt-PT", { day: "2-digit", month: "short", year: "numeric" });
}

