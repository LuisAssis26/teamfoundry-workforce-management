import React, { useMemo, useState } from "react";
import InputField from "../../../components/ui/Input/InputField.jsx";
import Button from "../../../components/ui/Button/Button.jsx";
import { useOutletContext } from "react-router-dom";

const passwordRequirements = [
  { id: "length", label: "Pelo menos 8 caracteres", test: (value) => value.length >= 8 },
  { id: "uppercase", label: "Uma letra maiúscula", test: (value) => /[A-Z]/.test(value) },
  { id: "lowercase", label: "Uma letra minúscula", test: (value) => /[a-z]/.test(value) },
  { id: "number", label: "Um número", test: (value) => /[0-9]/.test(value) },
  { id: "symbol", label: "Um símbolo", test: (value) => /[^A-Za-z0-9]/.test(value) },
];

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Passo 1 do registo de empresa: credenciais de acesso.
 */
export default function CompanyRegisterStep1() {
  const { companyData, updateStepData, completeStep } = useOutletContext();

  const [credentialEmail, setCredentialEmail] = useState(companyData.credentials?.credentialEmail || "");
  const [password, setPassword] = useState(companyData.credentials?.password || "");
  const [confirmPassword, setConfirmPassword] = useState(companyData.credentials?.password || "");
  const [errors, setErrors] = useState({});
  const [passwordFocused, setPasswordFocused] = useState(false);
  const [loading, setLoading] = useState(false);

  // Reaproveita o mesmo check visual do registo de colaboradores.
  const passwordChecks = useMemo(
    () => passwordRequirements.map((req) => ({ ...req, valid: req.test(password) })),
    [password],
  );

  const isPasswordValid = passwordChecks.every((item) => item.valid);

  // Valida credenciais e dados do responsável antes de persistir.
  const validate = () => {
    const newErrors = {};
    if (!credentialEmail.trim() || !emailRegex.test(credentialEmail.trim())) {
      newErrors.credentialEmail = "Insira um email válido";
    }
    if (!isPasswordValid) {
      newErrors.password = "A password não cumpre os requisitos mínimos.";
    }
    if (password !== confirmPassword) {
      newErrors.confirmPassword = "As passwords não coincidem.";
    }
    return newErrors;
  };

  // Apenas guarda no contexto local; o envio para backend ocorre no passo final.
  const handleSubmit = (event) => {
    event.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    setLoading(true);
    updateStepData("credentials", {
      credentialEmail: credentialEmail.trim(),
      password,
    });
    completeStep(1, 2);
    setLoading(false);
  };

  return (
    <section className="flex h-full flex-col">
      <div>
        <p className="text-sm font-semibold text-primary uppercase tracking-wide">Passo 1 de 4</p>
        <h1 className="mt-2 text-3xl font-bold text-accent">Credenciais de acesso</h1>
        <p className="mt-4 text-base text-base-content/70">
          Crie o email e password que irão aceder à área da empresa.
        </p>
      </div>

      <form className="mt-8 flex-1 space-y-6" onSubmit={handleSubmit}>
        <InputField
          label="Email de acesso"
          type="email"
          placeholder="credencial@empresa.com"
          icon={<i className="bi bi-envelope" />}
          value={credentialEmail}
          onChange={(event) => setCredentialEmail(event.target.value)}
          autoComplete="email"
          error={errors.credentialEmail}
        />

        <div className="relative">
          <InputField
          label="Password"
          type="password"
          placeholder="Crie uma password"
          icon={<i className="bi bi-lock" />}
          value={password}
          autoComplete="new-password"
          onChange={(event) => {
            setPassword(event.target.value);
          }}
          onFocus={() => setPasswordFocused(true)}
          onBlur={() => setPasswordFocused(false)}
            error={errors.password}
          />

          {passwordFocused && password.length > 0 && !isPasswordValid && (
            <div className="absolute left-0 right-0 mt-2 rounded-2xl border border-base-200 bg-base-100 p-4 shadow-lg z-20">
              <p className="text-sm font-semibold text-base-content">A password deve incluir:</p>
              <ul className="mt-2 space-y-1">
                {passwordChecks.map((item) => (
                  <li
                    key={item.id}
                    className={`flex items-center gap-2 text-sm ${item.valid ? "text-success" : "text-error"}`}
                  >
                    <i className={`bi ${item.valid ? "bi-check-circle-fill" : "bi-x-circle-fill"}`} />
                    <span>{item.label}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <InputField
          label="Confirmar password"
          type="password"
          placeholder="Repita a password"
          icon={<i className="bi bi-shield-lock" />}
          value={confirmPassword}
          autoComplete="new-password"
          onChange={(event) => setConfirmPassword(event.target.value)}
          error={errors.confirmPassword}
        />

        <div className="mt-10 grid grid-cols-2 gap-4">
          <Button label="Anterior" variant="outline" disabled className="btn-outline border-base-300 text-base-content/60" />
          <Button label="Avançar" variant="primary" type="submit" disabled={loading} />
        </div>
      </form>
    </section>
  );
}
