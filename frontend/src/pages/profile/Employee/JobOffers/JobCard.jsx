/**
 * Cartão reutilizável para exibir participação do colaborador numa equipa/projeto.
 */
export default function JobCard({ job, showAccepted = true, actionSlot = null }) {
  const {
    teamName,
    companyName,
    location,
    description,
    startDate,
    endDate,
    acceptedDate,
    requestedRole,
  } = job;

  return (
    <div className="rounded-xl border border-base-300 bg-base-100 shadow-sm overflow-hidden">
      <div className="flex items-start justify-between px-4 py-3">
        <div className="flex items-center gap-3">
          <div className="flex flex-col">
            <span className="font-semibold">{teamName || "Equipa sem nome"}</span>
            <span className="text-sm text-base-content/70">
              {companyName || "Empresa não informada"}
            </span>
            <span className="text-sm text-base-content/70">
              {location || "Local não informado"}
            </span>
          </div>
        </div>
        <div className="text-right text-sm text-base-content/70">
          <div>{formatDateRange(startDate, endDate)}</div>
          {requestedRole && (
            <span className="badge badge-ghost mt-1">{requestedRole}</span>
          )}
        </div>
      </div>

      {description && (
        <div className="border-t border-base-300 px-4 py-3 text-sm text-base-content/90">
          {description}
        </div>
      )}

      {showAccepted && (
        <div className="border-t border-base-300 px-4 py-3 flex items-center justify-between gap-3 text-sm">
          <div className="flex items-center gap-3">
            <i className="bi bi-calendar-check" aria-hidden="true" />
            <span className="text-base-content/90">
              Associado em: {formatDate(acceptedDate)}
            </span>
          </div>
          {actionSlot && <div className="flex items-center gap-2">{actionSlot}</div>}
        </div>
      )}

      {!showAccepted && actionSlot && (
        <div className="border-t border-base-300 px-4 py-3 flex justify-end gap-2">
          {actionSlot}
        </div>
      )}
    </div>
  );
}

function formatDateRange(start, end) {
  const options = { year: "numeric", month: "short" };
  const fmt = (date) => (date ? new Date(date).toLocaleDateString("pt-PT", options) : "—");
  return `${fmt(start)} · ${fmt(end)}`;
}

function formatDate(date) {
  if (!date) return "—";
  return new Date(date).toLocaleDateString("pt-PT", {
    year: "numeric",
    month: "short",
    day: "2-digit",
  });
}
