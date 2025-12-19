import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { setTokens } from "../../../auth/tokenStorage.js";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

export default function GoogleCallback() {
  const location = useLocation();
  const navigate = useNavigate();
  const { refreshAuth } = useAuthContext();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const accessToken = params.get("accessToken");
    const userType = params.get("userType");
    if (accessToken) {
      setTokens({ accessToken }, { persist: "both" });
    }
    if (userType) {
      localStorage.setItem("tf-user-type", userType);
    }
    refreshAuth();
    const dest = userType === "COMPANY" ? "/empresa" : "/";
    navigate(dest, { replace: true });
  }, [location.search, navigate, refreshAuth]);

  return (
    <main className="min-h-screen flex items-center justify-center bg-base-200">
      <div className="p-6 text-center text-base-content/80">Finalizando login com Google...</div>
    </main>
  );
}
