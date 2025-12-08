import { useMemo, useState, useEffect } from "react";
import PropTypes from "prop-types";
import phoneCountries from "./data/phoneCountries.js";
import { DropdownChevron, DROPDOWN_INLINE_TRIGGER_CLASS, DropdownPanel } from "../Dropdown/Dropdown.jsx";

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

  // Atualiza seleção e número local quando o valor vindo de fora mudar (ex.: carregamento assíncrono).
  useEffect(() => {
    const nextCountry = value?.startsWith("+")
      ? countries.find((c) => value.startsWith(`+${c.dialCode}`)) || countries[0] || FALLBACK_COUNTRY
      : countries[0] || FALLBACK_COUNTRY;
    setCountry(nextCountry);
    setLocalValue(extractLocal(value, nextCountry?.dialCode));
  }, [value, countries]);

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
            className={`${DROPDOWN_INLINE_TRIGGER_CLASS}`}
            onClick={() => !disabled && setOpen((o) => !o)}
            aria-label="Selecionar indicativo telefónico"
          >
            <span className="font-semibold text-base">+{country.dialCode}</span>
            <DropdownChevron open={open} className="text-xs" />
          </button>
          <DropdownPanel
            open={open}
            options={countries}
            onSelect={(code) => {
              const selected = countries.find((c) => c.code === code);
              if (selected) handleSelect(selected);
            }}
            getOptionLabel={(c) => c.name}
            getOptionValue={(c) => c.code}
            renderOption={(c, select) => (
              <button
                type="button"
                className="flex w-full items-center justify-start gap-3 px-3 py-2 hover:bg-base-200 cursor-pointer text-left"
                onClick={select}
              >
                <span className="font-semibold whitespace-nowrap">+{c.dialCode}</span>
                <span className="text-[12px] text-base-content/80 flex-1 text-left whitespace-normal">
                  {c.name}
                </span>
              </button>
            )}
            className="bottom-full right-0 mb-1 w-64 max-w-56 overflow-hidden"
          />
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
