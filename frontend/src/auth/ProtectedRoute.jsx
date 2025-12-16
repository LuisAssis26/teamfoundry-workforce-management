import PropTypes from "prop-types";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuthContext } from "./AuthContext.jsx";

// Redireciona cada tipo para a respetiva area inicial.
const HOME_BY_TYPE = {
  EMPLOYEE: "/candidato",
  COMPANY: "/empresa",
  ADMIN: "/admin/team-management",
  SUPERADMIN: "/admin/super/credenciais",
};

/**
 * Protege rotas verificando autenticacao e se o tipo de utilizador esta autorizado.
 * Quando o tipo nao for permitido, redireciona o utilizador para a sua area.
 */
export default function ProtectedRoute({ children, allowedTypes, redirectTo = "/login" }) {
  const { isAuthenticated, userType } = useAuthContext();
  const location = useLocation();

  const normalizedType = userType?.toUpperCase() || null;
  const normalizedAllowed =
    Array.isArray(allowedTypes) && allowedTypes.length > 0
      ? allowedTypes.map((type) => type.toUpperCase())
      : null;

  if (!isAuthenticated || !normalizedType) {
    return <Navigate to={redirectTo} replace state={{ from: location }} />;
  }

  if (normalizedAllowed && !normalizedAllowed.includes(normalizedType)) {
    const fallback = HOME_BY_TYPE[normalizedType] ?? redirectTo;
    return <Navigate to={fallback} replace state={{ from: location }} />;
  }

  return children || <Outlet />;
}

ProtectedRoute.propTypes = {
  children: PropTypes.node,
  allowedTypes: PropTypes.arrayOf(PropTypes.string),
  redirectTo: PropTypes.string,
};
