import PropTypes from "prop-types";

export default function FieldGroup({ label, value, onChange, placeholder, className }) {
  const containerClass = className ? `form-control ${className}` : "form-control";
  return (
    <label className={containerClass}>
      <span className="label-text font-semibold">{label}</span>
      <input
        type="text"
        className="input input-bordered"
        value={value}
        placeholder={placeholder}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

FieldGroup.propTypes = {
  label: PropTypes.string.isRequired,
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  onChange: PropTypes.func.isRequired,
  placeholder: PropTypes.string,
  className: PropTypes.string,
};

FieldGroup.defaultProps = {
  placeholder: "",
  className: "",
};
