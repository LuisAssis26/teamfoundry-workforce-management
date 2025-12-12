import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import PropTypes from "prop-types";
import { useNavigate } from "react-router-dom";

// Contexto partilhado pelos passos do registo de empresa.
const CompanyRegisterContext = createContext(null);

const buildInitialData = () => ({
  credentials: {},
  responsible: {},
  company: {},
  submission: {},
});

export function CompanyRegistrationProvider({ children }) {
  const [completedSteps, setCompletedSteps] = useState([]);
  const [pendingStep, setPendingStep] = useState(null);
  const [companyData, setCompanyData] = useState(buildInitialData);
  const navigate = useNavigate();
  const basePath = "/company-register";

  // Marca um passo como concluído e, se indicado, agenda a navegação para o próximo.
  const completeStep = (stepNumber, nextStepNumber = null) => {
    setCompletedSteps((prev) =>
      prev.includes(stepNumber)
        ? prev
        : [...prev, stepNumber].sort((a, b) => a - b)
    );
    if (nextStepNumber) {
      setPendingStep(nextStepNumber);
    }
  };

  // Só permite avançar quando o passo anterior estiver concluído (exceto o primeiro).
  const canAccessStep = useCallback(
    (stepNumber) => (stepNumber === 1 ? true : completedSteps.includes(stepNumber - 1)),
    [completedSteps],
  );

  // Navega para o passo indicado respeitando o guard acima.
  const goToStep = useCallback(
    (stepNumber) => {
      if (canAccessStep(stepNumber)) {
        navigate(`${basePath}/step${stepNumber}`);
      }
    },
    [basePath, canAccessStep, navigate],
  );

  // Quando `pendingStep` é definido, esta effect faz o redirect e limpa o estado.
  useEffect(() => {
    if (!pendingStep) return;
    if (canAccessStep(pendingStep)) {
      navigate(`${basePath}/step${pendingStep}`);
      setPendingStep(null);
    }
  }, [pendingStep, canAccessStep, navigate, basePath]);

  const updateStepData = (section, payload) => {
    setCompanyData((prev) => ({
      ...prev,
      [section]: {
        ...prev[section],
        ...payload,
      },
    }));
  };

  // Facilita iniciar um novo registo logo após uma submissão bem-sucedida.
  const resetFlow = useCallback(() => {
    setCompletedSteps([]);
    setCompanyData(buildInitialData());
    setPendingStep(null);
  }, []);

  const value = useMemo(
    () => ({
      completedSteps,
      completeStep,
      canAccessStep,
      goToStep,
      companyData,
      updateStepData,
      resetFlow,
    }),
    [completedSteps, canAccessStep, goToStep, companyData, resetFlow],
  );

  return (
    <CompanyRegisterContext.Provider value={value}>
      {children}
    </CompanyRegisterContext.Provider>
  );
}

CompanyRegistrationProvider.propTypes = {
  children: PropTypes.node.isRequired,
};

export function useCompanyRegistration() {
  const context = useContext(CompanyRegisterContext);
  if (!context) {
    throw new Error("useCompanyRegistration must be used within CompanyRegistrationProvider");
  }
  return context;
}
