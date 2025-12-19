import { useEffect, useMemo, useState } from "react";
import { Navigate } from "react-router-dom";
import Navbar from "../../components/sections/Navbar.jsx";
import Footer from "../../components/sections/Footer.jsx";
import { useAuthContext } from "../../auth/AuthContext.jsx";
import { useEmployeeProfile } from "../profile/Employee/EmployeeProfileContext.jsx";
import { fetchUnifiedHome } from "../../api/site/siteManagement.js";
import { fetchEmployeeProfileSummary } from "../../api/profile/employeeProfile.js";
import HeroPanel from "./HomeAuthenticated/components/HeroPanel.jsx";
import WeeklyTipSection from "./HomeAuthenticated/components/WeeklyTipSection.jsx";

function SectionHeader({ section, fallbackTitle, fallbackSubtitle }) {
  const hasSubtitle = (section?.subtitle ?? fallbackSubtitle)?.length;
  return (
    <header className="text-center space-y-3">
      <h2 className="text-3xl md:text-4xl font-extrabold text-base-content">
        {section?.title ?? fallbackTitle}
      </h2>
      {hasSubtitle && (
        <p className="text-base-content/70 max-w-3xl mx-auto">
          {section?.subtitle ?? fallbackSubtitle}
        </p>
      )}
    </header>
  );
}

function ShowcaseImage({ src, alt }) {
  if (!src) {
    return (
      <div className="h-full w-full flex items-center justify-center bg-base-200 text-base-content/50">
        {alt}
      </div>
    );
  }
  return <img src={src} alt={alt} className="h-full w-full object-cover" loading="lazy" />;
}

function EmptyState({ message }) {
  return (
    <div className="rounded-2xl border border-dashed border-base-300 bg-base-100/60 p-10 text-center">
      <p className="text-base-content/60">{message}</p>
    </div>
  );
}

function PublicHero({ section }) {
  const subtitle =
    section?.subtitle ??
    "A forma mais facil de conectar profissionais a projetos industriais e criar equipas de alto desempenho.";

  return (
    <section
      id="hero"
      className="relative text-primary-content overflow-hidden min-h-[26rem] md:min-h-[30rem] flex items-center"
      style={{
        backgroundImage: "url('/hero-home.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
      }}
    >
      <div className="absolute inset-0 bg-primary/80" aria-hidden="true" />
      <div
        className="absolute inset-0 mix-blend-screen opacity-60"
        aria-hidden="true"
        style={{
          background:
            "radial-gradient(90% 70% at 20% 30%, rgba(255,255,255,0.16), transparent 35%), radial-gradient(110% 90% at 80% 15%, rgba(255,255,255,0.12), transparent 45%)",
        }}
      />
      <div className="relative max-w-6xl mx-auto px-6 py-14 md:py-20 text-center space-y-5 w-full">
        <h1 className="text-4xl md:text-5xl font-extrabold">
          {section?.title ?? "Team Foundry"}
        </h1>
        <p className="text-lg text-primary-content/80 leading-relaxed -mt-2">{subtitle}</p>
        <div className="flex flex-wrap justify-center gap-3 pt-2">
          {section?.primaryCtaLabel && section?.primaryCtaUrl && (
            <a href={section.primaryCtaUrl} className="btn btn-primary btn-wide shadow">
              {section.primaryCtaLabel}
            </a>
          )}
          {section?.secondaryCtaLabel && section?.secondaryCtaUrl && (
            <a href={section.secondaryCtaUrl} className="btn btn-outline btn-wide shadow-sm">
              {section.secondaryCtaLabel}
            </a>
          )}
        </div>
      </div>
    </section>
  );
}

