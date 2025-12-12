import React, { useEffect, useMemo, useState } from "react";
import { useOutletContext } from "react-router-dom";
import InputField from "../../../components/ui/Input/InputField.jsx";
import Button from "../../../components/ui/Button/Button.jsx";
import { registerStep1 } from "../../../api/auth/auth.js";

const passwordRequirements = [
    {
        id: "length",
        label: "Pelo menos 8 caracteres",
        test: (value) => value.length >= 8,
    },
    {
        id: "uppercase",
        label: "Uma letra maiúscula",
        test: (value) => /[A-Z]/.test(value),
    },
    {
        id: "lowercase",
        label: "Uma letra minúscula",
        test: (value) => /[a-z]/.test(value),
    },
    {
        id: "number",
        label: "Um número",
        test: (value) => /[0-9]/.test(value),
    },
    {
        id: "symbol",
        label: "Um símbolo",
        test: (value) => /[^A-Za-z0-9]/.test(value),
    },
];

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Primeiro passo do registo de candidato: recolhe email + password com validação visual.
 * As credenciais são enviadas ao backend e, em caso de sucesso, os dados ficam no contexto para os passos seguintes.
 */
export default function EmployeeRegisterStep1() {
    const { registerData, updateStepData, completeStep, goToStep } = useOutletContext();

    const [email, setEmail] = useState(registerData.credentials?.email || "");
    const [password, setPassword] = useState(registerData.credentials?.password || "");
    const [confirmPassword, setConfirmPassword] = useState(registerData.credentials?.password || "");
    const [passwordFocused, setPasswordFocused] = useState(false);
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [serverError, setServerError] = useState("");

    // Se o utilizador voltar ao passo 1, repovoamos os campos com os dados guardados no contexto.
    useEffect(() => {
        setEmail(registerData.credentials?.email || "");
        setPassword(registerData.credentials?.password || "");
        setConfirmPassword(registerData.credentials?.password || "");
    }, [registerData.credentials?.email, registerData.credentials?.password]);

    const passwordChecks = useMemo(() => {
        return passwordRequirements.map((requirement) => ({
            ...requirement,
            valid: requirement.test(password),
        }));
    }, [password]);

    const isPasswordValid = useMemo(() => passwordChecks.every((item) => item.valid), [passwordChecks]);

    // Valida email/password/confirmPassword antes de chamar o backend.
    const validateFields = () => {
        const newErrors = {};

        if (!email.trim()) {
            newErrors.email = "O email é obrigatório.";
        } else if (!emailRegex.test(email.trim())) {
            newErrors.email = "Insira um email válido.";
        }

        if (!password) {
            newErrors.password = "A password é obrigatória.";
        } else if (!isPasswordValid) {
            newErrors.password = "A password não cumpre os requisitos mínimos.";
        }

        if (!confirmPassword) {
            newErrors.confirmPassword = "Confirme a password.";
        } else if (password !== confirmPassword) {
            newErrors.confirmPassword = "As passwords não coincidem.";
        }

        return newErrors;
    };

    // Envia o primeiro passo do registo e, em caso de sucesso, vai para o passo 2.
    const handleSubmit = async (event) => {
        event.preventDefault();
        setServerError("");

        const validationErrors = validateFields();

        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        setErrors({});
        setLoading(true);

        try {
            await registerStep1({ email, password });

            updateStepData("credentials", { email, password });
            completeStep(1, 2);
        } catch (error) {
            console.error("Erro no registo de credenciais:", error);
            setServerError(error.message || "Ocorreu um erro inesperado.");
        } finally {
            setLoading(false);
        }
    };

    const showPasswordTooltip = passwordFocused && password.length > 0 && !isPasswordValid;

    return (
        <section className="flex h-full flex-col">
            <div>
                <p className="text-sm font-semibold text-primary uppercase tracking-wide">
                    Passo 1 de 4
                </p>
                <h1 className="mt-2 text-3xl font-bold text-accent">
                    Crie as suas credenciais
                </h1>
                <p className="mt-4 text-base text-base-content/70">
                    Defina o email e password que irá utilizar para aceder à plataforma.
                </p>
            </div>

            {serverError && (
                <div className="alert alert-error mt-6">
                    <span>{serverError}</span>
                </div>
            )}

            <form className="mt-8 flex-1 space-y-6" onSubmit={handleSubmit} noValidate>
                <InputField
                    label="Email"
                    placeholder="exemplo@dominio.com"
                    type="email"
                    icon={<i className="bi bi-envelope"></i>}
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    autoComplete="email"
                    error={errors.email}
                />

                <div className="relative">
                    <InputField
                        label="Password"
                    placeholder="Crie uma password"
                    type="password"
                    icon={<i className="bi bi-lock"></i>}
                    value={password}
                    autoComplete="new-password"
                    onChange={(event) => setPassword(event.target.value)}
                    onFocus={() => setPasswordFocused(true)}
                    onBlur={() => setPasswordFocused(false)}
                    error={errors.password}
                />

                    {showPasswordTooltip && (
                        <div className="absolute left-0 right-0 mt-2 rounded-2xl border border-base-200 bg-base-100 p-4 shadow-lg z-20">
                            <p className="text-sm font-medium text-base-content">
                                A password deve incluir:
                            </p>
                            <ul className="mt-2 space-y-1">
                                {passwordChecks.map((item) => (
                                    <li
                                        key={item.id}
                                        className={`flex items-center gap-2 text-sm ${
                                            item.valid ? "text-success" : "text-error"
                                        }`}
                                    >
                                        <i
                                            className={`bi ${
                                                item.valid ? "bi-check-circle-fill" : "bi-x-circle-fill"
                                            }`}
                                        ></i>
                                        <span>{item.label}</span>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>

                <InputField
                    label="Confirmar password"
                    placeholder="Repita a password"
                    type="password"
                    icon={<i className="bi bi-shield-check"></i>}
                    value={confirmPassword}
                    autoComplete="new-password"
                    onChange={(event) => setConfirmPassword(event.target.value)}
                    error={errors.confirmPassword}
                />

                <div className="mt-10 grid grid-cols-2 gap-4">
                    <Button
                        label="Anterior"
                        variant="outline"
                        disabled
                        className="btn-outline border-base-300 text-base-content/60"
                    />
                    <Button
                        label={loading ? "A guardar..." : "Avançar"}
                        type="submit"
                        variant="primary"
                        disabled={loading}
                    />
                </div>
            </form>
        </section>
    );
}
