import AppHomeManager from "../AppHome/AppHomeManager.jsx";
import WeeklyTipsManager from "../WeeklyTipsManager.jsx";
import { VIEW_TABS } from "./constants.js";
import { useVariableManagement } from "./VariableManagementContext.jsx";
import GlobalOptionsView from "./components/GlobalOptionsView.jsx";
import PublicHomeView from "./components/PublicHomeView.jsx";
import ShowcaseModal from "./components/ShowcaseModal.jsx";
import TabPlaceholder from "./components/TabPlaceholder.jsx";

export default function VariableManagementLayout() {
  const {
    activeView,
    setActiveView,
    loading,
    loadError,
    config,
    retryHomepageConfig,
    modalState,
    modalForm,
    modalSaving,
    closeModal,
    handleModalFieldChange,
    handleModalSubmit,
    handleModalDelete,
    handleUnauthorized,
  } = useVariableManagement();

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
      return <GlobalOptionsView />;
    }

    if (activeView === "otherPages") {
      return (
        <TabPlaceholder
          title="Outras paginas"
          description="Gestao futura de paginas adicionais. Em breve."
        />
      );
    }

    return <PublicHomeView />;
  };

  return (
    <section className="space-y-12 pt-8">
      <header>
        <p className="text-sm uppercase tracking-[0.35em] text-primary/80">Gestão do site</p>
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
