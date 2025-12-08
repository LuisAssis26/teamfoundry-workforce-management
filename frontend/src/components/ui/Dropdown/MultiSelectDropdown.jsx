import React, { useEffect, useMemo, useRef, useState } from "react";
import PropTypes from "prop-types";

const MAX_VISIBLE_CHIPS = 2;

export default function MultiSelectDropdown({
                                              label,
                                              options,
                                              selectedOptions,
                                              onChange,
                                              placeholder = "Selecione uma ou mais opções",
                                              disabled = false,
                                              maxVisibleChips = MAX_VISIBLE_CHIPS,
                                            }) {
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState("");
  const containerRef = useRef(null);
  const chipsRef = useRef(null);
  const [hasOverflowed, setHasOverflowed] = useState(false);

  const normalizedOptions = useMemo(
      () =>
          options.map((option) =>
              typeof option === "string" ? { label: option, value: option } : option,
          ),
      [options],
  );

  const filteredOptions = useMemo(() => {
    if (!query.trim()) return normalizedOptions;
    const q = query.trim().toLowerCase();
    return normalizedOptions.filter(({ label, value }) => {
      const text = (label || value || "").toLowerCase();
      return text.includes(q);
    });
  }, [normalizedOptions, query]);

  const shouldCollapse = hasOverflowed || selectedOptions.length > maxVisibleChips;

  const { visibleChips, hiddenChips } = useMemo(() => {
    if (!shouldCollapse) {
      return { visibleChips: selectedOptions, hiddenChips: [] };
    }
    const visible = selectedOptions.slice(0, maxVisibleChips);
    const hidden = selectedOptions.slice(maxVisibleChips);
    return { visibleChips: visible, hiddenChips: hidden };
  }, [selectedOptions, shouldCollapse, maxVisibleChips]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (!containerRef.current?.contains(event.target)) {
        setIsOpen(false);
        setQuery("");
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    if (disabled && isOpen) {
      setIsOpen(false);
      setQuery("");
    }
  }, [disabled, isOpen]);

  useEffect(() => {
    setHasOverflowed(false);
  }, [selectedOptions.length]);

  useEffect(() => {
    if (!chipsRef.current) {
      return;
    }

    const observer = new ResizeObserver(() => {
      if (!chipsRef.current || hasOverflowed) {
        return;
      }
      const { scrollWidth, clientWidth } = chipsRef.current;
      if (scrollWidth > clientWidth) {
        setHasOverflowed(true);
      }
    });

    observer.observe(chipsRef.current);
    return () => observer.disconnect();
  }, [hasOverflowed]);

  const toggleOption = (value) => {
    if (disabled) return;
    if (selectedOptions.includes(value)) {
      onChange(selectedOptions.filter((item) => item !== value));
    } else {
      onChange([...selectedOptions, value]);
    }
    setIsOpen(false);
    setQuery("");
  };

  const removeOption = (value) => {
    onChange(selectedOptions.filter((item) => item !== value));
  };

  const handleTriggerKeyDown = (event) => {
    if (disabled) return;
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      setIsOpen((prev) => !prev);
      if (!isOpen) setQuery("");
    }
  };

  return (
      <div className="form-control w-full" ref={containerRef}>
        {label && (
            <label className="label">
              <span className="label-text font-medium">{label}</span>
            </label>
        )}

        <div className="relative">
          <div
              role="button"
              tabIndex={0}
              className={`input input-bordered w-full px-2 flex items-center justify-between gap-2 ${disabled ? "opacity-60 cursor-not-allowed" : ""} ${
                  selectedOptions.length === 0 ? "text-base-content/70" : ""
              }`}
              onClick={() => !disabled && setIsOpen((prev) => !prev)}
              onKeyDown={handleTriggerKeyDown}
              aria-disabled={disabled}
          >
            <div
                ref={chipsRef}
                className="flex-1 flex items-center gap-2 overflow-hidden whitespace-nowrap"
            >
              {selectedOptions.length === 0 && <span>{placeholder}</span>}
              {visibleChips.map((value) => (
                  <span
                      key={value}
                      className="badge badge-primary gap-1 rounded-md px-2 py-3 text-sm"
                      title={value}
                  >
                {value}
                    <button
                        type="button"
                        aria-label={`Remover ${value}`}
                        className="ml-1 h-5 w-5 flex items-center justify-center rounded-sm bg-transparent hover:bg-transparent focus:bg-transparent active:bg-transparent cursor-pointer"
                        onClick={(event) => {
                          event.stopPropagation();
                          removeOption(value);
                        }}
                    >
                  ×
                </button>
              </span>
              ))}
              {hiddenChips.length > 0 && (
                  <span
                      className="badge badge-outline"
                      title={`Também: ${hiddenChips.join(", ")}`}
                  >
                +{hiddenChips.length}
              </span>
              )}
            </div>
            <i
                className={`bi bi-chevron-${isOpen ? "up" : "down"} text-base-content/60`}
                aria-hidden
            ></i>
          </div>
          {isOpen && !disabled && (
              <div className="absolute left-0 right-0 z-20 mt-2 rounded-box border border-base-200 bg-base-100 shadow">
                <div className="p-2">
                  <input
                      type="text"
                      value={query}
                      onChange={(e) => setQuery(e.target.value)}
                      placeholder="Pesquisar..."
                      className="input input-bordered input-sm w-full"
                  />
                </div>
                <ul className="menu menu-sm p-2 max-h-64 overflow-auto">
                  {filteredOptions.length === 0 ? (
                      <li className="text-sm text-base-content/60 px-2 py-2">Nenhuma opção</li>
                  ) : (
                      filteredOptions.map(({ value, label: optionLabel }) => (
                          <li key={value}>
                            <button
                                type="button"
                                className="btn btn-ghost justify-start"
                                onClick={() => toggleOption(value)}
                            >
                              <i
                                  className={`bi mr-2 ${
                                      selectedOptions.includes(value)
                                          ? "bi-check-circle-fill text-success"
                                          : "bi-plus-circle text-base-content/60"
                                  }`}
                              ></i>
                              {optionLabel}
                            </button>
                          </li>
                      ))
                  )}
                </ul>
              </div>
          )}
        </div>
      </div>
  );
}

MultiSelectDropdown.propTypes = {
  label: PropTypes.string,
  options: PropTypes.arrayOf(
      PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.shape({
          label: PropTypes.string.isRequired,
          value: PropTypes.string.isRequired,
        }),
      ]),
  ).isRequired,
  selectedOptions: PropTypes.arrayOf(PropTypes.string).isRequired,
  onChange: PropTypes.func.isRequired,
  placeholder: PropTypes.string,
  disabled: PropTypes.bool,
  maxVisibleChips: PropTypes.number,
};
