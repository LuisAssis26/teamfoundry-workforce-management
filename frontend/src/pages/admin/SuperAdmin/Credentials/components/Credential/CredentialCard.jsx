import PropTypes from "prop-types";
import Button from "../../../../../../components/ui/Button/Button.jsx";

export default function CredentialCard({
                                                 company,
                                                 fieldConfig,
                                                 onViewMore,
                                                 onAccept,
                                                 viewLabel = "Ver Mais",
                                                 acceptLabel = "Aceitar",
                                                 viewVariant = "primary",
                                                 acceptVariant = "success",
                                                 viewButtonClassName = "",
                                                 acceptButtonClassName = "",
                                               }) {
  const visibleFields = fieldConfig.filter(({ key, getValue }) => {
    const value =
        typeof getValue === "function" ? getValue(company) : company[key];
    return value !== undefined && value !== null && `${value}`.trim() !== "";
  });

  const topRow = visibleFields.slice(0, 3);
  const bottomRow = visibleFields.slice(3);

  return (
      <article className="relative card bg-base-100 border border-base-200 shadow-md rounded-2xl overflow-hidden">
        <div className="card-body flex flex-col gap-6 md:flex-row md:gap-0 md:p-6">
          <div className="flex-1 space-y-4 md:pr-6">
            <div className="flex flex-wrap gap-x-6 gap-y-2 text-sm font-semibold text-primary">
              {topRow.map(({ key, label, getValue, className }) => {
                const value =
                    typeof getValue === "function" ? getValue(company) : company[key];
                return (
                    <span key={key} className={`flex gap-1 ${className ?? ""}`}>
                  <span>{label}</span>
                  <span className="font-medium text-base-content break-all">
                    {value}
                  </span>
                </span>
                );
              })}
            </div>

            {bottomRow.length > 0 && (
                <div className="flex flex-wrap gap-x-6 text-sm font-semibold text-primary">
                  {bottomRow.map(({ key, label, getValue, className }) => {
                    const value =
                        typeof getValue === "function" ? getValue(company) : company[key];
                    return (
                        <span key={key} className={`flex gap-1 ${className ?? ""}`}>
                    <span>{label}</span>
                    <span className="font-medium text-base-content break-all">
                      {value}
                    </span>
                  </span>
                    );
                  })}
                </div>
            )}
          </div>

          <div className="flex flex-row gap-3 md:w-60 md:border-l items-center md:border-base-200 md:pl-6">
            <Button
                label={viewLabel}
                variant={viewVariant}
                className={`flex-1 shadow-md ${viewButtonClassName}`.trim()}
                onClick={() => onViewMore(company)}
            />
            <Button
                label={acceptLabel}
                variant={acceptVariant}
                className={`flex-1 shadow-md ${acceptButtonClassName}`.trim()}
                onClick={() => onAccept(company.id)}
            />
          </div>
        </div>
      </article>
  );
}

CredentialCard.propTypes = {
  company: PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.number, PropTypes.string]).isRequired,
  }).isRequired,
  fieldConfig: PropTypes.arrayOf(
      PropTypes.shape({
        key: PropTypes.string,
        label: PropTypes.string.isRequired,
        getValue: PropTypes.func,
        className: PropTypes.string,
      })
  ).isRequired,
  onViewMore: PropTypes.func.isRequired,
  onAccept: PropTypes.func.isRequired,
  viewLabel: PropTypes.string,
  acceptLabel: PropTypes.string,
  viewVariant: PropTypes.string,
  acceptVariant: PropTypes.string,
  viewButtonClassName: PropTypes.string,
  acceptButtonClassName: PropTypes.string,
};
