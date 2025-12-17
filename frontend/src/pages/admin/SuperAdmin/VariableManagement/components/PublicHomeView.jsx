import PropTypes from "prop-types";
import FieldGroup from "../../components/FieldGroup.jsx";
import SectionOrderCard from "./SectionOrderCard.jsx";
import ShowcaseList from "./ShowcaseList.jsx";
import { useVariableManagement } from "../VariableManagementContext.jsx";

export default function PublicHomeView({
  showOrderCard = true,
  showHero = true,
  showShowcases = true,
  showBanner = true,
}) {
  const {
    banner,
    setBanner,
    heroForm,
    savingHero,
    handleHeroFieldChange,
    handleHeroSubmit,
    config,
    handleSectionMove,
    handleSectionToggle,
    handleIndustryMove,
    handlePartnerMove,
    openModal,
  } = useVariableManagement();

  if (!config) return null;

  return (
    <div className="space-y-10">
      {showBanner && banner && (
        <div className={`alert ${banner.type === "error" ? "alert-error" : "alert-success"} shadow flex justify-between`}>
          <span>{banner.message}</span>
          <button type="button" className="btn btn-ghost btn-xs" onClick={() => setBanner(null)}>
            Fechar
          </button>
        </div>
      )}

      {showHero && (
        <div className="card bg-base-100 shadow-xl">
          <div className="card-body space-y-6">
            <div>
              <h2 className="card-title text-3xl">Hero público</h2>
              <p className="text-base-content/70">
                Edite título, subtítulo e os botões principais que aparecem na homepage pública.
              </p>
            </div>
            {heroForm && (
              <form className="grid gap-4 md:grid-cols-2" onSubmit={handleHeroSubmit}>
                <label className="flex flex-col gap-2 items-start md:col-span-2">
                  <span className="label-text font-semibold">Título:</span>
                  <input
                    type="text"
                    className="input input-bordered"
                    required
                    value={heroForm.title}
                    onChange={(e) => handleHeroFieldChange("title", e.target.value)}
                  />
                </label>

                <label className="flex flex-col gap-2 items-start md:col-span-2">
                  <span className="label-text font-semibold">Subtítulo:</span>
                  <textarea
                    className="textarea textarea-bordered min-h-[120px]"
                    value={heroForm.subtitle}
                    onChange={(e) => handleHeroFieldChange("subtitle", e.target.value)}
                  />
                </label>

                <FieldGroup
                  label="Texto do botão principal:"
                  value={heroForm.primaryCtaLabel}
                  onChange={(value) => handleHeroFieldChange("primaryCtaLabel", value)}
                  placeholder="Ex.: Quero trabalhar"
                />
                <FieldGroup
                  label="URL do botão principal:"
                  value={heroForm.primaryCtaUrl}
                  onChange={(value) => handleHeroFieldChange("primaryCtaUrl", value)}
                  placeholder="/login"
                />
                <FieldGroup
                  label="Texto do botão secundário:"
                  value={heroForm.secondaryCtaLabel}
                  onChange={(value) => handleHeroFieldChange("secondaryCtaLabel", value)}
                  placeholder="Ex.: Sou empresa"
                />
                <FieldGroup
                  label="URL do botão secundário:"
                  value={heroForm.secondaryCtaUrl}
                  onChange={(value) => handleHeroFieldChange("secondaryCtaUrl", value)}
                  placeholder="/company-register"
                />

                <label className="flex flex-col gap-2 items-start">
                  <span className="label-text font-semibold">Visibilidade:</span>
                  <input
                    type="checkbox"
                    className="toggle toggle-primary"
                    checked={heroForm.active}
                    onChange={() => handleHeroFieldChange("active", !heroForm.active)}
                  />
                </label>

                <div className="md:col-span-2 flex justify-end">
                  <button type="submit" className={`btn btn-primary ${savingHero ? "loading" : ""}`} disabled={savingHero}>
                    Guardar alterações
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {showOrderCard && (
        <SectionOrderCard
          sections={config.sections}
          onMove={handleSectionMove}
          onToggle={handleSectionToggle}
          title="Ordem das seções"
          description="Defina a ordem das seções que aparecem para visitantes."
        />
      )}

      {showShowcases && (
        <>
          <ShowcaseList
            title="Áreas em que atuamos"
            description="Gerencie as áreas/indústrias que aparecem na vitrine."
            items={config.industries}
            onMove={handleIndustryMove}
            onAdd={() => openModal("industry")}
            onEdit={(item) => openModal("industry", item)}
          />

          <ShowcaseList
            title="Parceiros principais"
            description="Organize os parceiros exibidos na home."
            items={config.partners}
            onMove={handlePartnerMove}
            onAdd={() => openModal("partner")}
            onEdit={(item) => openModal("partner", item)}
          />
        </>
      )}
    </div>
  );
}

PublicHomeView.propTypes = {
  showOrderCard: PropTypes.bool,
  showHero: PropTypes.bool,
  showShowcases: PropTypes.bool,
  showBanner: PropTypes.bool,
};

PublicHomeView.defaultProps = {
  showOrderCard: true,
  showHero: true,
  showShowcases: true,
  showBanner: true,
};
