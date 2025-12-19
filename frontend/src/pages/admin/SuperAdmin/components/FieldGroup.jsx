import PropTypes from "prop-types";

export default function FieldGroup({ label, value, onChange, placeholder, className }) {
  const containerClass = className
    ? `flex flex-col gap-2 items-start ${className}`
    : "flex flex-col gap-2 items-start";
  const labelText = label?.trim().endsWith(":") ? label : `${label}:`;
  return (
    <label className={containerClass}>
      <span className="label-text font-semibold">{labelText}</span>
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
