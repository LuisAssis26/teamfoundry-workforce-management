import PropTypes from "prop-types";
import { Link } from "react-router-dom";

export default function HeroPanel({
  displayName,
  section,
  profileSummary,
  summaryLoading,
  loadingContent,
}) {
  if (section && section.active === false) return null;

  const greetingPrefix = section?.greetingPrefix || "Ola";
  const labelCompany = section?.labelCurrentCompany || "Empresa atual";
  const labelOffers = section?.labelOffers || "Ofertas disponiveis";
  const ctaLabel = section?.primaryCtaLabel || "Atualizar perfil";
  const ctaUrl = section?.primaryCtaUrl || "/candidato/dados-pessoais";
  const showBar = section?.profileBarVisible !== false;

  const completion = profileSummary?.profileCompletionPercentage ?? 0;
  const safeCompletion = Math.min(100, Math.max(0, completion));
  const currentCompany = profileSummary?.currentCompanyName || "Sem empresa ativa";
  const openOffers = profileSummary?.openOffers ?? 0;

  const isLoading = summaryLoading || loadingContent;

  return (
    <section
      className="relative overflow-hidden text-primary-content"
      style={{
        backgroundImage: "url('/hero-home.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
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
      <div className="relative max-w-6xl mx-auto px-6 py-14 md:py-20">
        <div className="relative rounded-[32px] border border-primary-content/25 bg-primary-content/10 shadow-2xl backdrop-blur-md p-8 md:p-10 space-y-6">
          <div className="space-y-2 text-left">
            <h1 className="text-3xl md:text-4xl font-extrabold drop-shadow-sm">
              {greetingPrefix} {displayName}
            </h1>
          </div>

          {showBar && (
            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm text-primary-content/80">
                <span className="text-lg">Progresso do perfil</span>
                <span className="font-semibold text-primary-content">{safeCompletion}%</span>
              </div>
              <div className="h-3 rounded-full bg-primary-content/20 overflow-hidden">
                <div
                  className="h-full bg-primary-content transition-all duration-500"
                  style={{ width: `${safeCompletion}%` }}
                  aria-label={`Perfil completo em ${safeCompletion}%`}
                />
              </div>
            </div>
          )}

          <div className="grid gap-3 text-sm md:text-base text-primary-content/90">
            <span>
              <strong className="text-primary-content">{labelCompany}:</strong>{" "}
              {isLoading ? "A carregar..." : currentCompany}
            </span>
            <div className="flex items-center justify-between gap-3">
              <span className="flex-1">
                <strong className="text-primary-content">{labelOffers}:</strong>{" "}
                {isLoading ? "A carregar..." : `${openOffers} novas oportunidades`}
              </span>
              <Link to={ctaUrl} className="btn btn-primary shadow-lg whitespace-nowrap">
                {ctaLabel}
              </Link>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

HeroPanel.propTypes = {
  displayName: PropTypes.string.isRequired,
  section: PropTypes.object,
  profileSummary: PropTypes.object,
  summaryLoading: PropTypes.bool,
  loadingContent: PropTypes.bool,
};

HeroPanel.defaultProps = {
  section: null,
  profileSummary: null,
  summaryLoading: false,
  loadingContent: false,
};
