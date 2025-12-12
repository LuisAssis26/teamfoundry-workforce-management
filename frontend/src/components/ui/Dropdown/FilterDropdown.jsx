import PropTypes from "prop-types";
import { useEffect, useRef, useState } from "react";
import { DropdownChevron, DropdownPanel, DROPDOWN_TRIGGER_CLASS } from "./Dropdown.jsx";

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
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);

  useEffect(() => {
    const handler = (e) => {
      if (!containerRef.current?.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const currentLabel = options.find((opt) => opt.value === value)?.label || options[0]?.label || "";

  return (
    <div className={`flex items-center gap-2 ${className}`} ref={containerRef}>
      {label && <span className="text-sm font-medium whitespace-nowrap leading-none">{label}</span>}
      <div className="relative min-w-36 md:min-w-56">
        <button
          type="button"
          className={`${DROPDOWN_TRIGGER_CLASS} h-10 min-h-10 text-left pr-2 focus-visible:outline-none ${selectClassName}`}
          onClick={() => setOpen((o) => !o)}
        >
          <span className="truncate">{currentLabel}</span>
          <DropdownChevron open={open} className="text-[11px]" />
        </button>
        <DropdownPanel
          open={open}
          options={options}
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
