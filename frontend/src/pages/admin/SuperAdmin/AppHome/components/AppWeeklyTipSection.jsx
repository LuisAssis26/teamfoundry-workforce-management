import PropTypes from "prop-types";
import FieldGroup from "../../components/FieldGroup.jsx";

export default function AppWeeklyTipSection({ form, saving, onFieldChange, onSubmit }) {
  if (!form) return null;
  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-6">
        <div>
          <h2 className="card-title text-3xl">Dica da semana</h2>
        </div>
        <form className="space-y-4" onSubmit={onSubmit}>
          <FieldGroup
            label="Titulo"
            value={form.title}
            onChange={(value) => onFieldChange("title", value)}
            placeholder="Dica da semana"
          />

          <div className="flex flex-col gap-4 md:flex-row">
            <FieldGroup
              label="Texto do botao"
              value={form.primaryCtaLabel}
              onChange={(value) => onFieldChange("primaryCtaLabel", value)}
              placeholder="Ver mais"
              className="flex-1"
            />
            <FieldGroup
              label="URL do botao"
              value={form.primaryCtaUrl}
              onChange={(value) => onFieldChange("primaryCtaUrl", value)}
              placeholder="/dicas"
              className="flex-1"
            />
          </div>

          <label className="label cursor-pointer gap-3">
            <input
              type="checkbox"
              className="toggle toggle-primary"
              checked={form.active}
              onChange={(e) => onFieldChange("active", e.target.checked)}
            />
            <span className="label-text">Mostrar secao</span>
          </label>

          <div className="flex justify-end">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? (
                <>
                  <span className="loading loading-spinner loading-sm" />
                  A guardar...
                </>
              ) : (
                "Guardar alteracoes"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

AppWeeklyTipSection.propTypes = {
  form: PropTypes.object,
  saving: PropTypes.bool,
  onFieldChange: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
};

AppWeeklyTipSection.defaultProps = {
  form: null,
  saving: false,
};
