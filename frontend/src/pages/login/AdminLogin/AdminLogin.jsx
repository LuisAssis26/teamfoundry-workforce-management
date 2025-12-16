import React, { useState } from "react";
import InputField from "../../../components/ui/Input/InputField.jsx";
import Button from "../../../components/ui/Button/Button.jsx";
import { useNavigate } from "react-router-dom";
import { setTokens } from "../../../auth/tokenStorage.js";

/**
 * Tela de login para administradores com validacao usando HTTPS e senha com hash.
 */
export default function AdminLogin() {
    const navigate = useNavigate();
    const [credentials, setCredentials] = useState({ username: "", password: "" });
    const [feedback, setFeedback] = useState({ type: "", message: "" });
    const [isSubmitting, setIsSubmitting] = useState(false);
    // Permite definir um endpoint HTTPS via VITE_API_BASE_URL.
    const envApiUrl = (import.meta.env.VITE_API_BASE_URL ?? "").trim().replace(/\/$/, "");
    const isViteLocalhost = window.location.hostname === "localhost" && window.location.port === "5173";
    const baseApiUrl = isViteLocalhost ? "" : envApiUrl;
    const loginEndpoint = baseApiUrl ? `${baseApiUrl}/api/admin/login` : "/api/admin/login";

    const handleChange = (field) => (event) => {
        setCredentials((prev) => ({
            ...prev,
            [field]: event.target.value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setFeedback({ type: "", message: "" });
        setIsSubmitting(true);

        try {
            const response = await fetch(loginEndpoint, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(credentials),
            });

            const rawBody = await response.text();
            let payload = null;
            if (rawBody) {
                try {
                    payload = JSON.parse(rawBody);
                } catch {
                    payload = null;
                }
            }

            if (!response.ok) {
                const apiMessage = payload?.error;
                const localizedMessage =
                    apiMessage === "Invalid credentials"
                        ? "Credenciais inválidas. Tente novamente."
                        : apiMessage || "Falha no login. Tente novamente.";
                throw new Error(localizedMessage);
            }

            if (!payload) {
                throw new Error("Resposta inesperada do servidor. Tente novamente.");
            }

            const userType = payload.role === "SUPERADMIN" ? "SUPERADMIN" : "ADMIN";
            const roleLabel = userType === "SUPERADMIN" ? "Superadmin" : "Admin";
            setFeedback({ type: "success", message: `Bem-vindo, ${roleLabel}` });

            localStorage.setItem("tf-user-type", userType);
            if (payload.accessToken) {
                // Mantém a sessão enquanto o browser estiver aberto (sessionStorage) e também no localStorage
                // para que o admin não perca a sessão em refresh dentro da mesma janela.
                setTokens(
                    { accessToken: payload.accessToken, refreshToken: payload.refreshToken },
                    { persist: "both" }
                );
            }

            if (payload.role === "SUPERADMIN") {
                navigate("/admin/super/credenciais", { replace: true });
            } else {
                navigate("/admin/team-management", { replace: true });
            }
        } catch (error) {
            // Converte qualquer erro em mensagem amigavel.
            setFeedback({
                type: "error",
                message: error.message || "Credentials invalidas. Tente novamente.",
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <main className="min-h-screen bg-base-200 flex items-center justify-center">
            <div className="card w-full max-w-md shadow-md bg-base-100">
                <form className="card-body space-y-6" onSubmit={handleSubmit}>
                    <h2 className="text-center text-2xl font-bold text-primary">Login Administrador</h2>

                    <InputField
                        label="Username"
                        placeholder="Insira o username"
                        icon={<i className="bi bi-person"></i>}
                        value={credentials.username}
                        onChange={handleChange("username")}
                    />

                    <InputField
                        label="Password"
                        placeholder="Insira a password"
                        type="password"
                        icon={<i className="bi bi-lock"></i>}
                        value={credentials.password}
                        onChange={handleChange("password")}
                    />

                    <Button
                        label={isSubmitting ? "Aguarde..." : "Entrar"}
                        type="submit"
                        variant="primary"
                        disabled={isSubmitting}
                    />

                    {feedback.message && (
                        <p
                            className={`text-center text-sm ${
                                feedback.type === "error" ? "text-error" : "text-success"
                            }`}
                        >
                            {feedback.message}
                        </p>
                    )}
                </form>
            </div>
        </main>
    );
}
