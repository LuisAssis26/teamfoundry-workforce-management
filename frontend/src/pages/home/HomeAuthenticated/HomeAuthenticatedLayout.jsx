import { Navigate } from "react-router-dom";
import Footer from "../../../components/sections/Footer.jsx";
import Navbar from "../../../components/sections/Navbar.jsx";
import { useHomeAuthenticated } from "./HomeAuthenticatedContext.jsx";
import HeroPanel from "./components/HeroPanel.jsx";
import WeeklyTipSection from "./components/WeeklyTipSection.jsx";
import NewsSection from "./components/NewsSection.jsx";

export default function HomeAuthenticatedLayout() {
  const {
    isAuthenticated,
    logout,
    contentError,
    contentLoading,
    heroSection,
    weeklyTipSection,
    weeklyTipsData,
    weeklyTipsError,
    newsSection,
    newsArticles,
    fallbackWeeklyTip,
    fallbackNews,
    displayName,
    profileSummary,
    profileSummaryLoading,
  } = useHomeAuthenticated();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen flex flex-col bg-base-100 text-base-content">
      <Navbar variant="private" homePath="/" links={[]} onLogout={logout} />
      <main className="flex-1">
        {contentError && (
          <div className="alert alert-warning shadow mx-auto mt-4 w-full max-w-3xl">
            <span>{contentError}</span>
          </div>
        )}
        <HeroPanel
          displayName={displayName}
          section={heroSection}
          profileSummary={profileSummary}
          summaryLoading={profileSummaryLoading}
          loadingContent={contentLoading}
        />
        <WeeklyTipSection
          section={weeklyTipSection}
          tipOfWeek={weeklyTipsData?.tipOfWeek}
          error={weeklyTipsError}
          fallbackTip={fallbackWeeklyTip}
        />
        <NewsSection section={newsSection} articles={newsArticles} loading={contentLoading} fallbackNews={fallbackNews} />
      </main>
      <Footer />
    </div>
  );
}
