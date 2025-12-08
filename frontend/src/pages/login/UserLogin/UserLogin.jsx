import React, { useEffect, useState } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import InputField from "../../../components/ui/Input/InputField.jsx";
import Button from "../../../components/ui/Button/Button.jsx";
import { login } from "../../../api/auth/auth.js";
import ForgotPassword from "../../../components/ui/Modal/ForgotPassword.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

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
                        <button className="btn bg-white text-black border-[#e5e5e5]">
                            <svg aria-label="Email icon" width="16" height="16" xmlns="http://www.w3.org/2000/svg"
                                 viewBox="0 0 24 24">
                                <g strokeLinejoin="round" strokeLinecap="round" strokeWidth="2" fill="none" stroke="black">
                                    <rect width="20" height="16" x="2" y="4" rx="2"></rect>
                                    <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"></path>
                                </g>
                            </svg>
                            Login with Email
                        </button>

                        {/* LinkedIn*/}
                        <button className="btn bg-[#0967C2] text-white border-[#0059b3]">
                            <svg aria-label="LinkedIn logo" width="16" height="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32"><path fill="white" d="M26.111,3H5.889c-1.595,0-2.889,1.293-2.889,2.889V26.111c0,1.595,1.293,2.889,2.889,2.889H26.111c1.595,0,2.889-1.293,2.889-2.889V5.889c0-1.595-1.293-2.889-2.889-2.889ZM10.861,25.389h-3.877V12.87h3.877v12.519Zm-1.957-14.158c-1.267,0-2.293-1.034-2.293-2.31s1.026-2.31,2.293-2.31,2.292,1.034,2.292,2.31-1.026,2.31-2.292,2.31Zm16.485,14.158h-3.858v-6.571c0-1.802-.685-2.809-2.111-2.809-1.551,0-2.362,1.048-2.362,2.809v6.571h-3.718V12.87h3.718v1.686s1.118-2.069,3.775-2.069,4.556,1.621,4.556,4.975v7.926Z" fillRule="evenodd"></path></svg>
                            Login with LinkedIn
                        </button>
                    </div>

                    <p className="text-xs text-gray-500 text-center !mt-0">
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
