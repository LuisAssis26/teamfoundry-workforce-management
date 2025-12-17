import { Link } from "react-router-dom";

export default function TeamManagementCard({ id, company, email, phone, workforce, status }) {
  const isComplete = (status || "").toUpperCase() === "COMPLETE";
  const buttonLabel = isComplete ? "Concluída" : "Montar";
  const buttonClass = isComplete
    ? "btn btn-success"
    : "btn btn-primary";

  return (
    <div
      className="relative rounded-2xl border border-base-300 bg-base-100 px-6 py-4 shadow-lg"
    >
      <dl className="grid grid-cols-1 gap-1 text-base text-base-content sm:grid-cols-2">
        <InfoRow label="Nome Empresa" value={company} />
        <InfoRow label="Email Responsavel" value={email} />
        <InfoRow label="Telefone Empresa" value={phone} />
        <InfoRow label="Mao de Obra" value={workforce} />
      </dl>

      <Link
        to={`/admin/team-management/requests?team=${id ?? ""}`}
        className={`absolute right-6 top-1/2 -translate-y-1/2 rounded-xl px-8 py-2 text-center text-sm font-semibold text-white shadow transition ${buttonClass}`}
      >
        {buttonLabel}
      </Link>
    </div>
  );
}

function InfoRow({ label, value }) {
  return (
    <div className="flex flex-wrap gap-2">
      <dt className="font-medium text-primary">{label}:</dt>
      <dd>{value}</dd>
    </div>
  );
}
