import PropTypes from "prop-types";
import ShowcasePreview from "./ShowcasePreview.jsx";

export default function ShowcaseList({ title, description, items, onCreate, onEdit, onMove, type }) {
  const isIndustry = type === "industry";
  const safeItems = Array.isArray(items) ? items : [];
  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 className="card-title text-2xl">{title}</h2>
            <p className="text-base-content/70">{description}</p>
          </div>
          <button type="button" className="btn btn-primary" onClick={onCreate}>
            Adicionar {isIndustry ? "industria" : "parceiro"}
          </button>
        </div>
        {safeItems.length ? (
          <div className="space-y-4">
            {safeItems.map((item, index) => (
              <article
                key={item.id}
                className="rounded-2xl border border-base-200 p-5 flex flex-col gap-4"
              >
                <div className="flex flex-col gap-4 md:flex-row md:items-center md:gap-6">
                  <div className="w-full md:w-48 h-32 rounded-2xl overflow-hidden bg-base-200 border border-base-300">
                    <ShowcasePreview src={item.imageUrl} alt={item.name} />
                  </div>
                  <div className="flex-1 space-y-2">
                    <div className="flex flex-wrap items-center gap-3">
                      <h3 className="text-xl font-semibold">{item.name}</h3>
                      <span
                        className={`badge ${item.active ? "badge-success" : "badge-ghost"} uppercase`}
                      >
                        {item.active ? "Visiðvel" : "Oculto"}
                      </span>
                      <div className="flex items-center gap-2 text-xs text-base-content/60">
                        <button
                          type="button"
                          className="btn btn-xs btn-ghost"
                          onClick={() => onMove(item.id, "up")}
                          disabled={index === 0}
                          aria-label="Subir item"
                        >
                          <i className="bi bi-arrow-up" />
                        </button>
                        <button
                          type="button"
                          className="btn btn-xs btn-ghost"
                          onClick={() => onMove(item.id, "down")}
                          disabled={index === safeItems.length - 1}
                          aria-label="Descer item"
                        >
                          <i className="bi bi-arrow-down" />
                        </button>
                      </div>
                    </div>
                    {item.description && (
                      <p className="text-sm text-base-content/70 leading-relaxed">
                        {item.description}
                      </p>
                    )}
                  </div>
                </div>
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div className="flex flex-wrap gap-2">
                    <button type="button" className="btn btn-sm btn-outline" onClick={() => onEdit(item)}>
                      Editar
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="rounded-2xl border border-dashed border-base-300 p-10 text-center">
            <p className="text-base-content/60">
              Ainda nÆo existem {isIndustry ? "indÇ§strias" : "parceiros"} configurados.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

ShowcaseList.propTypes = {
  title: PropTypes.string.isRequired,
  description: PropTypes.string.isRequired,
  items: PropTypes.arrayOf(PropTypes.shape({ id: PropTypes.oneOfType([PropTypes.number, PropTypes.string]) })),
  onCreate: PropTypes.func.isRequired,
  onEdit: PropTypes.func.isRequired,
  onMove: PropTypes.func.isRequired,
  type: PropTypes.string.isRequired,
};

ShowcaseList.defaultProps = {
  items: [],
};
