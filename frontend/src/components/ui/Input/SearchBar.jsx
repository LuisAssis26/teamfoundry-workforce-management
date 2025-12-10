import PropTypes from "prop-types";

/**
 * Barra de pesquisa reutilizável com ícone integrado.
 */
export default function SearchBar({
  value,
  onChange,
  placeholder = "Pesquisar",
  className = "",
  inputClassName = "",
  size = "md",
}) {
  const sizeClass = size === "sm" ? "input-sm" : "input-md";
  return (
    <label className={`input input-bordered flex items-center gap-2 ${sizeClass} ${className}`.trim()}>
      <input
        type="search"
        className={`grow ${size === "sm" ? "text-sm" : ""} ${inputClassName}`.trim()}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
      />
      <i className="bi bi-search text-base-content/75" aria-hidden />
    </label>
  );
}

SearchBar.propTypes = {
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
  placeholder: PropTypes.string,
  className: PropTypes.string,
  inputClassName: PropTypes.string,
  size: PropTypes.oneOf(["sm", "md"]),
};
