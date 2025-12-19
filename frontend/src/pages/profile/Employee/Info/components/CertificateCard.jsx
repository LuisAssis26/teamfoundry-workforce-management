import PropTypes from "prop-types";
import { formatDate } from "../../../../../utils/dateUtils.js";

export default function CertificateCard({ education, onEdit }) {
  const { name, institution, location, completionDate, description, certificateUrl, certificateFileName } = education;
  const isHttpLink = certificateUrl && (certificateUrl.startsWith("http://") || certificateUrl.startsWith("https://"));

  return (
    <div className="rounded-xl border border-base-300 bg-base-100 shadow-sm overflow-hidden">
      <div className="flex items-center justify-between px-4 py-3">
        <div className="flex items-center gap-3">
          <div className="h-8 w-8 rounded-md bg-base-200 flex items-center justify-center">
            <i className="bi bi-mortarboard" aria-hidden="true" />
          </div>
          <div className="flex flex-col">
            <span className="font-semibold">{name}</span>
            <span className="text-sm text-base-content/70">
              Instituição: {institution}
              {location ? ` · ${location}` : ""}
            </span>
          </div>
        </div>
        <button
          type="button"
          className="btn btn-sm btn-ghost"
          onClick={() => onEdit(education)}
        >
          <i className="bi bi-pencil-square mr-1" aria-hidden="true" />
          Editar
        </button>
      </div>
      <div className="border-t border-base-300 px-4 py-3 flex flex-col gap-1 text-sm">
        <span className="text-base-content/80">Concluído em: {formatDate(completionDate)}</span>
        {description && <span className="text-base-content/80">{description}</span>}
        {isHttpLink && (
          <a
            href={certificateUrl}
            target="_blank"
            rel="noreferrer"
            className="link link-primary text-sm mt-1 inline-flex items-center gap-1"
          >
            <i className="bi bi-paperclip" aria-hidden="true" />
            {certificateFileName || "Ver certificado"}
          </a>
        )}
      </div>
    </div>
  );
}

CertificateCard.propTypes = {
  education: PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    name: PropTypes.string,
    institution: PropTypes.string,
    location: PropTypes.string,
    completionDate: PropTypes.oneOfType([PropTypes.string, PropTypes.instanceOf(Date)]),
    description: PropTypes.string,
    certificateUrl: PropTypes.string,
  }).isRequired,
  onEdit: PropTypes.func.isRequired,
};