function AreasSection({ section, industries }) {
  return (
    <section id="areas" className="bg-base-200/40 border-y border-base-200 py-16 overflow-hidden">
      <div className="max-w-6xl mx-auto px-6 space-y-10">
        <SectionHeader
          section={section}
          fallbackTitle="Areas em que atuamos"
          fallbackSubtitle="Segmentos onde ligamos empresas e profissionais especializados."
        />
        {industries?.length ? (
          <div className="grid gap-8 md:grid-cols-3">
            {industries.map((industry) => (
              <article key={industry.id} className="bg-base-100 rounded-3xl shadow-lg overflow-hidden">
                <div className="h-56 bg-base-200">
                  <ShowcaseImage src={industry.imageUrl} alt={industry.name} />
                </div>
                <div className="px-5 py-4 text-center">
                  <h3 className="text-base font-semibold text-base-content">{industry.name}</h3>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState message="Ainda nao ha areas destacadas." />
        )}
      </div>
    </section>
  );
}

function PartnersSection({ section, partners }) {
  return (
    <section id="parceiros" className="bg-base-100 pt-0 pb-16 mb-20">
      <div className="max-w-6xl mx-auto px-6 space-y-10">
        <SectionHeader
          section={section}
          fallbackTitle="Parceiros principais"
          fallbackSubtitle="Empresas que confiam na Team Foundry para acelerar os seus projetos."
        />
        {partners?.length ? (
          <div className="grid gap-10 md:grid-cols-2">
            {partners.map((partner) => (
              <article
                key={partner.id}
                className="bg-base-100 rounded-3xl shadow-lg overflow-hidden flex flex-col md:flex-row"
              >
                <div className="w-full md:w-[45%] h-56 md:h-auto bg-base-200 flex-shrink-0">
                  <ShowcaseImage src={partner.imageUrl} alt={partner.name} />
                </div>
                <div className="w-full md:w-[55%] p-6 space-y-3 flex flex-col justify-center">
                  <h3 className="text-xl font-semibold text-base-content">{partner.name}</h3>
                  <p className="text-sm text-base-content/80 leading-relaxed">{partner.description}</p>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState message="Ainda nao ha parceiros publicados." />
        )}
      </div>
    </section>
  );
}

export default function HomeUnified() {
  const { isAuthenticated, userType, logout } = useAuthContext();
  const { profile } = useEmployeeProfile();

  const [homeData, setHomeData] = useState(null);
  const [homeLoading, setHomeLoading] = useState(true);
  const [homeError, setHomeError] = useState(null);

  const [profileSummary, setProfileSummary] = useState(null);
  const [profileSummaryLoading, setProfileSummaryLoading] = useState(false);

  useEffect(() => {
    let active = true;
    setHomeLoading(true);
    fetchUnifiedHome()
      .then((data) => {
        if (!active) return;
        setHomeData(data);
        setHomeError(null);
      })
      .catch((err) => {
        if (!active) return;
        setHomeError(err.message || "Falha ao carregar a home.");
      })
      .finally(() => active && setHomeLoading(false));
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!isAuthenticated) return;
    let active = true;
    setProfileSummaryLoading(true);
    fetchEmployeeProfileSummary()
      .then((data) => {
        if (!active) return;
        setProfileSummary(data);
      })
      .catch(() => {
        if (!active) return;
        setProfileSummary(null);
      })
      .finally(() => active && setProfileSummaryLoading(false));
    return () => {
      active = false;
    };
  }, [isAuthenticated]);

  const orderedPublicSections = useMemo(() => {
    if (!Array.isArray(homeData?.publicSections)) return [];
    return [...homeData.publicSections]
      .filter((section) => section.active)
      .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0));
  }, [homeData]);

  const industries = homeData?.industries ?? [];
  const partners = homeData?.partners ?? [];

  const displayName = useMemo(() => {
    if (profile?.firstName) {
      return profile.firstName;
    }
    return "Utilizador";
  }, [profile]);

  if (isAuthenticated && userType === "COMPANY") {
    return <Navigate to="/empresa" replace />;
  }

  const heroSectionAuth = homeData?.authenticatedSections?.find((section) => section.type === "HERO");
  const weeklyTipSection = homeData?.authenticatedSections?.find((section) => section.type === "WEEKLY_TIP");

  const publicHero = orderedPublicSections.find((section) => section.type === "HERO");
  const areasSection = orderedPublicSections.find((section) => section.type === "INDUSTRIES");
  const partnersSection = orderedPublicSections.find((section) => section.type === "PARTNERS");

  const isLoading = homeLoading;
  const anyError = homeError;

  return (
    <div className="min-h-screen flex flex-col bg-base-100 text-base-content">
      <Navbar
        variant={isAuthenticated ? "private" : "public"}
        homePath="/"
        links={[]}
        onLogout={isAuthenticated ? logout : undefined}
      />
      <main className="flex-1">
        {anyError && (
          <div className="alert alert-warning shadow mx-auto mt-4 w-full max-w-3xl">
            <span>{anyError}</span>
          </div>
        )}

        {isLoading && (
          <div className="flex min-h-[50vh] items-center justify-center">
            <span className="loading loading-spinner loading-lg text-primary" />
          </div>
        )}

        {!isLoading && !anyError && (
          <div className="space-y-14">
            {isAuthenticated ? (
              <HeroPanel
                displayName={displayName}
                section={heroSectionAuth}
                profileSummary={profileSummary}
                summaryLoading={profileSummaryLoading}
                loadingContent={homeLoading}
              />
            ) : (
              <PublicHero section={publicHero} />
            )}

            {isAuthenticated && (
              <WeeklyTipSection
                section={weeklyTipSection}
                tipOfWeek={homeData?.weeklyTips?.tipOfWeek}
                error={null}
                fallbackTip={{ title: "Dica da semana", description: [] }}
              />
            )}

            <AreasSection section={areasSection} industries={industries} />
            <PartnersSection section={partnersSection} partners={partners} />
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
}
