import PropTypes from "prop-types";
import FieldGroup from "../../components/FieldGroup.jsx";

export default function AppHeroSection({ form, saving, onFieldChange, onSubmit }) {
  if (!form) return null;
  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-6">
        <div>
          <h2 className="card-title text-3xl">Hero</h2>
          <p className="text-base-content/60 text-sm">
            Personalize o prefixo do cumprimento, a visibilidade da barra, os rótulos e o botão.
          </p>
        </div>

        <form className="flex flex-col gap-6" onSubmit={onSubmit}>
          <div className="grid gap-4 md:grid-cols-2">
            <FieldGroup
              label="Frase antes do nome"
              value={form.greetingPrefix}
              onChange={(value) => onFieldChange("greetingPrefix", value)}
              placeholder="Olá"
              className="w-full"
            />
            <div />
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <FieldGroup
              label="Etiqueta da empresa atual"
              value={form.labelCurrentCompany}
              onChange={(value) => onFieldChange("labelCurrentCompany", value)}
              placeholder="Empresa atual"
            />
            <FieldGroup
              label="Etiqueta das ofertas disponíveis"
              value={form.labelOffers}
              onChange={(value) => onFieldChange("labelOffers", value)}
              placeholder="Ofertas disponíveis"
            />
          </div>

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

          <input type="hidden" value={form.title} readOnly />
          <input type="hidden" value={form.subtitle} readOnly />
          <input type="hidden" value={form.content} readOnly />

          <div className="flex flex-wrap gap-6">
            <label className="flex flex-col gap-2 items-start w-fit">
              <span className="label-text font-semibold">Mostrar barra de progresso:</span>
              <input
                type="checkbox"
                className="toggle toggle-primary"
                checked={form.profileBarVisible}
                onChange={(e) => onFieldChange("profileBarVisible", e.target.checked)}
              />
            </label>
            <label className="flex flex-col gap-2 items-start w-fit">
              <span className="label-text font-semibold">Mostrar seção:</span>
              <input
                type="checkbox"
                className="toggle toggle-primary"
                checked={form.active}
                onChange={(e) => onFieldChange("active", e.target.checked)}
              />
            </label>
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

AppHeroSection.propTypes = {
  form: PropTypes.object,
  saving: PropTypes.bool,
  onFieldChange: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
};

AppHeroSection.defaultProps = {
  form: null,
  saving: false,
};
