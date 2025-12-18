import React, { useEffect, useState } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import InputField from "../../../components/ui/Input/InputField.jsx";
import Button from "../../../components/ui/Button/Button.jsx";
import { login } from "../../../api/auth/auth.js";
import ForgotPassword from "../../../components/ui/Modal/ForgotPassword.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";
import { API_URL } from "../../../api/config/config.js";

/**
 * Ecrã de login dos utilizadores (colaboradores e empresas).
 * Centraliza as credenciais, CTA para os fluxos de registo e o modal de recuperação de palavra-passe.
 */
export default function LoginCandidate() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [rememberMe, setRememberMe] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState(false);
    const [showForgotModal, setShowForgotModal] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();
    const { refreshAuth } = useAuthContext();

    // Sempre que alguém navega para /login com state { openForgotModal: true } abrimos o modal automaticamente.
    useEffect(() => {
        if (location.state?.openForgotModal) {
            setShowForgotModal(true);
        }
    }, [location.state]);

    /**
     * Envia as credenciais para o backend e apresenta feedback visual de sucesso/erro.
     * Os tokens são guardados automaticamente através do helper em api/auth.
     */
    const handleLogin = async () => {
        setLoading(true);
        setError("");
        setSuccess(false);
        try {
      const data = await login(email, password, rememberMe);
      console.log("Login OK:", data);
      setSuccess(true);
      refreshAuth();
      const destination = data?.userType === "COMPANY" ? "/empresa" : "/";
      navigate(destination, { replace: true });
    } catch (e) {
            setError(e.message || "Falha no login");
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="min-h-screen flex items-center justify-center bg-base-200">
            <div className="card w-full max-w-md shadow-md bg-base-100">
                <div className="card-body space-y-6">
                    <h2 className="text-center text-2xl font-bold text-primary mb-4">
                        Login
                    </h2>

                    {error && <div className="alert alert-error">{error}</div>}
                    {success && <div className="alert alert-success">Login efetuado com sucesso!</div>}

                    <InputField
                        label="Email"
                        placeholder="Insira o seu email"
                        icon={<i className="bi bi-envelope"></i>}
                        type="email"
                        value={email}
                        autoComplete="email"
                        onChange={(e) => setEmail(e.target.value)}
                    />

                    <div  className="flex flex-col gap-4">
                        <InputField
                            label="Password"
                            placeholder="Insira a sua password"
                            icon={<i className="bi bi-lock"></i>}
                        type="password"
                        value={password}
                        autoComplete="current-password"
                        onChange={(e) => setPassword(e.target.value)}
                    />
                        {/* Lembrar-me */}
                        <div className="flex items-center justify-center text-xs mt-0 space-x-1">
                            <label className="label cursor-pointer gap-2">
                                <input
                                    type="checkbox"
                                    className="checkbox checkbox-sm"
                                    checked={rememberMe}
                                    onChange={() => setRememberMe(!rememberMe)}
                                />
                                <span className="label-text text-xs">Lembrar-me</span>
                            </label>
                            <p className="text-xs text-accent text-right mt-0">
                                <button
                                    type="button"
                                    onClick={() => setShowForgotModal(true)}
                                    className="link link-accent"
                                >
                                    Esqueceu-se?
                                </button>
                            </p>
                        </div>
                    </div>

                    <div  className="flex flex-col gap-2">
                        <Button
                            label={loading ? "Entrando..." : "Entrar"}
                            variant="primary"
                            onClick={handleLogin}
                        />

                        {/* Registe-se */}
                        <div className="flex flex-wrap justify-center items-center text-xs mt-2 text-center">
                            <span className="text-gray-500">Registe-se como</span>
                            <Link
                                to="/employee-register/step1"
                                className="text-accent font-medium hover:underline"
                            >
                            &nbsp;Funcionário&nbsp;
                            </Link>
                            <span className="text-gray-500">ou</span>
                            <Link
                                to="/company-register/step1"
                                className="text-accent font-medium hover:underline"
                            >
                            &nbsp;Empresa&nbsp;
                            </Link>
                        </div>
                    </div>

                    <div className="divider">Ou</div>

                    {/* Email */}
                    <div className="flex flex-col gap-4">
                        <button
                            type="button"
                            className="btn bg-white text-black border-[#e5e5e5] gap-2"
                            onClick={() => {
                                // usa a URL do backend; em dev, defina VITE_API_URL=https://localhost:8443 para garantir HTTPS
                                const base = API_URL && API_URL.trim() ? API_URL : "https://localhost:8443";
                                window.location.href = `${base}/oauth2/authorization/google`;
                            }}
                        >
                            <i className="bi bi-google text-lg"></i>
                            Login com Google
                        </button>
                    </div>

                    <p className="text-xs text-gray-500 text-center -mt-3">
                        <i className="bi bi-exclamation-octagon text-gray-400 mr-1"></i>
                        Opções disponíveis apenas para Funcionários.
                    </p>
                </div>
            </div>
            <Outlet />
            <ForgotPassword
                open={showForgotModal}
                initialEmail={email}
                onClose={() => setShowForgotModal(false)}
            />
        </main>
    );
}
