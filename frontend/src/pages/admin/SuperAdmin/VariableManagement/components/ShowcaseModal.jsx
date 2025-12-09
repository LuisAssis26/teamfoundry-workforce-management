import { useState } from "react";
import PropTypes from "prop-types";
import Modal from "../../../../../components/ui/Modal/Modal.jsx";
import DropZone from "../../../../../components/ui/Upload/DropZone.jsx";
import { useVariableManagement } from "../VariableManagementContext.jsx";
import ShowcasePreview from "./ShowcasePreview.jsx";

export default function ShowcaseModal({ state, form, saving, onClose, onChange, onSubmit, onDelete }) {
  const [uploadingImage, setUploadingImage] = useState(false);
  const [uploadError, setUploadError] = useState(null);
  const { uploadSiteImage: uploadImage } = useVariableManagement();

  if (!state.open || !form) return null;
  const isIndustry = state.entity === "industry";

  const handleImageUpload = async (file) => {
    if (!file) return;
    setUploadError(null);
    setUploadingImage(true);
    try {
      const result = await uploadImage(file);
      onChange("imageUrl", result.url);
    } catch (err) {
      setUploadError(err.message || "NÆo foi possivel carregar a imagem.");
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
          <button type="submit" form="showcase-form" className="btn btn-primary" disabled={saving}>
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
            <span className="label-text font-semibold">Descri‡Æo</span>
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
              <DropZone label="Imagem (arraste ou clique para carregar)" onSelect={handleImageUpload} />
              {uploadingImage && (
                <div className="text-sm text-primary flex items-center gap-2">
                  <span className="loading loading-spinner loading-xs" />
                  A enviar imagem!
                </div>
              )}
              {uploadError && <p className="text-sm text-error">{uploadError}</p>}
              <label className="form-control">
                <span className="label-text text-sm text-base-content/70">Ou cole um URL de imagem</span>
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

ShowcaseModal.propTypes = {
  state: PropTypes.shape({
    open: PropTypes.bool,
    entity: PropTypes.string,
    mode: PropTypes.string,
    record: PropTypes.object,
  }).isRequired,
  form: PropTypes.object,
  saving: PropTypes.bool,
  onClose: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  onDelete: PropTypes.func.isRequired,
};

ShowcaseModal.defaultProps = {
  form: null,
  saving: false,
};
