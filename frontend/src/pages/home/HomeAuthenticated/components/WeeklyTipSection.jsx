import PropTypes from "prop-types";
import { Link } from "react-router-dom";

export default function WeeklyTipSection({ section, tipOfWeek, error, fallbackTip }) {
  const hasTip = Boolean(tipOfWeek);
  const sectionTitle = section?.title ?? "Dica da Semana";
  const tipTitle = hasTip ? tipOfWeek.title : fallbackTip.title;
  const category = hasTip ? tipOfWeek.category : null;
  const descriptionText = hasTip ? tipOfWeek.description : fallbackTip.description.join("\n");
  const paragraphs = descriptionText ? descriptionText.split("\n").filter(Boolean) : [];
  const ctaLabel = section?.primaryCtaLabel ?? "Ver mais dicas";
  const ctaUrl = section?.primaryCtaUrl ?? "/dicas";

  return (
    <section className="max-w-6xl mx-auto px-6">
      <div className="rounded-3xl border border-base-200 bg-base-100 shadow p-8">
        <p className="text-sm uppercase tracking-[0.4em] text-primary/80 mb-2">{sectionTitle}</p>
        <div className="flex flex-col gap-2">
          {tipTitle && <h3 className="text-2xl font-semibold text-base-content">{tipTitle}</h3>}
          {category && <p className="text-sm font-medium text-base-content/70">{category}</p>}
          {error && <p className="text-xs text-error/80">{error}</p>}
          {paragraphs.map((paragraph, index) => (
            <p key={index} className="text-base-content/70 text-sm leading-relaxed">
              {paragraph}
            </p>
          ))}
        </div>
        <div className="mt-4">
          <Link to={ctaUrl} className="btn btn-primary btn-sm">
            {ctaLabel}
          </Link>
        </div>
      </div>
    </section>
  );
}

WeeklyTipSection.propTypes = {
  section: PropTypes.object,
  tipOfWeek: PropTypes.object,
  error: PropTypes.string,
  fallbackTip: PropTypes.shape({
    title: PropTypes.string.isRequired,
    description: PropTypes.arrayOf(PropTypes.string).isRequired,
  }).isRequired,
};

WeeklyTipSection.defaultProps = {
  section: null,
  tipOfWeek: null,
  error: null,
};
