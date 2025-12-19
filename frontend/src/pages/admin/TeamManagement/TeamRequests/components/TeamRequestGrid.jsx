import React from "react";
import PropTypes from "prop-types";
import TeamRequestCard from "./TeamRequestCard.jsx";

export default function TeamRequestGrid({ requests, onSelect }) {
  return (
    <section className="rounded-2xl border border-base-200 bg-base-100 p-8 shadow-lg">
      <div className="flex flex-col gap-4">
        {requests.map((request) => (
          <TeamRequestCard
            key={request.id}
            role={request.role}
            workforceCurrent={request.workforceCurrent}
            workforceTotal={request.workforceTotal}
            proposalsSent={request.proposalsSent}
            status={request.status}
            onAssemble={onSelect ? () => onSelect(request) : undefined}
            onCompleteClick={onSelect ? () => onSelect(request) : undefined}
          />
        ))}
      </div>
    </section>
  );
}

TeamRequestGrid.propTypes = {
  requests: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
      role: PropTypes.string.isRequired,
      workforceCurrent: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
      workforceTotal: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
      proposalsSent: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
      status: PropTypes.string,
    })
  ).isRequired,
  onSelect: PropTypes.func,
};

TeamRequestGrid.defaultProps = {
  onSelect: undefined,
};
