import React, { useMemo, useState, useEffect } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import registerIllustration from "../../../assets/images/logo/teamFoundry_LogoPrimary.png";
import { CompanyRegistrationProvider, useCompanyRegistration } from "./CompanyRegisterContext.jsx";

// Configura o wizard da empresa (4 etapas).
const steps = [
  { id: "step1", path: "/company-register/step1", label: "Credenciais" },
  { id: "step2", path: "/company-register/step2", label: "Responsável" },
  { id: "step3", path: "/company-register/step3", label: "Dados da empresa" },
  { id: "step4", path: "/company-register/step4", label: "Submissão" },
];

/**
 * Responsável por proteger a navegação e fornecer o contexto do registo de empresa.
 */
function CompanyRegisterLayoutInner() {
  const location = useLocation();
  const navigate = useNavigate();
  const {
    canAccessStep,
    goToStep,
    completeStep,
    completedSteps,
    companyData,
    updateStepData,
    resetFlow,
  } = useCompanyRegistration();

  const currentStepIndex = useMemo(() => {
    const foundIndex = steps.findIndex((step) => location.pathname.startsWith(step.path));
    return foundIndex >= 0 ? foundIndex : 0;
  }, [location.pathname]);

  // Garante que não há saltos de rota para passos ainda não preenchidos.
  useEffect(() => {
    const currentStepNumber = steps.findIndex((step) => location.pathname.startsWith(step.path)) + 1;
    if (currentStepNumber === 0) {
      navigate(steps[0].path, { replace: true });
      return;
    }

    if (!canAccessStep(currentStepNumber)) {
      const highestAccessible = steps.reduce((acc, _, idx) => (canAccessStep(idx + 1) ? idx + 1 : acc), 1);
      goToStep(highestAccessible);
    }
  }, [location.pathname, canAccessStep, goToStep, navigate]);

  return (
    <main className="min-h-screen bg-base-200 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-5xl bg-base-100 shadow-xl rounded-3xl overflow-hidden border border-base-200">
        <div className="grid grid-cols-1 md:grid-cols-2">
          <div className="hidden md:block bg-base-200">
            <img
              src={registerIllustration}
              alt="Fluxo de registo de empresa"
              className="h-full w-full object-contain p-8 bg-base-100"
            />
          </div>

          <div className="p-8 sm:p-10 lg:p-12 flex flex-col">
            <nav className="flex flex-wrap items-center justify-center gap-2 text-sm">
              {steps.map((step, index) => {
                const isActive = index === currentStepIndex;
                const isCompleted = completedSteps.includes(index + 1) && !isActive;
                const baseClasses = "btn btn-sm rounded-full px-4 normal-case border";
                const stateClasses = isActive
                  ? "btn-primary border-primary"
                  : isCompleted
                    ? "btn-success border-success"
                    : "btn-ghost border-base-200";

                return (
                  <React.Fragment key={step.id}>
                    <button
                      type="button"
                      className={`${baseClasses} ${stateClasses}`}
                      onClick={() => goToStep(index + 1)}
                      disabled={!canAccessStep(index + 1)}
                    >
                      <span aria-hidden="true">{index + 1}</span>
                      <span className="sr-only">{step.label}</span>
                    </button>
                    {index < steps.length - 1 && (
                      <span className="text-base-content/40">
                        <i className="bi bi-chevron-right"></i>
                      </span>
                    )}
                  </React.Fragment>
                );
              })}
            </nav>

            <div className="mt-8 flex-1">
              <Outlet
                context={{
                  companyData,
                  updateStepData,
                  currentStepIndex,
                  steps,
                  goToStep,
                  completeStep,
                  resetFlow,
                }}
              />
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

/**
 * Wrapper que injeta o provider esperado por todos os passos.
 */
export default function CompanyRegisterLayout() {
  return (
    <CompanyRegistrationProvider>
      <CompanyRegisterLayoutInner />
    </CompanyRegistrationProvider>
  );
}
