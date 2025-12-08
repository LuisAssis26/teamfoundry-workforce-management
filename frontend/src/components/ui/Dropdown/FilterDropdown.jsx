import PropTypes from "prop-types";

/**
 * Dropdown de filtro compacto com label e truncamento do texto das opções.
 */
export default function FilterDropdown({
  label,
  value,
  onChange,
  options,
  className = "",
  selectClassName = "",
}) {
  return (
    <div className={`flex items-center gap-2 ${className}`}>
      {label && <span className="text-sm font-medium">{label}</span>}
      <select
        className={`select select-sm select-ghost border border-base-300 bg-base-100 truncate ${selectClassName}`}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      >
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}

FilterDropdown.propTypes = {
  label: PropTypes.string,
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
  options: PropTypes.arrayOf(
    PropTypes.shape({
      value: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
    })
  ).isRequired,
  className: PropTypes.string,
  selectClassName: PropTypes.string,
};
