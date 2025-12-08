import { useMemo, useState } from "react";
import PropTypes from "prop-types";
import phoneCountries from "./data/phoneCountries.js";

const FALLBACK_COUNTRY = phoneCountries[0];
/**
 * Componente de telefone com dropdown de prefixo + input livre.
 * Mantém o valor normalizado em E.164 (`+<prefixo><local>`).
 */
export default function PhoneInput({
  label = "Telefone",
  value = "",
  onChange,
  countries = phoneCountries,
  error,
  placeholder = "912 345 678",
  className = "",
  disabled = false,
}) {
  const initialCountry = useMemo(() => {
    if (value?.startsWith("+")) {
      const match = countries.find((c) => value.startsWith(`+${c.dialCode}`));
      if (match) return match;
    }
    return countries[0] || FALLBACK_COUNTRY;
  }, [countries, value]);

  const [open, setOpen] = useState(false);
  const [country, setCountry] = useState(initialCountry);
  const [localValue, setLocalValue] = useState(extractLocal(value, initialCountry?.dialCode));

  const handleSelect = (c) => {
    if (disabled) return;
    setCountry(c);
    const next = formatE164(c.dialCode, localValue);
    onChange?.(next);
    setOpen(false);
  };

  const handleLocalChange = (e) => {
    if (disabled) return;
    const digits = e.target.value.replace(/\D/g, "");
    setLocalValue(digits);
    const next = formatE164(country.dialCode, digits);
    onChange?.(next);
  };

  return (
    <div className={`form-control w-full ${className}`}>
      {label && (
        <label className="label">
          <span className="label-text font-medium">{label}</span>
        </label>
      )}
      <div className={`relative flex h-10 w-full items-stretch input input-bordered p-0 focus-within:border-primary focus-within:ring-0 focus-within:outline-none transition   ${disabled ? "pointer-events-none opacity-60" : ""}`}>
        <div className="relative inline-block">
          <button
            type="button"
            className="flex justify-between h-full min-w-[64px] items-center gap-2 border-r border-base-300 pl-4 pr-2 bg-transparent focus:outline-none cursor-pointer"
            onClick={() => !disabled && setOpen((o) => !o)}
            aria-label="Selecionar indicativo telefónico"
          >
            <span className="font-semibold text-base">+{country.dialCode}</span>
            <i className={`bi bi-chevron-${open ? "down" : "up"} text-xs`} />
          </button>
          {open && (
            <ul className="absolute z-50 max-h-60 w-56 overflow-auto rounded-lg border border-base-300 bg-base-100 shadow-lg bottom-full left-0 mb-1 text-base-content">
              {countries.map((c) => (
                <li key={c.code}>
                  <button
                    type="button"
                    onClick={() => handleSelect(c)}
                    className="flex w-full items-center justify-between px-3 py-2 hover:bg-base-200 cursor-pointer"
                  >
                    <span className="font-semibold">+{c.dialCode}</span>
                    <span className="text-[11px] text-base-content/70 truncate ml-2">{c.name}</span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
        <input
          type="tel"
          className="h-full w-full bg-transparent px-2 outline-none"
          placeholder={placeholder}
          value={localValue}
          onChange={handleLocalChange}
          disabled={disabled}
          aria-label="Número de telefone"
        />
      </div>
      {error && <p className="mt-2 text-sm text-error">{error}</p>}
    </div>
  );
}

PhoneInput.propTypes = {
  label: PropTypes.string,
  value: PropTypes.string,
  onChange: PropTypes.func,
  countries: PropTypes.arrayOf(
    PropTypes.shape({
      code: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
      dialCode: PropTypes.string.isRequired,
      flag: PropTypes.string.isRequired,
    })
  ),
  error: PropTypes.string,
  placeholder: PropTypes.string,
  className: PropTypes.string,
  disabled: PropTypes.bool,
};

function formatE164(dialCode, localDigits) {
  const digits = (localDigits || "").replace(/\D/g, "");
  return digits ? `+${dialCode}${digits}` : `+${dialCode}`;
}

function extractLocal(full, dialCode) {
  if (!full || !full.startsWith("+")) return "";
  const prefix = `+${dialCode}`;
  if (full.startsWith(prefix)) {
    return full.slice(prefix.length);
  }
  return full.replace(/\D/g, "");
}
