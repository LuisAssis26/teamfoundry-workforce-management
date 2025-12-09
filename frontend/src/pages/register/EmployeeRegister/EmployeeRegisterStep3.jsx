import React, { useEffect, useMemo, useState } from "react";
import { useOutletContext } from "react-router-dom";
import Button from "../../../components/ui/Button/Button.jsx";
import MultiSelectDropdown from "../../../components/ui/Dropdown/MultiSelectDropdown.jsx";
import { registerStep3 } from "../../../api/auth/auth.js";
import { fetchProfileOptions } from "../../../api/profile/profileOptions.js";

const FALLBACK_FUNCTIONS = ["Eletricista", "Canalizador", "Soldador", "Carpinteiro", "Pedreiro"];
const FALLBACK_COMPETENCES = ["Eletricista", "Canalizador", "Soldador", "Técnico de AVAC", "Pintor"];
const FALLBACK_AREAS = ["Lisboa", "Porto", "Braga", "Faro", "Madeira", "Açores"];

/**
 * Passo 3: guarda preferências (função, áreas e competências) e termos.
 * Depois de validado, envia para o backend que sincroniza as tabelas associativas.
 */
export default function EmployeeRegisterStep3() {
    const { registerData, updateStepData, completeStep, goToStep } = useOutletContext();
    const emailFromState = registerData.credentials?.email || "";

    const initialRoles = registerData.preferences?.roles ||
        (registerData.preferences?.role ? [registerData.preferences.role] : []);
    const initialCompetences = registerData.preferences?.competences ||
        (registerData.preferences?.competence ? [registerData.preferences.competence] : []);

    const [selectedRoles, setSelectedRoles] = useState(initialRoles);
    const [selectedAreas, setSelectedAreas] = useState(registerData.preferences?.areas || []);
    const [selectedCompetences, setSelectedCompetences] = useState(initialCompetences);
    const [termsAccepted, setTermsAccepted] = useState(registerData.preferences?.termsAccepted || false);

    const [functionOptions, setFunctionOptions] = useState(FALLBACK_FUNCTIONS);
    const [competenceOptions, setCompetenceOptions] = useState(FALLBACK_COMPETENCES);
    const [geoAreas, setGeoAreas] = useState(FALLBACK_AREAS);
    const [optionsError, setOptionsError] = useState("");

    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [serverError, setServerError] = useState("");

    // Repõe as seleções guardadas caso o utilizador regresse ao passo 3.
    useEffect(() => {
        setSelectedRoles(registerData.preferences?.roles ||
            (registerData.preferences?.role ? [registerData.preferences.role] : []));
        setSelectedAreas(registerData.preferences?.areas || []);
        setSelectedCompetences(registerData.preferences?.competences ||
            (registerData.preferences?.competence ? [registerData.preferences.competence] : []));
        setTermsAccepted(registerData.preferences?.termsAccepted || false);
    }, [registerData.preferences]);

    // Busca opções dinâmicas do backend (com fallbacks) logo que o componente monta.
    useEffect(() => {
        let isMounted = true;

        const loadOptions = async () => {
            try {
                const data = await fetchProfileOptions();
                if (!isMounted) return;

                const safeFunctions = (data?.functions?.length ? data.functions : FALLBACK_FUNCTIONS).sort();
                const safeCompetences = (data?.competences?.length ? data.competences : FALLBACK_COMPETENCES).sort();
                const safeAreas = (data?.geoAreas?.length ? data.geoAreas : FALLBACK_AREAS).sort();

                setFunctionOptions(safeFunctions);
                setCompetenceOptions(safeCompetences);
                setGeoAreas(safeAreas);

                setSelectedRoles((prev) => prev.filter((value) => safeFunctions.includes(value)));
                setSelectedCompetences((prev) => prev.filter((value) => safeCompetences.includes(value)));
                setSelectedAreas((prev) => prev.filter((value) => safeAreas.includes(value)));
                setOptionsError("");
            } catch (error) {
                if (!isMounted) return;
                setOptionsError("Falha ao carregar opções. A usar valores padrão.");
                setFunctionOptions(FALLBACK_FUNCTIONS);
                setCompetenceOptions(FALLBACK_COMPETENCES);
                setGeoAreas(FALLBACK_AREAS);
            }
        };

        loadOptions();
        return () => {
            isMounted = false;
        };
    }, []);

    const isFormValid = useMemo(() => {
        return selectedRoles.length > 0 && selectedAreas.length > 0 && selectedCompetences.length > 0 && termsAccepted;
    }, [selectedRoles, selectedAreas, selectedCompetences, termsAccepted]);

    // Mantém mensagens de erro coerentes com as validações visuais.
    const validateFields = () => {
        const newErrors = {};

        if (selectedRoles.length === 0) {
            newErrors.role = "Selecione pelo menos uma função.";
        }

        if (selectedCompetences.length === 0) {
            newErrors.competence = "Selecione pelo menos uma competência.";
        }

        if (selectedAreas.length === 0) {
            newErrors.areas = "Selecione pelo menos uma área geográfica.";
        }

        if (!termsAccepted) {
            newErrors.termsAccepted = "É necessário aceitar os termos e condições.";
        }

        return newErrors;
    };

    // Envia preferências/termos e só avança se o backend confirmar o payload.
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
            // O backend atualmente aceita apenas uma função principal, por isso enviamos o primeiro item selecionado.
            await registerStep3({
                email: emailFromState,
                role: selectedRoles[0] || "",
                areas: selectedAreas,
                skills: selectedCompetences,
                termsAccepted,
            });

            updateStepData("preferences", {
                roles: selectedRoles,
                areas: selectedAreas,
                competences: selectedCompetences,
                termsAccepted,
            });
            completeStep(3, 4);
        } catch (error) {
            console.error("Erro ao guardar preferências:", error);
            setServerError(error.message || "Ocorreu um erro inesperado.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <section className="flex h-full flex-col">
            <div>
                <p className="text-sm font-semibold text-primary uppercase tracking-wide">
                    Passo 3 de 4
                </p>
                <h1 className="mt-2 text-3xl font-bold text-accent">
                    Preferências de trabalho
                </h1>
                <p className="mt-4 text-base text-base-content/70">
                    Ajude-nos a encontrar oportunidades alinhadas com o seu perfil.
                </p>
            </div>

            {serverError && (
                <div className="alert alert-error mt-6">
                    <span>{serverError}</span>
                </div>
            )}

            {optionsError && (
                <div className="alert alert-warning">
                    <span>{optionsError}</span>
                </div>
            )}

            <form className="mt-8 flex-1 space-y-6" onSubmit={handleSubmit} noValidate>
                <MultiSelectDropdown
                    label="Funções"
                    options={functionOptions}
                    selectedOptions={selectedRoles}
                    onChange={setSelectedRoles}
                    placeholder="Selecione as funções que pretende exercer"
                />
                {errors.role && <p className="text-sm text-error">{errors.role}</p>}

                <MultiSelectDropdown
                    label="Área geográfica"
                    options={geoAreas}
                    selectedOptions={selectedAreas}
                    onChange={setSelectedAreas}
                    placeholder="Selecione as áreas em que pretende trabalhar"
                />
                {errors.areas && <p className="text-sm text-error">{errors.areas}</p>}

                <MultiSelectDropdown
                    label="Competências"
                    options={competenceOptions}
                    selectedOptions={selectedCompetences}
                    onChange={setSelectedCompetences}
                    placeholder="Selecione as suas competências principais"
                />
                {errors.competence && <p className="text-sm text-error">{errors.competence}</p>}

                <div className="space-y-2">
                    <label className="label cursor-pointer justify-start gap-3 items-start whitespace-normal">
                        <input
                            type="checkbox"
                            className={`checkbox ${errors.termsAccepted ? "checkbox-error" : "checkbox-primary"}`}
                            checked={termsAccepted}
                            onChange={(event) => setTermsAccepted(event.target.checked)}
                        />
                        <span className="label-text text-sm text-base-content/80 leading-relaxed flex-1">
                            Ao registar-se, o utilizador concorda em fornecer informações verdadeiras e autoriza a
                            TeamFoundry a utilizar os dados apenas para fins de recrutamento e gestão de carreira.
                        </span>
                    </label>
                    {errors.termsAccepted && (
                        <p className="text-sm text-error">{errors.termsAccepted}</p>
                    )}
                </div>

                <div className="mt-10 grid grid-cols-2 gap-4">
                    <Button
                        label="← Voltar"
                        variant="outline"
                        className="btn-outline border-base-300"
                        onClick={() => goToStep(2)}
                    />
                    <Button
                        label={loading ? "A guardar..." : "Estou quase →"}
                        type="submit"
                        variant="primary"
                        disabled={!isFormValid || loading}
                    />
                </div>
            </form>
        </section>
    );
}
