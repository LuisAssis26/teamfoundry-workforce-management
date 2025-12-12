import React, { useState } from "react";
import { useOutletContext } from "react-router-dom";
import InputField from "../../../components/ui/Input/InputField.jsx";
import PhoneInput from "../../../components/ui/Input/PhoneInput.jsx";
import Button from "../../../components/ui/Button/Button.jsx";

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phoneRegex = /^\+\d{7,20}$/;

/**
 * Passo 2 do registo de empresa: dados do responsável.
 */
export default function CompanyRegisterStep2() {
  const { companyData, updateStepData, completeStep, goToStep } = useOutletContext();
  const [responsibleName, setResponsibleName] = useState(companyData.responsible?.responsibleName || "");
  const [responsibleRole, setResponsibleRole] = useState(companyData.responsible?.responsibleRole || "");
  const [responsibleEmail, setResponsibleEmail] = useState(companyData.responsible?.responsibleEmail || "");
  const [responsiblePhone, setResponsiblePhone] = useState(companyData.responsible?.responsiblePhone || "");
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const newErrors = {};
    if (!responsibleName.trim()) {
      newErrors.responsibleName = "Informe o nome do responsável.";
    }
    if (!responsibleRole.trim()) {
      newErrors.responsibleRole = "Informe o cargo.";
    }
    if (!responsibleEmail.trim() || !emailRegex.test(responsibleEmail.trim())) {
      newErrors.responsibleEmail = "Informe um email corporativo válido.";
    }
    if (!responsiblePhone.trim()) {
      newErrors.responsiblePhone = "Informe um telefone.";
    } else if (!phoneRegex.test(responsiblePhone.trim())) {
      newErrors.responsiblePhone = "Telefone deve estar no formato +<indicativo><número>.";
    }
    return newErrors;
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    updateStepData("responsible", {
      responsibleName: responsibleName.trim(),
      responsibleRole: responsibleRole.trim(),
      responsibleEmail: responsibleEmail.trim(),
      responsiblePhone: responsiblePhone.trim(),
    });
    completeStep(2, 3);
    setLoading(false);
  };

  return (
    <section className="flex h-full flex-col">
      <div>
        <p className="text-sm font-semibold text-primary uppercase tracking-wide">Passo 2 de 4</p>
        <h1 className="mt-2 text-3xl font-bold text-accent">Responsável pela empresa</h1>
        <p className="mt-4 text-base text-base-content/70">
          Identifique a pessoa que será o ponto de contacto da empresa.
        </p>
      </div>

      <form className="mt-8 flex-1 space-y-6" onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <InputField
            label="Nome do responsável"
            value={responsibleName}
            onChange={(event) => setResponsibleName(event.target.value)}
            error={errors.responsibleName}
          />
          <InputField
            label="Cargo do responsável"
            value={responsibleRole}
            onChange={(event) => setResponsibleRole(event.target.value)}
            error={errors.responsibleRole}
          />
        </div>

        <InputField
          label="Email do responsável"
          type="email"
          value={responsibleEmail}
          onChange={(event) => setResponsibleEmail(event.target.value)}
          error={errors.responsibleEmail}
        />

        <PhoneInput
          label="Telefone do responsável"
          value={responsiblePhone}
          onChange={setResponsiblePhone}
          error={errors.responsiblePhone}
        />

        <div className="mt-10 grid grid-cols-2 gap-4">
          <Button
            label="Voltar"
            variant="outline"
            type="button"
            onClick={() => goToStep(1)}
            className="btn-outline border-base-300 text-base-content/60"
          />
          <Button label="Avançar" variant="primary" type="submit" disabled={loading} />
        </div>
      </form>
    </section>
  );
}
