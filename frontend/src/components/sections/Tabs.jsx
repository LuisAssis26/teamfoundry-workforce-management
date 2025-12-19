import PropTypes from "prop-types";
import { NavLink, useLocation } from "react-router-dom";
import { useLayoutEffect, useRef, useState } from "react";

/**
 * Abstração reutilizável de tabs com indicador animado.
 * Recebe um array de tabs { to, label } e usa NavLink para navegação.
 */
export default function Tabs({ tabs, className = "", activeKey, onTabChange }) {
  const location = useLocation();
  const itemRefs = useRef([]);
  const [indicatorStyle, setIndicatorStyle] = useState(() =>
    lastIndicator.hasValue ? { width: lastIndicator.width, left: lastIndicator.left } : { width: 0, left: 0 }
  );
  const [ready, setReady] = useState(lastIndicator.hasValue);

  useLayoutEffect(() => {
    const activeIndex = onTabChange
      ? tabs.findIndex((tab) => tab.key === activeKey)
      : tabs.findIndex((tab) => tab.to && location.pathname.startsWith(tab.to));
    const el = itemRefs.current[activeIndex];
    if (!el) return;
    const measure = () => {
      const nextStyle = { width: el.offsetWidth, left: el.offsetLeft };
      setIndicatorStyle(nextStyle);
      lastIndicator = { ...nextStyle, hasValue: true };
      setReady(true);
    };
    // mede no próximo frame para garantir layout calculado
    const raf = requestAnimationFrame(measure);
    return () => cancelAnimationFrame(raf);
  }, [location.pathname, tabs]);

  useLayoutEffect(() => {
    const handleResize = () => {
      const activeIndex = onTabChange
        ? tabs.findIndex((tab) => tab.key === activeKey)
        : tabs.findIndex((tab) => tab.to && location.pathname.startsWith(tab.to));
      const el = itemRefs.current[activeIndex];
      if (el) {
        setIndicatorStyle({ width: el.offsetWidth, left: el.offsetLeft });
      }
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, [location.pathname, tabs]);

  return (
    <div
      className={`mt-6 flex items-center justify-center gap-6 border-b border-base-300 text-sm md:text-base relative ${className}`}
    >
      {tabs.map(({ to, label, key }, index) => {
        const isActive = onTabChange
          ? key === activeKey
          : to && location.pathname.startsWith(to);
        const baseClasses = "relative pb-2 -mb-px transition-colors duration-600";
        const stateClasses = isActive
          ? "text-base-content font-semibold"
          : "text-base-content/70 hover:text-base-content";
        const classes = `${baseClasses} ${stateClasses}`;

        if (onTabChange) {
          return (
            <button
              key={key || label}
              type="button"
              ref={(el) => {
                itemRefs.current[index] = el;
              }}
              className={`${classes} cursor-pointer`}
              onClick={() => onTabChange(key)}
              draggable={false}
              onDragStart={(e) => e.preventDefault()}
            >
              {label}
            </button>
          );
        }

        return (
          <NavLink
            key={to}
            to={to}
            ref={(el) => {
              itemRefs.current[index] = el;
            }}
            className={({ isActive: navActive }) => {
              const state = navActive
                ? "text-base-content font-semibold text-center"
                : "text-base-content/70 hover:text-base-content text-center";
              return `${baseClasses} ${state} cursor-pointer`;
            }}
            draggable={false}
            onDragStart={(e) => e.preventDefault()}
          >
            {label}
          </NavLink>
        );
      })}
      <span
        className="absolute bottom-[-2px] h-0.5 bg-base-content"
        style={{
          width: indicatorStyle.width,
          left: indicatorStyle.left,
          transition: ready ? "all 260ms ease" : "none",
        }}
        aria-hidden="true"
      />
    </div>
  );
}

Tabs.propTypes = {
  tabs: PropTypes.arrayOf(
    PropTypes.shape({
      to: PropTypes.string,
      label: PropTypes.string.isRequired,
      key: PropTypes.string,
    })
  ).isRequired,
  className: PropTypes.string,
  activeKey: PropTypes.string,
  onTabChange: PropTypes.func,
};
// Guarda última posição do indicador entre desmontagens para evitar que recomece da esquerda.
let lastIndicator = { width: 0, left: 0, hasValue: false };
