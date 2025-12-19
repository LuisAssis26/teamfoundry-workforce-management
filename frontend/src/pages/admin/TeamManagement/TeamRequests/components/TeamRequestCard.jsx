import React from "react";
import PropTypes from "prop-types";

export default function TeamRequestCard({
  role,
  workforceCurrent,
  workforceTotal,
  proposalsSent,
  status,
  onAssemble,
  onCompleteClick,
}) {
  const isComplete = status?.toUpperCase() === "COMPLETE";
  const buttonLabel = isComplete ? "Completo" : "Montar";
  const handleClick = isComplete ? onCompleteClick || onAssemble : onAssemble;

  return (
    <div
      className="relative rounded-2xl border border-base-300 bg-base-100 px-6 py-4 shadow-lg"
    >
      <dl className="grid grid-cols-1 gap-1 text-base text-base-content sm:grid-cols-2">
        <InfoRow label="Funcao" value={role} />
        <InfoRow label="Mao de Obra" value={`${workforceCurrent} de ${workforceTotal}`} />
        <InfoRow label="Propostas Enviadas" value={proposalsSent} />
      </dl>

      <div className="absolute right-6 top-1/2 -translate-y-1/2">
        <button
          type="button"
          onClick={handleClick}
          className={`rounded-xl px-8 py-2 text-center text-sm font-semibold text-white shadow transition ${
            isComplete
              ? "btn btn-success"
              : "btn btn-primary"
          }`}
        >
          {buttonLabel}
        </button>
      </div>
    </div>
  );
}

function InfoRow({ label, value }) {
  return (
    <div className="flex flex-wrap gap-2">
      <dt className="font-medium text-primary">{label}:</dt>
      <dd className="text-base-content">{value}</dd>
    </div>
  );
}

TeamRequestCard.propTypes = {
  role: PropTypes.string.isRequired,
  workforceCurrent: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  workforceTotal: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  proposalsSent: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  status: PropTypes.string,
  onAssemble: PropTypes.func,
  onCompleteClick: PropTypes.func,
};

TeamRequestCard.defaultProps = {
  status: "IN_PROGRESS",
  onAssemble: undefined,
  onCompleteClick: undefined,
};
