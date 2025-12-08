import PropTypes from "prop-types";

export const DROPDOWN_TRIGGER_CLASS =
  "input input-bordered w-full px-2 flex items-center justify-between gap-2";

export const DROPDOWN_INLINE_TRIGGER_CLASS =
  "flex justify-between h-full min-w-[64px] items-center gap-2 border-r border-base-300 pl-4 pr-2 bg-transparent focus:outline-none cursor-pointer";

export function DropdownChevron({ open = false, className = "" }) {
  return (
    <i
      className={`bi bi-chevron-down text-base-content/60 chevron-rotate ${open ? "open" : ""} ${className}`}
      aria-hidden
    />
  );
}

DropdownChevron.propTypes = {
  open: PropTypes.bool,
  className: PropTypes.string,
};

/**
 * Painel base de dropdown com layout e pesquisa opcionais.
 */
export function DropdownPanel({
  open,
  options,
  onSelect,
  getOptionLabel = (opt) => opt?.label ?? opt?.value ?? "",
  getOptionValue = (opt) => opt?.value ?? opt,
  renderOption,
  showSearch = false,
  searchPlaceholder = "Pesquisar...",
  searchQuery = "",
  onSearchChange,
  className = "",
}) {
  if (!open) return null;
  return (
    <div
      className={`absolute left-0 z-20 mt-2 rounded-box border border-base-200 bg-base-100 shadow overflow-x-hidden ${className}`}
    >
      {showSearch && (
        <div className="p-2">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange?.(e.target.value)}
            placeholder={searchPlaceholder}
            className="input input-bordered input-sm w-full"
          />
        </div>
      )}
      <ul className="flex flex-col gap-1 p-2 max-h-64 overflow-auto w-full">
        {options.length === 0 ? (
          <li className="text-sm text-base-content/60 px-2 py-2">Nenhuma opção</li>
        ) : (
          options.map((opt) => {
            const value = getOptionValue(opt);
            const label = getOptionLabel(opt);
            return (
              <li key={value}>
                {renderOption ? (
                  renderOption(opt, () => onSelect?.(value))
                ) : (
                  <button
                    type="button"
                    className="btn btn-ghost justify-start"
                    onClick={() => onSelect?.(value)}
                  >
                    {label}
                  </button>
                )}
              </li>
            );
          })
        )}
      </ul>
    </div>
  );
}

DropdownPanel.propTypes = {
  open: PropTypes.bool.isRequired,
  options: PropTypes.array.isRequired,
  onSelect: PropTypes.func,
  getOptionLabel: PropTypes.func,
  getOptionValue: PropTypes.func,
  renderOption: PropTypes.func,
  showSearch: PropTypes.bool,
  searchPlaceholder: PropTypes.string,
  searchQuery: PropTypes.string,
  onSearchChange: PropTypes.func,
  className: PropTypes.string,
};
