import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import PropTypes from "prop-types";
import { clearTokens } from "../../auth/tokenStorage.js";
import logo from "../../assets/images/logo/teamFoundry_LogoPrimary.png";

const SUPER_NAV_LINKS = [
  { to: "/admin/super/credenciais", label: "Credenciais" },
  { to: "/admin/super/gestao-trabalho", label: "Gestão de trabalho" },
  { to: "/admin/super/gestao-site", label: "Gestão do Site" },
  { to: "/admin/super/metricas", label: "Métricas" },
  { to: "/admin/super/logs", label: "Logs" },
];

export default function AdminNavbar({ variant }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [underline, setUnderline] = useState({ width: 0, left: 0 });
  const profileRef = useRef(null);
  const navListRef = useRef(null);

  const isSuper = variant === "super" || location.pathname.startsWith("/admin/super");
  const navLinks = isSuper ? SUPER_NAV_LINKS : [];
  const homePath = isSuper ? "/admin/super/credenciais" : "/admin/team-management";
  const logoutPath = isSuper ? "/admin" : "/admin/login";

  const handleLogout = () => {
    clearTokens();
    setIsProfileOpen(false);
    navigate(logoutPath, { replace: true });
  };

  useEffect(() => {
    setIsProfileOpen(false);
  }, [location.pathname]);

  useLayoutEffect(() => {
    if (!navListRef.current) return;
    const activeLink = navListRef.current.querySelector('a[aria-current="page"]');
    if (!activeLink) {
      setUnderline({ width: 0, left: 0 });
      return;
    }
    const linkRect = activeLink.getBoundingClientRect();
    const listRect = navListRef.current.getBoundingClientRect();
    setUnderline({
      width: linkRect.width,
      left: linkRect.left - listRect.left,
    });
  }, [location.pathname]);

  useEffect(() => {
    function handleOutsideClick(event) {
      if (profileRef.current && !profileRef.current.contains(event.target)) {
        setIsProfileOpen(false);
      }
    }

    if (isProfileOpen) {
      document.addEventListener("mousedown", handleOutsideClick);
    }
    return () => document.removeEventListener("mousedown", handleOutsideClick);
  }, [isProfileOpen]);

  return (
    <header className="bg-base-100 border-b border-base-200 shadow-sm">
      <div className="mx-auto flex flex-col gap-4 md:flex-row md:items-center md:justify-between h-auto max-w-6xl px-6 py-4">
        <Link to={homePath} className="flex items-center gap-3 shrink-0">
          <div className="h-10 w-10">
            <img src={logo} alt="TeamFoundry" className="h-10 w-10 object-contain" />
          </div>
          <span className="font-semibold tracking-[0.2em] uppercase text-primary">TeamFoundry</span>
        </Link>

        {navLinks.length > 0 && (
          <nav className="w-full md:w-auto">
            <ul
              ref={navListRef}
              className="relative flex flex-wrap items-center gap-6 text-sm md:text-base font-medium"
            >
              {navLinks.map(({ to, label }) => (
                <li key={to}>
                  <NavLink
                    to={to}
                    className={({ isActive }) =>
                      [
                        "relative px-1 pb-2 transition-colors duration-200",
                        isActive ? "text-primary font-semibold" : "text-base-content/70 hover:text-primary",
                      ].join(" ")
                    }
                  >
                    {label}
                  </NavLink>
                </li>
              ))}
              <span
                className="pointer-events-none absolute bottom-0 left-0 h-0.5 rounded-full bg-gradient-to-r from-primary to-secondary transition-[width,transform] duration-300 ease-out will-change-transform will-change-width"
                style={{
                  width: `${underline.width}px`,
                  transform: `translateX(${underline.left}px)`,
                  opacity: underline.width ? 1 : 0,
                }}
              />
            </ul>
          </nav>
        )}

        <div className="relative flex justify-end" ref={profileRef}>
          <button
            type="button"
            className="btn btn-ghost btn-circle h-12 w-12 text-3xl ml-42"
            onClick={() => setIsProfileOpen((open) => !open)}
            aria-haspopup="true"
            aria-expanded={isProfileOpen}
          >
            <i className="bi bi-person-circle" aria-hidden="true" />
            <span className="sr-only">Abrir menu do perfil</span>
          </button>

          {isProfileOpen && (
            <div className="absolute right-0 mt-2 w-48 rounded-xl border border-base-300 bg-base-100 shadow-lg z-50">
              <button
                type="button"
                className="w-full text-left px-4 py-3 text-sm font-semibold text-error hover:bg-error/10 transition-colors duration-150 cursor-pointer rounded-b-xl"
                onClick={handleLogout}
              >
                Fazer logout
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

AdminNavbar.propTypes = {
  variant: PropTypes.oneOf(["admin", "super"]),
};
