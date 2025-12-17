import { useEffect, useRef, useState } from "react";
import PropTypes from "prop-types";
import { Link, useLocation } from "react-router-dom";
import Button from "../ui/Button/Button";
import logo from "../../assets/images/logo/teamFoundry_LogoWhite.png";
import { useAuthContext } from "../../auth/AuthContext.jsx";
import { useEmployeeProfile } from "../../pages/profile/Employee/EmployeeProfileContext.jsx";

const NAV_LINKS = [];

const PUBLIC_ACTIONS = [{ to: "/login", label: "Entrar", variant: "secondary" }];

export default function Navbar({
  variant = "private",
  homePath = "/",
  links = NAV_LINKS,
  actions = PUBLIC_ACTIONS,
  onLogout,
  mobileMenuButton = null,
}) {
  const isPublic = variant === "public";
  const { userType } = useAuthContext();
  const { profile } = useEmployeeProfile();
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const location = useLocation();
  const profileRef = useRef(null);
  const profileImageUrl = profile?.profilePictureUrl;

  useEffect(() => {
    setIsProfileOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    function handleOutsideClick(event) {
      if (profileRef.current && !profileRef.current.contains(event.target)) {
        setIsProfileOpen(false);
      }
    }
    if (isProfileOpen) document.addEventListener("mousedown", handleOutsideClick);
    return () => document.removeEventListener("mousedown", handleOutsideClick);
  }, [isProfileOpen]);

  const wrapperClasses = isPublic
    ? "bg-primary text-primary-content sticky top-0 z-30"
    : "bg-primary text-primary-content";

  const actionButtons = isPublic
    ? (actions?.length ? actions : PUBLIC_ACTIONS)
    : [];

  const navItems = Array.isArray(links) ? links : NAV_LINKS;

  return (
    <header className={wrapperClasses}>
      <div className="max-w-6xl mx-auto px-4 lg:px-6 py-3 flex items-center justify-between gap-6">
        <div className="flex items-center gap-3 shrink-0 group">
          {mobileMenuButton && <div className="md:hidden">{mobileMenuButton}</div>}
          <Link to={homePath} className="hidden md:flex items-center gap-3">
            <img
              src={logo}
              alt="TeamFoundry"
              className={`h-10 w-10 object-contain`}
            />
            <span className="font-semibold tracking-[0.2em] uppercase text-primary-content">
              TeamFoundry
            </span>
          </Link>
        </div>

        {(!isPublic && navItems.length > 0) && (
          <nav className="hidden md:flex flex-1 justify-center">
            <ul className="flex items-center gap-8 font-medium text-sm uppercase tracking-wide">
              {navItems.map(({ to, label, internal }) => (
                <li key={label}>
                  {internal ? (
                    <Link
                      to={to}
                      className="hover:opacity-90 transition"
                      aria-label={label}
                    >
                      {label}
                    </Link>
                  ) : (
                    <a href={to} className="hover:opacity-90 transition" aria-label={label}>
                      {label}
                    </a>
                  )}
                </li>
              ))}
            </ul>
          </nav>
        )}

        {isPublic ? (
          <div className="flex items-center gap-3">
            {actionButtons.map(({ to, label, variant }) => (
              <Button
                key={label}
                as={Link}
                to={to}
                label={label}
                variant={variant}
                className="w-auto btn-md px-6 text-white max-w-[10rem]"
              />
            ))}
          </div>
        ) : (
          <div className="relative " ref={profileRef}>
            <button
              type="button"
              className="btn btn-ghost btn-circle h-12 w-12 text-3xl text-primary-content hover:bg-transparent active:bg-transparent focus:bg-transparent"
              onClick={() => setIsProfileOpen((o) => !o)}
              aria-haspopup="true"
              aria-expanded={isProfileOpen}
            >
              {profileImageUrl ? (
                <img
                  src={profileImageUrl}
                  alt="Foto do perfil"
                  className="w-full h-full rounded-full object-cover border-2 border-primary-content/30"
                />
              ) : (
                <i className="bi bi-person-circle" aria-hidden="true" />
              )}
              <span className="sr-only">Abrir menu do perfil</span>
            </button>

            {isProfileOpen && (
              <div className="absolute right-0 mt-2 w-56 rounded-xl border border-base-300 bg-base-100 text-base-content shadow-lg z-50">
                <nav>
                  {(userType === "COMPANY" ? COMPANY_MENU : EMPLOYEE_MENU).map(
                    ({ to, label, icon }, idx, arr) => (
                      <Link
                        key={to}
                        to={to}
                        className={`flex items-center gap-3 px-4 py-3 text-sm hover:bg-base-200 transition ${
                          idx === 0 ? "rounded-t-xl" : ""
                        } ${idx === arr.length - 1 ? "border-b border-base-200" : ""}`}
                        onClick={() => setIsProfileOpen(false)}
                      >
                        <i className={`bi ${icon}`} aria-hidden="true" />
                        <span>{label}</span>
                      </Link>
                    )
                  )}
                  <button
                    type="button"
                    className={`flex w-full items-center gap-3 px-4 py-3 text-sm font-semibold text-error hover:bg-error/10 transition-colors duration-150 cursor-pointer ${
                      userType !== "COMPANY" ? "rounded-b-xl" : ""
                    }`}
                    onClick={() => {
                      setIsProfileOpen(false);
                      onLogout?.();
                    }}
                  >
                    <i className="bi bi-box-arrow-right" aria-hidden="true" />
                    <span>Terminar sessão</span>
                  </button>
                  {userType === "COMPANY" && (
                    <>
                      <div className="border-t border-base-200" />
                      <Link
                        to="/faq"
                        className="flex items-center gap-3 px-4 py-3 text-sm hover:bg-base-200 transition"
                        onClick={() => setIsProfileOpen(false)}
                      >
                        <i className="bi bi-question-circle" aria-hidden="true" />
                        <span>FAQ&apos;s</span>
                      </Link>
                      <Link
                        to="/sobre-nos"
                        className="flex items-center gap-3 px-4 py-3 text-sm hover:bg-base-200 transition rounded-b-xl"
                        onClick={() => setIsProfileOpen(false)}
                      >
                        <i className="bi bi-info-circle" aria-hidden="true" />
                        <span>Sobre nós</span>
                      </Link>
                    </>
                  )}
                </nav>
              </div>
            )}
          </div>
        )}
      </div>
    </header>
  );
}

