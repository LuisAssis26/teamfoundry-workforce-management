import PropTypes from "prop-types";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuthContext } from "./AuthContext.jsx";

/**
 * Protege rotas verificando se o utilizador está autenticado e, opcionalmente, se possui um tipo permitido.
 * Pode ser usado como wrapper de elementos ou como <Route element={<ProtectedRoute ... />}> com Outlet.
 */
export default function ProtectedRoute({ children, allowedTypes, redirectTo = "/login" }) {
  const { isAuthenticated, userType } = useAuthContext();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to={redirectTo} replace state={{ from: location }} />;
  }

  if (Array.isArray(allowedTypes) && allowedTypes.length > 0 && !allowedTypes.includes(userType)) {
    const fallback =
      userType === "EMPLOYEE" ? "/candidato" : userType === "COMPANY" ? "/empresa" : "/";
    return <Navigate to={fallback} replace />;
  }

  return children || <Outlet />;
}

ProtectedRoute.propTypes = {
  children: PropTypes.node,
  allowedTypes: PropTypes.arrayOf(PropTypes.string),
  redirectTo: PropTypes.string,
};
