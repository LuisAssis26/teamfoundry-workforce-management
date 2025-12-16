import FieldGroup from "../../components/FieldGroup.jsx";
import SectionOrderCard from "./SectionOrderCard.jsx";
import ShowcaseList from "./ShowcaseList.jsx";
import { useVariableManagement } from "../VariableManagementContext.jsx";

export default function PublicHomeView() {
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
      {banner && (
        <div className={`alert ${banner.type === "error" ? "alert-error" : "alert-success"} shadow flex justify-between`}>
          <span>{banner.message}</span>
          <button type="button" className="btn btn-ghost btn-xs" onClick={() => setBanner(null)}>
            Fechar
          </button>
        </div>
      )}

      <div className="card bg-base-100 shadow-xl">
        <div className="card-body space-y-6">
          <div>
            <h2 className="card-title text-3xl">Hero</h2>
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
                label="Texto do botão principal"
                value={heroForm.primaryCtaLabel}
                onChange={(value) => handleHeroFieldChange("primaryCtaLabel", value)}
                placeholder="Ex.: Quero trabalhar"
              />
              <FieldGroup
                label="URL do botão principal"
                value={heroForm.primaryCtaUrl}
                onChange={(value) => handleHeroFieldChange("primaryCtaUrl", value)}
                placeholder="/login"
              />
              <FieldGroup
                label="Texto do botão secundário"
                value={heroForm.secondaryCtaLabel}
                onChange={(value) => handleHeroFieldChange("secondaryCtaLabel", value)}
                placeholder="Sou empresa"
              />
              <FieldGroup
                label="URL do botão secundário"
                value={heroForm.secondaryCtaUrl}
                onChange={(value) => handleHeroFieldChange("secondaryCtaUrl", value)}
                placeholder="/company-register"
              />

              <div className="md:col-span-2">
                <label className="flex flex-col gap-2 items-start w-fit">
                  <span className="label-text font-semibold">Mostrar esta seção no site público:</span>
                  <input
                    type="checkbox"
                    className="toggle toggle-primary"
                    checked={heroForm.active}
                    onChange={(e) => handleHeroFieldChange("active", e.target.checked)}
                  />
                </label>
              </div>

              <div className="md:col-span-2 flex gap-3 justify-end">
                <button type="submit" className="btn btn-primary" disabled={savingHero}>
                  {savingHero ? (
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
          )}
        </div>
      </div>

      <SectionOrderCard sections={config.sections} onMove={handleSectionMove} onToggle={handleSectionToggle} />

      <ShowcaseList
        title="Indústrias em destaque"
        description="Controle quais setores aparecem e a ordem apresentada na homepage."
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