Navbar.propTypes = {
  variant: PropTypes.oneOf(["private", "public"]),
  homePath: PropTypes.string,
  links: PropTypes.arrayOf(
    PropTypes.shape({
      to: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      internal: PropTypes.bool,
    })
  ),
  actions: PropTypes.arrayOf(
    PropTypes.shape({
      to: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      variant: PropTypes.oneOf([
        "primary",
        "secondary",
        "accent",
        "neutral",
        "outline",
        "ghost",
        "warning",
        "success",
      ]),
    })
  ),
  onLogout: PropTypes.func,
  mobileMenuButton: PropTypes.node,
};

const EMPLOYEE_MENU = [
  { to: "/candidato/dados-pessoais", label: "Perfil", icon: "bi-person" },
  { to: "/candidato/ofertas", label: "Ofertas", icon: "bi-bell" },
  { to: "/candidato/documentos", label: "Documentos", icon: "bi-file-earmark-text" },
  { to: "/candidato/definicoes", label: "Definições", icon: "bi-gear" },
];

const COMPANY_MENU = [
  { to: "/empresa/informacoes", label: "Informações", icon: "bi-buildings" },
  { to: "/empresa/requisicoes", label: "Requisições", icon: "bi-list-check" },
  { to: "/empresa/definicoes", label: "Definições", icon: "bi-gear" },
];
