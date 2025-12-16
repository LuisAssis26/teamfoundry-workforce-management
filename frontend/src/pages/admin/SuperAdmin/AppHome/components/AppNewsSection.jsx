import PropTypes from "prop-types";

export default function AppNewsSection({ form, section, saving, onFieldChange, onSubmit }) {
  if (!form) return null;

  const selectedItems = Math.min(Math.max(Number(form.apiMaxItems || section?.apiMaxItems || 3), 1), 6);
  const previewArticles = section?.newsArticles ?? [];
  const handleCountChange = (value) => {
    onFieldChange("apiMaxItems", String(value));
  };

  const formatPreviewDate = (value) => {
    if (!value) return "Data indisponível";
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return "Data indisponível";
    }
    return parsed.toLocaleDateString("pt-PT", { day: "2-digit", month: "short" });
  };

  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-6">
        <div>
          <h2 className="card-title text-3xl">Notícias da NewsAPI</h2>
          <p className="text-base-content/70">
            As manchetes são sincronizadas automaticamente. Ajuste quantos cards deseja mostrar (máximo de 6).
          </p>
        </div>
        <form className="space-y-6" onSubmit={onSubmit}>
          <div className="rounded-2xl border border-base-200 bg-base-100 p-4 space-y-3">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-base-content">Quantidade de notícias</span>
              <span className="text-sm text-base-content/70">
                {selectedItems} {selectedItems === 1 ? "notícia" : "notícias"}
              </span>
            </div>
            <input
              type="range"
              min="1"
              max="6"
              value={selectedItems}
              className="range range-primary"
              onChange={(event) => handleCountChange(event.target.value)}
            />
            <div className="flex justify-between text-xs text-base-content/70 px-1">
              <span>Min. 1</span>
              <span>Max. 6</span>
            </div>
          </div>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-base-content">Pré-visualização</span>
              <span className="badge badge-ghost">
                {previewArticles.length ? `${previewArticles.length} artigos` : "Sem dados"}
              </span>
            </div>
            {previewArticles.length ? (
              <div className="grid gap-3 md:grid-cols-2">
                {previewArticles.slice(0, 4).map((article) => (
                  <article key={article.url} className="rounded-2xl border border-base-300 bg-base-100 p-4 space-y-2">
                    <p className="text-xs uppercase tracking-wide text-primary/70">
                      {article.sourceName ?? "Fonte externa"}
                    </p>
                    <p className="font-semibold text-base-content line-clamp-2">{article.title}</p>
                    <p className="text-xs text-base-content/60">{formatPreviewDate(article.publishedAt)}</p>
                  </article>
                ))}
              </div>
            ) : (
              <div className="rounded-2xl border border-dashed border-base-300 bg-base-100 p-4 text-sm text-base-content/70">
                Ainda não recebemos artigos da API. Guarde a configuração e verifique se a chave NEWSAPI foi definida no
                backend.
              </div>
            )}
          </div>
          <div className="flex justify-end">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? (
                <>
                  <span className="loading loading-spinner loading-sm" />
                  A guardar...
                </>
              ) : (
                "Guardar alterações"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

AppNewsSection.propTypes = {
  form: PropTypes.object,
  section: PropTypes.object,
  saving: PropTypes.bool,
  onFieldChange: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
};

AppNewsSection.defaultProps = {
  form: null,
  section: null,
  saving: false,
};
