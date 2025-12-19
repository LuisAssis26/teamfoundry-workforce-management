import WeeklyTipsManager from "../WeeklyTipsManager.jsx";
import { VIEW_TABS } from "./constants.js";
import { useVariableManagement } from "./VariableManagementContext.jsx";
import GlobalOptionsView from "./components/GlobalOptionsView.jsx";
import PublicHomeView from "./components/PublicHomeView.jsx";
import ShowcaseModal from "./components/ShowcaseModal.jsx";
import { AppHomeProvider } from "../AppHome/AppHomeContext.jsx";
import AppHomeLayout from "../AppHome/AppHomeLayout.jsx";
import CombinedOrderCard from "./components/CombinedOrderCard.jsx";
import Tabs from "../../../../components/sections/Tabs.jsx";

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

  const viewTabs = VIEW_TABS.map((tab) => ({
    key: tab.id,
    label: tab.label,
  }));

  const renderContent = () => {
    if (activeView === "home") {
      return (
        <AppHomeProvider onUnauthorized={handleUnauthorized}>
          <div className="space-y-12">
            <PublicHomeView showOrderCard={false} showShowcases={false} />
            <AppHomeLayout showOrderCard={false} />
            <PublicHomeView showOrderCard={false} showHero={false} showBanner={false} />
            <CombinedOrderCard />
          </div>
        </AppHomeProvider>
      );
    }
    if (activeView === "weeklyTips") {
      return <WeeklyTipsManager onUnauthorized={handleUnauthorized} />;
    }
    if (activeView === "globalVars") {
      return <GlobalOptionsView />;
    }
    return null;
  };

  return (
    <section className="space-y-12 pt-8">
      <header>
        <h1 className="text-4xl font-extrabold text-primary">Configurações do Site</h1>
      </header>

      <Tabs
        tabs={viewTabs}
        activeKey={activeView}
        onTabChange={setActiveView}
        className="mt-0"
      />

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
