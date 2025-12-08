import { Outlet, Navigate, useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import Navbar from "../../../components/sections/Navbar.jsx";
import Sidebar from "../../../components/sections/Sidebar.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

const PROFILE_INFO_ROUTES = [
  "/candidato/dados-pessoais",
  "/candidato/certificacoes",
  "/candidato/ultimos-trabalhos",
  "/candidato/preferencias",
];

const SIDEBAR_LINKS = [
  {
    to: "/candidato/dados-pessoais",
    label: "Perfil",
    icon: "bi-person",
    matches: PROFILE_INFO_ROUTES,
  },
  { to: "/candidato/ofertas", label: "Ofertas", icon: "bi-bell" },
  { to: "/candidato/documentos", label: "Documentos", icon: "bi-file-earmark-text" },
  { to: "/candidato/proximos-passos", label: "Próximos passos", icon: "bi-flag" },
  { to: "/candidato/definicoes", label: "Definições", icon: "bi-gear" },
];

export default function EmployeeLayout() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [animatingOut, setAnimatingOut] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuthContext();

  const closeMobileMenu = () => {
    if (!mobileMenuOpen) return;
    setAnimatingOut(true);
    setTimeout(() => {
      setMobileMenuOpen(false);
      setAnimatingOut(false);
    }, 150);
  };

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <div className="min-h-screen bg-base-100 text-base-content">
      <Navbar
        onLogout={handleLogout}
        mobileMenuButton={
          <button
            type="button"
            className="btn btn-ghost btn-circle text-primary-content"
            onClick={() => setMobileMenuOpen(true)}
          >
            <i className="bi bi-list text-2xl" aria-hidden="true" />
            <span className="sr-only">Abrir menu</span>
          </button>
        }
      />

      <div className="max-w-6xl mx-auto px-2 md:px-2 py-6 flex gap-6">
        <Sidebar
          items={SIDEBAR_LINKS}
          activePath={location.pathname}
          onNavigate={() => {}}
          className="pr-2"
        />
        <main className="flex-1">
          <Outlet />
        </main>
      </div>

      {mobileMenuOpen && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div
            className="absolute inset-0 bg-black/40"
            onClick={closeMobileMenu}
            aria-hidden="true"
          />
          <div className="absolute inset-y-0 left-0 w-72 max-w-[80%]">
            <div className="h-full translate-x-0 transform transition-transform duration-200 ease-out">
              <Sidebar
                items={SIDEBAR_LINKS}
                activePath={location.pathname}
                onNavigate={closeMobileMenu}
                isMobile
                animateOut={animatingOut}
                logo={undefined}
                title="TeamFoundry"
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// Fallback element if used as index route
export function CandidateIndexRedirect() {
  return <Navigate to="dados-pessoais" replace />;
}
