import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import Navbar from "../../components/sections/Navbar.jsx";
import Footer from "../../components/sections/Footer.jsx";
import { useAuthContext } from "../../auth/AuthContext.jsx";
import { useEmployeeProfile } from "../profile/Employee/EmployeeProfileContext.jsx";
import { fetchAppHomePublic, fetchWeeklyTipsPage } from "../../api/site/siteManagement.js";

const FALLBACK_METRICS = [
  { label: "Equipas concluidas", value: "8" },
  { label: "Requisicoes em aberto", value: "15" },
  { label: "Horas trabalhadas", value: "320h" },
  { label: "Avaliacao media", value: "4.7" },
];

const FALLBACK_NEWS_PLACEHOLDERS = Array.from({ length: 6 });

const FALLBACK_WEEKLY_TIP = {
  title: "Seguranca em primeiro lugar!",
  description: [
    "Antes de comecares o turno, confirma se todos os equipamentos estao em boas condicoes.",
    "Pequenos cuidados evitam grandes acidentes.",
  ],
};

export default function HomeAuthenticated() {
  const { profile, loadingProfile, refreshProfile } = useEmployeeProfile();
  const { logout, isAuthenticated } = useAuthContext();
  const [homeContent, setHomeContent] = useState(null);
  const [contentLoading, setContentLoading] = useState(true);
  const [contentError, setContentError] = useState(null);
  const [weeklyTipsData, setWeeklyTipsData] = useState(null);
  const [weeklyTipsError, setWeeklyTipsError] = useState(null);

  useEffect(() => {
    if (!profile && !loadingProfile) {
      refreshProfile();
    }
  }, [profile, loadingProfile, refreshProfile]);

  useEffect(() => {
    let active = true;
    setContentLoading(true);
    fetchAppHomePublic()
      .then((data) => {
        if (!active) return;
        setHomeContent(data);
        setContentError(null);
      })
      .catch((err) => {
        if (!active) return;
        setContentError(err.message || "Nao foi possivel carregar a home autenticada.");
      })
      .finally(() => {
        if (active) setContentLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    fetchWeeklyTipsPage()
      .then((data) => {
        if (!active) return;
        setWeeklyTipsData(data);
        setWeeklyTipsError(null);
      })
      .catch((err) => {
        if (!active) return;
        setWeeklyTipsError(err.message || "Nao foi possivel carregar as dicas da semana.");
      });
    return () => {
      active = false;
    };
  }, []);

  const heroSection = homeContent?.sections?.find((section) => section.type === "HERO");
  const weeklyTipSection = homeContent?.sections?.find((section) => section.type === "WEEKLY_TIP");
  const newsSection = homeContent?.sections?.find((section) => section.type === "NEWS");
  const metrics = homeContent?.metrics?.length ? homeContent.metrics : FALLBACK_METRICS;
  const newsArticles = Array.isArray(newsSection?.newsArticles) ? newsSection.newsArticles : [];

  const displayName = profile?.firstName
    ? `${profile.firstName}${profile.lastName ? ` ${profile.lastName}` : ""}`
    : "Utilizador";

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
          loading={loadingProfile}
          section={heroSection}
          loadingContent={contentLoading}
        />
        <MetricsSection metrics={metrics} />
        <WeeklyTipSection
          section={weeklyTipSection}
          tipOfWeek={weeklyTipsData?.tipOfWeek}
          error={weeklyTipsError}
        />
        <NewsSection section={newsSection} articles={newsArticles} loading={contentLoading} />
      </main>
      <Footer />
    </div>
  );
}

function HeroPanel({ displayName, loading, section, loadingContent }) {
  const title = section?.title ?? "Perfil do candidato";
  const subtitle = section?.subtitle ?? "Perfil =%";
  const contentLines = section?.content
    ? section.content.split("\n").filter(Boolean)
    : [];
  const ctaLabel = section?.primaryCtaLabel ?? "Atualizar perfil";
  const ctaUrl = section?.primaryCtaUrl ?? "/candidato/dados-pessoais";

  return (
    <section className="bg-base-200/40 py-12 border-b border-base-200">
      <div className="max-w-6xl mx-auto px-6">
        <div className="rounded-3xl bg-base-100 shadow-xl border border-base-300 p-8 flex flex-col gap-4">
          <div>
            <p className="text-sm uppercase tracking-[0.4em] text-primary/70">
              Ola {displayName}
            </p>
            <h1 className="text-4xl font-bold text-base-content">{title}</h1>
            <p className="text-lg text-base-content/70">{subtitle}</p>
          </div>
          <div className="text-base-content/70 space-y-1">
            {contentLines.map((line) => (
              <p key={line}>{line}</p>
            ))}
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <Link to={ctaUrl} className="btn btn-primary">
              {ctaLabel}
            </Link>
            {(loading || loadingContent) && (
              <div className="flex items-center gap-2 text-sm text-base-content/60">
                <span className="loading loading-spinner loading-xs" />
                <span>A carregar dados do perfil</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}

function MetricsSection({ metrics }) {
  return (
    <section className="max-w-6xl mx-auto px-6 py-14 space-y-8">
      <div className="space-y-2">
        <h2 className="text-2xl font-semibold text-base-content">As tuas metricas</h2>
        <p className="text-base-content/70 text-sm">
          Em breve vamos ligar estes numeros a API de relatorios internos.
        </p>
      </div>
      <div className="grid gap-6 md:grid-cols-4">
        {metrics.map((metric) => (
          <article
            key={metric.label}
            className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 text-center"
          >
            <p className="text-4xl font-bold text-primary">{metric.value}</p>
            <p className="mt-2 text-sm uppercase tracking-wide text-base-content/70">
              {metric.label}
            </p>
          </article>
        ))}
      </div>
    </section>
  );
}

function WeeklyTipSection({ section, tipOfWeek, error }) {
  const hasTip = Boolean(tipOfWeek);
  const sectionTitle = section?.title ?? "Dica da Semana";
  const tipTitle = hasTip ? tipOfWeek.title : FALLBACK_WEEKLY_TIP.title;
  const category = hasTip ? tipOfWeek.category : null;
  const descriptionText = hasTip
    ? tipOfWeek.description
    : FALLBACK_WEEKLY_TIP.description.join("\n");
  const paragraphs = descriptionText
    ? descriptionText.split("\n").filter(Boolean)
    : [];
  const ctaLabel = section?.primaryCtaLabel ?? "Ver mais dicas";
  const ctaUrl = section?.primaryCtaUrl ?? "/dicas";

  return (
    <section className="max-w-6xl mx-auto px-6 pb-14">
      <div className="rounded-3xl border border-base-200 bg-base-100 shadow p-8 space-y-4">
        <p className="text-sm uppercase tracking-[0.4em] text-primary/80">
          {sectionTitle}
        </p>
        <div className="flex flex-col gap-2">
          {tipTitle && (
            <h3 className="text-2xl font-semibold text-base-content">
              {tipTitle}
            </h3>
          )}
          {category && (
            <p className="text-sm font-medium text-base-content/70">
              {category}
            </p>
          )}
          {error && (
            <p className="text-xs text-error/80">
              {error}
            </p>
          )}
          {paragraphs.map((paragraph, index) => (
            <p key={index} className="text-base-content/70 text-sm leading-relaxed">
              {paragraph}
            </p>
          ))}
        </div>
        <div>
          <Link to={ctaUrl} className="btn btn-primary btn-sm">
            {ctaLabel}
          </Link>
        </div>
      </div>
    </section>
  );
}

function NewsSection({ section, articles, loading }) {
  const targetCount = section?.apiMaxItems ?? FALLBACK_NEWS_PLACEHOLDERS.length;
  const safeCount = Math.min(Math.max(targetCount, 1), 6);
  const placeholders = Array.from({ length: safeCount });
  const title = section?.title ?? "Noticias Recentes";
  const subtitle =
    section?.subtitle ?? "Secao preparada para integracao com fontes externas de noticias.";
  const ctaLabel = section?.primaryCtaLabel ?? "Ver mais";
  const ctaUrl = section?.primaryCtaUrl ?? "#";
  const hasArticles = Array.isArray(articles) && articles.length > 0;
  const visibleArticles = hasArticles ? articles.slice(0, safeCount) : [];

  const formatPublishedDate = (value) => {
    if (!value) return null;
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return null;
    }
    return parsed.toLocaleDateString("pt-PT", { day: "2-digit", month: "short" });
  };

  return (
    <section className="bg-base-200/40 border-t border-b border-base-200 py-14">
      <div className="max-w-6xl mx-auto px-6 space-y-8">
        <div className="text-center space-y-2">
          <h2 className="text-2xl font-bold text-base-content">{title}</h2>
          <p className="text-sm text-base-content/70">{subtitle}</p>
        </div>
        <div className={`grid gap-6 ${safeCount > 2 ? "md:grid-cols-3" : "md:grid-cols-2"}`}>
          {hasArticles
            ? visibleArticles.map((article) => (
                <article
                  key={article.url ?? article.title}
                  className="rounded-2xl border border-base-300 bg-base-100 overflow-hidden flex flex-col"
                >
                  {article.imageUrl ? (
                    <div className="h-40 w-full overflow-hidden">
                      <img
                        src={article.imageUrl}
                        alt={article.title}
                        className="h-full w-full object-cover"
                        loading="lazy"
                      />
                    </div>
                  ) : (
                    <div className="h-2 w-full bg-base-200" />
                  )}
                  <div className="p-5 flex flex-col gap-3 flex-1">
                    <div className="text-xs uppercase tracking-wide text-primary/70">
                      {article.sourceName ?? "Fonte externa"}
                    </div>
                    <h3 className="text-lg font-semibold text-base-content line-clamp-2">
                      {article.title}
                    </h3>
                    {article.description && (
                      <p className="text-sm text-base-content/70 line-clamp-3">{article.description}</p>
                    )}
                    <div className="mt-auto flex items-center justify-between text-xs text-base-content/60">
                      <span>{formatPublishedDate(article.publishedAt) ?? "Atualizado hoje"}</span>
                      <a
                        href={article.url}
                        className="link link-primary font-semibold"
                        target="_blank"
                        rel="noreferrer"
                      >
                        Ler materia
                      </a>
                    </div>
                  </div>
                </article>
              ))
            : placeholders.map((_, idx) => (
                <article
                  key={`news-placeholder-${idx}`}
                  className="rounded-2xl border border-dashed border-base-300 bg-base-100 h-48 flex flex-col items-center justify-center text-center text-base-content/60"
                >
                  <p className="font-semibold text-base-content/70">
                    {loading ? "A carregar..." : "Sem noticias disponiveis"}
                  </p>
                  <p className="text-xs text-base-content/60">
                    {loading ? "Sincronizando conteudo" : "Voltaremos a tentar em breve."}
                  </p>
                </article>
              ))}
        </div>
        {!hasArticles && !loading && (
          <p className="text-center text-sm text-base-content/70">
            Ainda nao recebemos noticias externas. Confirme a chave da NewsAPI ou tente mais tarde.
          </p>
        )}
        <div className="text-center">
          <a href={ctaUrl} className="btn btn-primary btn-wide">
            {ctaLabel}
          </a>
        </div>
      </div>
    </section>
  );
}



