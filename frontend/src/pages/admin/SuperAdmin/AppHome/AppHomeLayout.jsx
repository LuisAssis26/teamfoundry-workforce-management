import AppHeroSection from "./components/AppHeroSection.jsx";
import AppSectionOrderCard from "./components/AppSectionOrderCard.jsx";
import AppWeeklyTipSection from "./components/AppWeeklyTipSection.jsx";
import PropTypes from "prop-types";
import { useAppHome } from "./AppHomeContext.jsx";

export default function AppHomeLayout({ showOrderCard = true }) {
  const {
    banner,
    setBanner,
    forms,
    savingSections,
    sections,
    config,
    loadError,
    loading,
    loaded,
    handleSectionFieldChange,
    handleSectionSubmit,
    handleSectionMove,
    handleSectionToggle,
    retryAppHomeConfig,
  } = useAppHome();

  if (config == null && loadError) {
    return (
      <div className="flex flex-col items-center gap-4 py-20">
        <p className="text-lg text-base-content/70">{loadError}</p>
        <button type="button" className="btn btn-primary" onClick={retryAppHomeConfig}>
          Tentar novamente
        </button>
      </div>
    );
  }

  if (config == null && (loading || !loaded)) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    );
  }

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

      {showOrderCard && (
        <AppSectionOrderCard sections={sections} onMove={handleSectionMove} onToggle={handleSectionToggle} />
      )}
    </div>
  );
}

AppHomeLayout.propTypes = {
  showOrderCard: PropTypes.bool,
};

AppHomeLayout.defaultProps = {
  showOrderCard: true,
};
