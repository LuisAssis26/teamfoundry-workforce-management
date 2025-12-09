import { useEffect, useMemo, useRef, useState } from "react";
import PropTypes from "prop-types";
import InputField from "../Input/InputField.jsx";
import Button from "../Button/Button.jsx";
import Modal from "./Modal.jsx";
import { forgotPassword, resetPassword, verifyResetCode } from "../../../api/auth/auth.js";

const CODE_LENGTH = 6;
const RESEND_COOLDOWN_SECONDS = 45;

const STEPS = {
  EMAIL: "email",
  CODE: "code",
  RESET: "reset",
};

const maskEmail = (email = "") => {
  if (!email.includes("@")) return email;
  const [local, domain] = email.split("@");
  if (!local) return email;
  const visible = local.slice(0, Math.min(2, local.length));
  return `${visible}${local.length > 2 ? "***" : ""}@${domain}`;
};

const passwordRequirements = [
  { id: "length", label: "Pelo menos 8 caracteres", test: (value) => value.length >= 8 },
  { id: "uppercase", label: "Uma letra maiúscula", test: (value) => /[A-Z]/.test(value) },
  { id: "lowercase", label: "Uma letra minúscula", test: (value) => /[a-z]/.test(value) },
  { id: "number", label: "Um número", test: (value) => /[0-9]/.test(value) },
  { id: "symbol", label: "Um símbolo", test: (value) => /[^A-Za-z0-9]/.test(value) },
];

