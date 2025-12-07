import { useEffect, useMemo, useState } from "react";
import { fetchPublicHomepage } from "../../api/site/siteManagement.js";
import Navbar from "../../components/sections/Navbar.jsx";
import Footer from "../../components/sections/Footer.jsx";

const SECTION_COMPONENTS = {
  HERO: HeroSection,
  INDUSTRIES: IndustriesSection,
  PARTNERS: PartnersSection,
};

const SECTION_NAV_LINKS = {
  HERO: { to: "#hero", label: "Topo" },
  INDUSTRIES: { to: "#industrias", label: "Indǧstrias" },
  PARTNERS: { to: "#parceiros", label: "Parceiros" },
};

export function HomeNoLogin() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    fetchPublicHomepage()
      .then((payload) => {
        if (active) {
          setData(payload);
          setError(null);
        }
      })
      .catch((err) => {
        if (active) setError(err.message || "Falha ao carregar a pǭgina inicial.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const orderedSections = useMemo(() => {
    if (!Array.isArray(data?.sections)) return [];
    return [...data.sections]
      .filter((section) => section.active)
      .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0));
  }, [data]);

  const navLinks = [];

  const renderSection = (section) => {
    const Component = SECTION_COMPONENTS[section.type];
    if (!Component) return null;
    return (
      <Component
        key={section.id}
        section={section}
        industries={data?.industries ?? []}
        partners={data?.partners ?? []}
      />
    );
  };

  return (
    <div className="min-h-screen bg-base-100 text-base-content">
      <Navbar variant="public" homePath="/" links={navLinks} />
      {loading && (
        <div className="flex min-h-[60vh] items-center justify-center">
          <span className="loading loading-spinner loading-lg text-primary" />
        </div>
      )}

      {error && !loading && (
        <div className="flex min-h-[60vh] items-center justify-center">
          <div className="alert alert-error shadow-lg max-w-lg">
            <i className="bi bi-exclamation-triangle-fill text-xl" />
            <span>{error}</span>
          </div>
        </div>
      )}

      {!loading && !error && data && (
        <>
          {orderedSections.length ? (
            orderedSections.map(renderSection)
          ) : (
            <div className="max-w-6xl mx-auto px-6 py-24">
              <EmptyState message="Nenhuma sec��ǜo foi ativada ainda." />
            </div>
          )}
          <Footer />
        </>
      )}
    </div>
  );
}

function HeroSection({ section }) {
  const subtitle =
    section?.subtitle ??
    "A forma mais fǭcil de conectar profissionais a projetos industriais e criar equipas de alto desempenho.";

  return (
    <section
      id="hero"
      className="relative text-primary-content overflow-hidden"
      style={{
        backgroundImage: "url('/hero-home.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
      }}
    >
      <div className="absolute inset-0 bg-primary/50" aria-hidden="true" />
      <div className="relative max-w-6xl mx-auto px-6 py-20 text-center space-y-5">
        <div className="flex items-center justify-center gap-3" />
        <h1 className="text-4xl md:text-5xl font-extrabold">
          Team Foundry
        </h1>
        <p className="text-lg text-primary-content/80 leading-relaxed -mt-2">
          {subtitle}
        </p>
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

function IndustriesSection({ section, industries }) {
  return (
    <section id="industrias" className="bg-base-200/40 border-y border-base-200 py-16 overflow-hidden">
      <div className="max-w-6xl mx-auto px-6 space-y-10">
        <SectionHeader
          section={section}
          fallbackTitle="Indǧstrias em que atuamos"
          fallbackSubtitle="Segmentos onde ligamos empresas e profissionais especializados."
        />
        {industries?.length ? (
          <div className="grid gap-8 md:grid-cols-3">
            {industries.map((industry) => (
              <article
                key={industry.id}
                className="bg-base-100 rounded-3xl shadow-lg overflow-hidden"
              >
                <div className="h-56 bg-base-200">
                  <ShowcaseImage src={industry.imageUrl} alt={industry.name} />
                </div>
                <div className="px-5 py-4 text-center">
                  <h3 className="text-base font-semibold text-base-content">
                    {industry.name}
                  </h3>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState message="Ainda nǜo hǭ indǧstrias destacadas." />
        )}
      </div>
    </section>
  );
}

function PartnersSection({ section, partners }) {
  return (
    <section id="parceiros" className="bg-base-100 py-16">
      <div className="max-w-6xl mx-auto px-6 space-y-10">
        <SectionHeader
          section={section}
          fallbackTitle="Parceiros principais"
          fallbackSubtitle="Empresas que confiam na TeamFoundry para acelerar os seus projetos."
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
                  <h3 className="text-xl font-semibold text-base-content">
                    {partner.name}
                  </h3>
                  <p className="text-sm text-base-content/80 leading-relaxed">
                    {partner.description}
                  </p>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState message="Ainda não há parceiros publicados." />
        )}
      </div>
    </section>
  );
}

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
