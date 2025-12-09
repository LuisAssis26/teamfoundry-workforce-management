import PropTypes from "prop-types";

export default function NewsSection({ section, articles, loading, fallbackNews }) {
  const targetCount = section?.apiMaxItems ?? fallbackNews.length;
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
                    <h3 className="text-lg font-semibold text-base-content">{article.title}</h3>
                    {article.description && (
                      <p className="text-sm text-base-content/70 line-clamp-3">{article.description}</p>
                    )}
                    <div className="mt-auto flex items-center justify-between text-xs text-base-content/60">
                      <span>{formatPublishedDate(article.publishedAt) ?? "Atualizado hoje"}</span>
                      <a href={article.url} className="link link-primary font-semibold" target="_blank" rel="noreferrer">
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
      </div>
    </section>
  );
}

NewsSection.propTypes = {
  section: PropTypes.object,
  articles: PropTypes.arrayOf(PropTypes.object),
  loading: PropTypes.bool,
  fallbackNews: PropTypes.arrayOf(PropTypes.any).isRequired,
};

NewsSection.defaultProps = {
  section: null,
  articles: [],
  loading: false,
};
