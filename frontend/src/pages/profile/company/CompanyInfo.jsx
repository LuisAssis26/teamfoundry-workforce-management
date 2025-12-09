import { useEffect, useMemo, useState } from "react";
import InputField from "../../../components/ui/Input/InputField.jsx";
import Button from "../../../components/ui/Button/Button.jsx";
import { useCompanyProfile } from "./CompanyProfileContext.jsx";
import {
  sendCompanyManagerCode,
  confirmCompanyManagerEmail,
} from "../../../api/profile/companyProfile.js";
import EmailVerificationModal from "./components/EmailVerificationModal.jsx";

/**
 * Página "Informações" do perfil da empresa.
 * Mostra dados da conta (read-only) e permite atualizar o responsável.
 */
export default function CompanyInfo() {
  const { companyProfile, loadingProfile, profileError, refreshProfile, saveManager } =
    useCompanyProfile();
  const [form, setForm] = useState({ name: "", email: "", phone: "", position: "" });
  const [originalEmail, setOriginalEmail] = useState("");
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState("");
  const [error, setError] = useState("");
  const [verificationOpen, setVerificationOpen] = useState(false);
  const [codeDigits, setCodeDigits] = useState(Array(CODE_LENGTH).fill(""));
  const [codeError, setCodeError] = useState("");
  const [resendCooldown, setResendCooldown] = useState(0);
  const [sendingCode, setSendingCode] = useState(false);

  useEffect(() => {
    refreshProfile();
  }, [refreshProfile]);

  useEffect(() => {
    if (companyProfile?.manager) {
      setForm({
        name: companyProfile.manager.name ?? "",
        email: companyProfile.manager.email ?? companyProfile.email ?? "",
        phone: companyProfile.manager.phone ?? "",
        position: companyProfile.manager.position ?? "",
      });
      setOriginalEmail(companyProfile.manager.email ?? companyProfile.email ?? "");
    } else if (companyProfile) {
      setForm((prev) => ({ ...prev, email: companyProfile.email ?? "" }));
      setOriginalEmail(companyProfile.email ?? "");
    }
  }, [companyProfile]);

  const companyFields = useMemo(
    () => [
      { label: "Nome da empresa", value: companyProfile?.name, type: "text" },
      { label: "NIF", value: companyProfile?.nif, type: "text" },
      { label: "Email", value: companyProfile?.email, type: "email" },
      { label: "País", value: companyProfile?.country, type: "text" },
      { label: "Morada", value: companyProfile?.address, type: "text" },
      { label: "Telefone", value: companyProfile?.phone, type: "text" },
      { label: "Website", value: companyProfile?.website, type: "text" },
    ],
    [companyProfile]
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    setFeedback("");
    try {
      if (form.email.trim().toLowerCase() !== originalEmail.trim().toLowerCase()) {
        // Se email mudou, abre modal e envia código
        await triggerSendCode();
        setVerificationOpen(true);
      } else {
        await saveManager({ name: form.name, phone: form.phone, position: form.position });
        setFeedback("Responsável atualizado com sucesso.");
      }
    } catch (err) {
      setError(err.message || "Não foi possível atualizar o responsável.");
    } finally {
      setSaving(false);
    }
  };

  const triggerSendCode = async () => {
    setSendingCode(true);
    setCodeError("");
    try {
      await sendCompanyManagerCode(form.email.trim());
      setResendCooldown(RESEND_COOLDOWN_SECONDS);
      setCodeDigits(Array(CODE_LENGTH).fill(""));
    } catch (err) {
      setCodeError(err.message || "Não foi possível enviar o código.");
      throw err;
    } finally {
      setSendingCode(false);
    }
  };

  useEffect(() => {
    if (resendCooldown <= 0) return;
    const timer = setInterval(
      () => setResendCooldown((prev) => (prev > 0 ? prev - 1 : 0)),
      1000
    );
    return () => clearInterval(timer);
  }, [resendCooldown]);

  const handleCodeChange = (index, value) => {
    const sanitized = value.replace(/\D/g, "");
    setCodeDigits((prev) => {
      const next = [...prev];
      next[index] = sanitized ? sanitized.slice(-1) : "";
      return next;
    });
  };

  const handleCodeVerify = async (e) => {
    e.preventDefault();
    const code = codeDigits.join("");
    if (code.length !== CODE_LENGTH) {
      setCodeError("Insira o código completo.");
      return;
    }
    setSendingCode(true);
    setCodeError("");
    setError("");
    setFeedback("");
    try {
      await confirmCompanyManagerEmail({
        newEmail: form.email.trim(),
        code,
        name: form.name,
        phone: form.phone,
        position: form.position,
      });
      setFeedback("Email do responsável atualizado com sucesso.");
      setVerificationOpen(false);
      setOriginalEmail(form.email.trim());
      refreshProfile();
    } catch (err) {
      setCodeError(err.message || "Código inválido ou expirado.");
    } finally {
      setSendingCode(false);
    }
  };

  return (
    <div className="space-y-6">
      <header className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-3xl font-semibold text-center sm:text-center md:text-left w-full">Informações</h1>
        </div>
      </header>

      {profileError && (
        <div className="alert alert-error text-sm" role="alert">
          {profileError}
        </div>
      )}

      <div className="grid md:grid-cols-2 gap-6">
        <section className="border border-base-300 rounded-xl bg-base-100 shadow p-5 space-y-3">
          <h2 className="text-xl font-semibold text-primary">Dados da empresa</h2>
          {loadingProfile && <p className="text-sm text-base-content/70">A carregar...</p>}
          {!loadingProfile && (
            <div className="grid md:grid-cols-2 gap-4">
              {companyFields.map(({ label, value, type }) => (
                <InputField
                  key={label}
                  label={label}
                  type={type}
                  value={value ?? ""}
                  disabled
                  inputClassName="bg-base-200"
                />
              ))}
              <InputField
                label="Descrição"
                as="textarea"
                value={companyProfile?.description ?? ""}
                disabled
                inputClassName="bg-base-200 min-h-[120px]"
              />
              <InputField
                label="Estado"
                value={companyProfile?.status ? "Ativa" : "Em aprovação"}
                disabled
                inputClassName="bg-base-200"
              />
            </div>
          )}
        </section>

        <section className="border border-base-300 rounded-xl bg-base-100 shadow p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold text-primary">Responsável da conta</h2>
          </div>
          {error && (
            <div className="alert alert-error text-sm" role="alert">
              {error}
            </div>
          )}
          {feedback && (
            <div className="alert alert-success text-sm" role="status">
              {feedback}
            </div>
          )}
          <form className="space-y-4" onSubmit={handleSubmit}>
            <InputField
              label="Nome"
              value={form.name}
              onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
              required
            />
            <InputField
              label="Email"
              value={form.email}
              onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
              required
            />
            <InputField
              label="Telefone"
              value={form.phone}
              onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))}
              required
            />
            <InputField
              label="Cargo"
              value={form.position}
              onChange={(e) => setForm((prev) => ({ ...prev, position: e.target.value }))}
              required
            />
            <div className="flex justify-end">
              <Button
                label={saving ? "A guardar..." : "Guardar alterações"}
                disabled={saving}
                fullWidth={false}
                type="submit"
              />
            </div>
          </form>
        </section>
      </div>

      <EmailVerificationModal
        open={verificationOpen}
        email={form.email}
        codeDigits={codeDigits}
        resendCooldown={resendCooldown}
        sending={sendingCode}
        error={codeError}
        onClose={() => setVerificationOpen(false)}
        onChangeDigit={(idx, val) => handleCodeChange(idx, val)}
        onResend={triggerSendCode}
        onSubmit={handleCodeVerify}
      />
    </div>
  );
}

const CODE_LENGTH = 6;
const RESEND_COOLDOWN_SECONDS = 45;