export default function ForgotPassword({ open, onClose, initialEmail }) {
  const [step, setStep] = useState(STEPS.EMAIL);
  const [email, setEmail] = useState(initialEmail || "");
  const [codeDigits, setCodeDigits] = useState(Array(CODE_LENGTH).fill(""));
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordFocused, setPasswordFocused] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [infoMessage, setInfoMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [codeError, setCodeError] = useState("");

  const [resendCooldown, setResendCooldown] = useState(0);
  const [resent, setResent] = useState(false);
  const inputRefs = useRef([]);

  const isCodeComplete = useMemo(() => codeDigits.every((digit) => digit !== ""), [codeDigits]);
  const codeValue = useMemo(() => codeDigits.join(""), [codeDigits]);
  const passwordChecks = useMemo(
    () =>
      passwordRequirements.map((requirement) => ({
        ...requirement,
        valid: requirement.test(newPassword),
      })),
    [newPassword]
  );
  const isPasswordValid = useMemo(() => passwordChecks.every((item) => item.valid), [passwordChecks]);

  useEffect(() => {
    let interval;
    if (resendCooldown > 0) {
      interval = setInterval(() => setResendCooldown((prev) => (prev > 0 ? prev - 1 : 0)), 1000);
    }
    return () => clearInterval(interval);
  }, [resendCooldown]);

  useEffect(() => {
    if (!open) {
      resetModalState();
    } else {
      setEmail(initialEmail || "");
    }
  }, [open, initialEmail]);

  useEffect(() => {
    if (step === STEPS.CODE && inputRefs.current[0]) {
      inputRefs.current[0].focus();
    }
  }, [step]);

  const resetModalState = () => {
    setStep(STEPS.EMAIL);
    setEmail(initialEmail || "");
    setCodeDigits(Array(CODE_LENGTH).fill(""));
    setNewPassword("");
    setConfirmPassword("");
    setPasswordFocused(false);
    setLoading(false);
    setError("");
    setInfoMessage("");
    setSuccessMessage("");
    setResendCooldown(0);
    setResent(false);
    setCodeError("");
  };

  const handleRequestCode = async (event) => {
    event?.preventDefault?.();
    if (!email.trim()) {
      setError("Informe o email associado à conta.");
      return;
    }
    setLoading(true);
    setError("");
    setInfoMessage("");
    setSuccessMessage("");
    setCodeError("");
    try {
      await forgotPassword(email.trim());
      setStep(STEPS.CODE);
      setCodeDigits(Array(CODE_LENGTH).fill(""));
      setInfoMessage("Enviámos um código para o seu email. Verifique a caixa de entrada ou spam.");
      setResendCooldown(RESEND_COOLDOWN_SECONDS);
      setResent(false);
    } catch (err) {
      setError(err.message || "Não foi possível enviar o código.");
    } finally {
      setLoading(false);
    }
  };

  const handleCodeSubmit = async (event) => {
    event?.preventDefault?.();
    if (!isCodeComplete) {
      const message = "Insira o código completo.";
      setError(message);
      setCodeError(message);
      return;
    }
    setLoading(true);
    setError("");
    setInfoMessage("");
    setCodeError("");
    try {
      await verifyResetCode(email.trim(), codeValue);
      setStep(STEPS.RESET);
    } catch (err) {
      const message = err.message || "Código inválido ou expirado.";
      setError(message);
      setCodeError(message);
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (event) => {
    event?.preventDefault?.();
    if (!isPasswordValid) {
      setError("A nova password não cumpre os requisitos mínimos.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("As passwords não coincidem.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      await resetPassword(email.trim(), codeValue, newPassword);
      setSuccessMessage("Password atualizada com sucesso. Já pode iniciar sessão.");
      setTimeout(() => {
        onClose();
      }, 1200);
    } catch (err) {
      setError(err.message || "Não foi possível atualizar a password.");
    } finally {
      setLoading(false);
    }
  };

  const handleResendCode = async () => {
    if (resendCooldown > 0 || loading) return;
    if (!email.trim()) {
      setError("Email inválido. Volte ao passo anterior para informar o email.");
      return;
    }
    setLoading(true);
    setError("");
    setInfoMessage("");
    setCodeError("");
    try {
      await forgotPassword(email.trim());
      setInfoMessage("Reenviámos um novo código. Verifique a caixa de entrada ou spam.");
      setResendCooldown(RESEND_COOLDOWN_SECONDS);
      setResent(true);
    } catch (err) {
      setError(err.message || "Não foi possível reenviar o código.");
    } finally {
      setLoading(false);
    }
  };

  const handleCodeDigitChange = (index, value) => {
    if (!/^[0-9]?$/.test(value)) return;
    const next = [...codeDigits];
    next[index] = value;
    setCodeDigits(next);
    if (value && inputRefs.current[index + 1]) {
      inputRefs.current[index + 1].focus();
    }
  };

  const handleCodeKeyDown = (event, index) => {
    if (event.key === "Backspace" && !codeDigits[index] && inputRefs.current[index - 1]) {
      inputRefs.current[index - 1].focus();
    }
  };

  const renderStep = () => {
    switch (step) {
      case STEPS.EMAIL:
        return (
          <form className="space-y-4" onSubmit={handleRequestCode}>
            <InputField
              label="Email"
              type="email"
              value={email}
              placeholder="Insira o email da sua conta"
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
              required
            />
            <Button label={loading ? "A enviar..." : "Enviar código"} type="submit" disabled={loading} />
          </form>
        );
      case STEPS.CODE:
        return (
          <form className="space-y-4" onSubmit={handleCodeSubmit}>
            <p className="text-sm text-base-content/80">
              Introduza o código enviado para <strong>{maskEmail(email)}</strong>.
            </p>
            <div className="grid grid-cols-6 justify-items-center">
              {codeDigits.map((digit, idx) => (
                <input
                  key={idx}
                  type="text"
                  inputMode="numeric"
                  pattern="[0-9]*"
                  maxLength={1}
                  className={`input input-bordered w-12 h-12 text-center text-lg ${codeError ? "input-error" : ""}`}
                  value={digit}
                  onChange={(e) => handleCodeDigitChange(idx, e.target.value)}
                  onKeyDown={(e) => handleCodeKeyDown(e, idx)}
                  ref={(el) => (inputRefs.current[idx] = el)}
                  disabled={loading}
                  aria-label={`Código dígito ${idx + 1}`}
                />
              ))}
            </div>
            {codeError && <p className="text-error text-sm text-center">{codeError}</p>}
            <div className="flex flex-col sm:flex-row gap-3 justify-center sm:justify-between items-center w-full">
              <Button label="Validar código" type="submit" disabled={loading} className="w-full sm:w-auto" />
              <button
                type="button"
                className="btn btn-ghost btn-sm whitespace-normal text-center w-full sm:w-auto"
                onClick={handleResendCode}
                disabled={loading || resendCooldown > 0}
              >
                {resendCooldown > 0
                  ? `Reenviar em ${resendCooldown}s`
                  : resent
                    ? "Reenviar novamente"
                    : "Reenviar código"}
              </button>
            </div>
          </form>
        );
      case STEPS.RESET:
        return (
          <form className="space-y-4" onSubmit={handleResetPassword}>
            <InputField
              label="Nova password"
              type="password"
              value={newPassword}
              onFocus={() => setPasswordFocused(true)}
              onChange={(e) => setNewPassword(e.target.value)}
              disabled={loading}
              required
            />
            <InputField
              label="Confirmar password"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              disabled={loading}
              required
            />
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
              {passwordChecks.map((item) => (
                <div key={item.id} className={`flex items-center gap-2 ${item.valid ? "text-success" : "text-base-content/70"}`}>
                  <i className={`bi ${item.valid ? "bi-check-circle" : "bi-dot"}`} aria-hidden="true" />
                  <span>{item.label}</span>
                </div>
              ))}
            </div>
            <Button label={loading ? "A guardar..." : "Atualizar password"} type="submit" disabled={loading} />
          </form>
        );
      default:
        return null;
    }
  };

  return (
    <Modal title="Recuperar password" open={open} onClose={onClose}>
      <div className="relative space-y-4">
        <div className="toast toast-top toast-center left-1/2 -translate-x-1/2">
          {error && <div className="alert alert-error text-sm">{error}</div>}
          {infoMessage && <div className="alert alert-info text-sm">{infoMessage}</div>}
          {successMessage && <div className="alert alert-success text-sm">{successMessage}</div>}
        </div>
        {renderStep()}
      </div>
    </Modal>
  );
}

ForgotPassword.propTypes = {
  open: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  initialEmail: PropTypes.string,
};
