import { NavLink } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import PropTypes from "prop-types";

/**
 * Sidebar reutilizável (desktop + mobile) com indicador animado.
 */
export default function Sidebar({
  items,
  activePath,
  onNavigate,
  isMobile = false,
  animateOut = false,
  logo,
  title,
  wrapperClassName = "",
  panelClassName = "",
  listClassName = "",
}) {
  const itemRefs = useRef([]);
  const [indicator, setIndicator] = useState({ top: 0, height: 0 });

  useEffect(() => {
    const activeIndex = items.findIndex(({ to, matches }) => {
      const isMatch = matches?.some((path) => activePath?.startsWith(path)) ?? false;
      return activePath?.startsWith(to) || isMatch;
    });
    const el = itemRefs.current[activeIndex];
    if (el) {
      setIndicator({ top: el.offsetTop, height: el.offsetHeight });
    }
  }, [activePath, items]);

  const navWrapperClasses = isMobile ? "w-72 max-w-xs h-full" : "hidden md:block w-64 px-4";
  const navClasses = isMobile
    ? "bg-accent text-primary-content h-full flex flex-col rounded-r-2xl shadow-xl"
    : "bg-accent text-primary-content rounded-lg shadow-lg sticky top-6 px-0 py-6 h-screen";

  return (
    <aside
      className={`${navWrapperClasses} ${
        isMobile ? (animateOut ? "animate-drawer-out" : "animate-drawer-in") : ""
      } ${wrapperClassName}`}
    >
      <div className={`${navClasses} ${panelClassName}`}>
        {isMobile && (
          <div className="flex items-center justify-between px-4 py-3 border-b border-primary-content/20">
            <NavLink className="flex items-center gap-2"
                      to="/">
              {logo && <img src={logo} alt={title || "logo"} className="h-8 w-8 object-contain" />}
              {title && <span className="font-semibold tracking-wide">{title}</span>}
                          
            </NavLink>
            <div className="flex items-center gap-2">
              
            </div>
            <button
              type="button"
              className="btn btn-ghost btn-sm text-primary-content"
              onClick={onNavigate}
            >
              <i className="bi bi-x-lg text-lg" aria-hidden="true" />
              <span className="sr-only">Fechar menu</span>
            </button>
          </div>
        )}

        <ul className={`relative flex flex-col gap-4 py-4 pl-3 ${listClassName}`}>
          {indicator.height > 0 && (
            <span
              className="absolute left-1 w-1 rounded-full bg-primary-content/80 transition-all duration-300 ease-out"
              style={{ top: indicator.top + 4, height: indicator.height - 8 }}
              aria-hidden="true"
            />
          )}
          {items.map(({ to, label, icon, matches }) => (
            <li key={to} className="pr-2">
              <NavLink
                to={to}
                className={({ isActive }) => {
                  const isMatch = matches?.some((path) => activePath?.startsWith(path)) ?? false;
                  const active = isActive || isMatch;
                  const base = "flex items-center gap-3 px-6 py-3 rounded-lg transition-all";
                  const state = active
                    ? "bg-primary-content/15 font-semibold shadow-sm"
                    : "hover:bg-primary-content/10";
                  return `${base} ${state}`;
                }}
                onClick={onNavigate}
                ref={(el) => {
                  if (el) {
                    const idx = items.findIndex((link) => link.to === to);
                    itemRefs.current[idx] = el;
                  }
                }}
              >
                {icon && <i className={`bi ${icon} text-xl`} aria-hidden="true" />}
                <span>{label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </div>
    </aside>
  );
}

Sidebar.propTypes = {
  items: PropTypes.arrayOf(
    PropTypes.shape({
      to: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      icon: PropTypes.string,
      matches: PropTypes.arrayOf(PropTypes.string),
    })
  ).isRequired,
  activePath: PropTypes.string,
  onNavigate: PropTypes.func,
  isMobile: PropTypes.bool,
  animateOut: PropTypes.bool,
  logo: PropTypes.string,
  title: PropTypes.string,
  wrapperClassName: PropTypes.string,
  panelClassName: PropTypes.string,
  listClassName: PropTypes.string,
};
