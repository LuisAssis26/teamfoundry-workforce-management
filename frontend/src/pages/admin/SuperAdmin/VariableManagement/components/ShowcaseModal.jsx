import { useState } from "react";
import PropTypes from "prop-types";
import Modal from "../../../../../components/ui/Modal/Modal.jsx";
import DropZone from "../../../../../components/ui/Upload/DropZone.jsx";
import InputField from "../../../../../components/ui/Input/InputField.jsx";
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
      setUploadError(err.message || "Não foi possível carregar a imagem.");
    } finally {
      setUploadingImage(false);
    }
  };

  return (
    <Modal
      open
      title={
        state.mode === "create"
          ? `Adicionar ${isIndustry ? "indústria" : "parceiro"}`
          : `Editar ${isIndustry ? "indústria" : "parceiro"}`
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
                A guardar...
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
          <label className="form-control gap-2">
            <span className="label-text font-semibold">Nome</span>
            <input
              type="text"
              className="input input-bordered"
              required
              value={form.name}
              onChange={(e) => onChange("name", e.target.value)}
            />
          </label>
          <label className="form-control gap-2">
            <div className="flex flex-col gap-1">
              <span className="label-text font-semibold">Visibilidade</span>
              <span className="text-xs text-base-content/60">
                Controle se aparece na página inicial
              </span>
            </div>
            <div className="mt-1">
              <label className="label cursor-pointer gap-4 justify-start">
                <span className="text-sm">{form.active ? "Visível" : "Oculto"}</span>
                <input
                  type="checkbox"
                  className="toggle toggle-primary"
                  checked={form.active}
                  onChange={(e) => onChange("active", e.target.checked)}
                />
              </label>
            </div>
          </label>
        </div>

        {!isIndustry && (
          <InputField
            label="Descrição"
            as="textarea"
            placeholder="Descreva o parceiro..."
            value={form.description}
            onChange={(e) => onChange("description", e.target.value)}
            inputClassName="min-h-[120px]"
          />
        )}

        <div className="space-y-4">
          <div className="flex flex-col gap-4 md:flex-row">
            <div className="flex-1 space-y-3">
              <DropZone label="Imagem (arraste ou clique para carregar)" onSelect={handleImageUpload} />
              {uploadingImage && (
                <div className="text-sm text-primary flex items-center gap-2">
                  <span className="loading loading-spinner loading-xs" />
                  A enviar imagem...
                </div>
              )}
              {uploadError && <p className="text-sm text-error">{uploadError}</p>}
              <label className="form-control gap-2">
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
