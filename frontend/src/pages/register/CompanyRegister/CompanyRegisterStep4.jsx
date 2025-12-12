import React, { useState } from "react";
import { useOutletContext } from "react-router-dom";
import Button from "../../../components/ui/Button/Button.jsx";
import { registerCompany } from "../../../api/company/company.js";

/**
 * Passo 4 do registo de empresa: confirmação e submissão.
 */
export default function CompanyRegisterStep4() {
  const { companyData, goToStep, updateStepData, resetFlow } = useOutletContext();
  const [termsAccepted, setTermsAccepted] = useState(companyData.submission?.termsAccepted || false);
  const [loading, setLoading] = useState(false);
  const [feedback, setFeedback] = useState(null);
  const [submitted, setSubmitted] = useState(false);

  // Consolida todos os dados e envia o pedido de registo ao backend.
  const handleSubmit = async (event) => {
    event.preventDefault();
    if (loading || submitted) return;
    setFeedback(null);
    setLoading(true);
    try {
      const response = await registerCompany({
        credentialEmail: companyData.credentials?.credentialEmail,
        password: companyData.credentials?.password,
        responsibleName: companyData.responsible?.responsibleName,
        responsiblePosition: companyData.responsible?.responsibleRole,
        responsibleEmail: companyData.responsible?.responsibleEmail,
        responsiblePhone: companyData.responsible?.responsiblePhone,
        companyName: companyData.company?.companyName,
        nif: companyData.company?.nif,
        activitySectors: companyData.company?.activitySectors || [],
        country: companyData.company?.country,
        address: companyData.company?.address,
        website: companyData.company?.website,
        description: companyData.company?.description,
        termsAccepted,
      });
      setFeedback({
        type: "success",
        message: response?.message || "Registo submetido com sucesso.",
      });
      setSubmitted(true);
      setTermsAccepted(false);
    } catch (error) {
      setFeedback({ type: "error", message: error.message || "Falha ao submeter o registo." });
    } finally {
      setLoading(false);
    }
  };

  // Permite iniciar outro registo sem necessidade de recarregar a página.
  const handleNewRegistration = () => {
    resetFlow();
    setFeedback(null);
    setSubmitted(false);
    setTermsAccepted(false);
    goToStep(1);
  };

  return (
    <section className="flex h-full flex-col">
      <div>
        <p className="text-sm font-semibold text-primary uppercase tracking-wide">Passo 4 de 4</p>
        <h1 className="mt-2 text-3xl font-bold text-accent">Revisão e Submissão</h1>
        <p className="mt-4 text-base text-base-content/70">
          Confirme os dados abaixo e submeta o pedido de registo.
        </p>
      </div>

      <form className="mt-8 flex-1 space-y-6" onSubmit={handleSubmit}>
          <SummaryCard title="Credenciais e Responsável">
            <div className="grid grid-cols-2 items-center gap-2">
              <SummaryItem label="Email" value={companyData.credentials?.credentialEmail} />
              <SummaryItem label="Nome" value={companyData.responsible?.responsibleName} />
              <SummaryItem label="Cargo" value={companyData.responsible?.responsibleRole} />
              <SummaryItem label="Email" value={companyData.responsible?.responsibleEmail} />
              <SummaryItem label="Telefone" value={companyData.responsible?.responsiblePhone} />
            </div>
            
          </SummaryCard>
        <SummaryCard title="Empresa">
            <SummaryItem label="Nome" value={companyData.company?.companyName} />

            <div className="grid grid-cols-2 items-center gap-2">
              <SummaryItem label="NIF" value={companyData.company?.nif} />
              <SummaryItem label="País" value={companyData.company?.country} />
            </div>
            
            <SummaryItem label="Morada" value={companyData.company?.address} />
            <SummaryItem label="Website" value={companyData.company?.website || "-"} />
            <SummaryItem
              label="Áreas de atividade"
              value={(companyData.company?.activitySectors || []).join(", ")}
            />
          </SummaryCard>

        <SummaryCard title="Descrição">
          <p className="text-sm text-base-content/80 whitespace-pre-wrap">
            {companyData.company?.description || "Sem descrição."}
          </p>
        </SummaryCard>

        <label className="flex items-center gap-3">
          <input
            type="checkbox"
            className="checkbox checkbox-primary"
            checked={termsAccepted}
            disabled={submitted}
            onChange={(event) => {
              setTermsAccepted(event.target.checked);
              updateStepData("submission", { termsAccepted: event.target.checked });
            }}
          />
          <span className="text-sm">Aceito os termos e condições.</span>
        </label>

        {feedback && (
          <div className={`alert ${feedback.type === "success" ? "alert-success" : "alert-error"}`}>
            <span>{feedback.message}</span>
          </div>
        )}

        <div className="mt-8 grid grid-cols-2 gap-4">
          <Button type="button" variant="outline" label="Voltar" onClick={() => goToStep(3)} disabled={loading || submitted} />
          <Button
            type="submit"
            variant="primary"
            label={loading ? "A enviar..." : submitted ? "Submetido" : "Submeter"}
            disabled={!termsAccepted || loading || submitted}
          />
        </div>

        {submitted && (
          <div className="mt-6">
            <Button
              type="button"
              variant="secondary"
              label="Iniciar novo registo"
              className="btn-outline w-full"
              onClick={handleNewRegistration}
            />
          </div>
        )}
      </form>
    </section>
  );
}

function SummaryCard({ title, children }) {
  return (
    <div className="rounded-2xl border border-base-200 bg-base-100 p-4 shadow-sm">
      <h3 className="font-semibold text-primary mb-3">{title}</h3>
      <div className="space-y-2 text-sm text-base-content/80">{children}</div>
    </div>
  );
}

function SummaryItem({ label, value }) {
  return (
    <p>
      <span className="font-semibold text-base-content">{label}: </span>
      <span>{value || "-"}</span>
    </p>
  );
}
