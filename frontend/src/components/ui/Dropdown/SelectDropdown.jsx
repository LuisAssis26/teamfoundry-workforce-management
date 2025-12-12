import PropTypes from "prop-types";
import { useEffect, useRef, useState } from "react";
import { DropdownChevron, DropdownPanel, DROPDOWN_TRIGGER_CLASS } from "./Dropdown.jsx";

/**
 * Dropdown simples com label por cima, mantendo o estilo dos outros inputs custom.
 */
export default function SelectDropdown({
  label,
  value,
  onChange,
  options,
  placeholder = "Selecione",
  className = "",
  disabled = false,
}) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);

  useEffect(() => {
    const handler = (e) => {
      if (!containerRef.current?.contains(e.target)) setOpen(false);
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const currentLabel =
    options.find((opt) => opt.value === value)?.label ||
    (value ? placeholder : placeholder);

  return (
    <div className={`form-control w-full ${className}`} ref={containerRef}>
      {label && (
        <label className="label">
          <span className="label-text font-medium">{label}</span>
        </label>
      )}
      <div className="relative">
        <button
          type="button"
          className={`${DROPDOWN_TRIGGER_CLASS} h-11 min-h-11 text-left pr-2.5 focus-visible:outline-none`}
          onClick={() => !disabled && setOpen((o) => !o)}
          disabled={disabled}
        >
          <span className={`truncate ${!value ? "text-base-content/70" : ""}`}>{currentLabel}</span>
          <DropdownChevron open={open} className="text-[12px]" />
        </button>
        <DropdownPanel
          open={open && !disabled}
          options={[{ value: "", label: placeholder }, ...options]}
          onSelect={(val) => {
            onChange(val);
            setOpen(false);
          }}
          getOptionLabel={(opt) => opt.label}
          getOptionValue={(opt) => opt.value}
          renderOption={(opt, select) => (
            <button
              type="button"
              className="btn btn-ghost justify-start w-full text-left"
              onClick={select}
              disabled={opt.value === "" && disabled}
            >
              {opt.label}
            </button>
          )}
          className="left-0 right-auto min-w-full w-full"
        />
      </div>
    </div>
  );
}

SelectDropdown.propTypes = {
  label: PropTypes.string,
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
  options: PropTypes.arrayOf(
    PropTypes.shape({
      value: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
    })
  ).isRequired,
  placeholder: PropTypes.string,
  className: PropTypes.string,
  disabled: PropTypes.bool,
};
