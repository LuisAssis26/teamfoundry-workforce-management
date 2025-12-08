import { useEffect, useMemo, useRef, useState } from "react";
import InfoLayout from "./InfoLayout.jsx";
import Button from "../../../../components/ui/Button/Button.jsx";
import MultiSelectDropdown from "../../../../components/ui/Dropdown/MultiSelectDropdown.jsx";
import { fetchProfileOptions } from "../../../../api/profile/profileOptions.js";
import { updateEmployeePreferences } from "../../../../api/profile/employeePreferences.js";
import { useEmployeeProfile } from "../EmployeeProfileContext.jsx";
import { formatName, normalizeSelection } from "../utils/profileUtils.js";

const initialForm = {
  roles: [],
  areas: [],
  skills: [],
};

export default function Preferences() {
  const [form, setForm] = useState(initialForm);
  const [options, setOptions] = useState({ functions: [], geoAreas: [], competences: [] });
  const {
    profile,
    refreshProfile,
    preferencesData,
    setPreferencesData,
    preferencesLoaded,
    setPreferencesLoaded,
    refreshPreferencesData,
    profileOptionsData,
    setProfileOptionsData,
  } = useEmployeeProfile();
  const [displayName, setDisplayName] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState("");
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const hasLoadedOnce = useRef(false);

  useEffect(() => {
    let isMounted = true;

    // Carrega opções (sempre) e preferências (cache do contexto se existir).
    async function loadData() {
      setLoading(true);
      setError("");
      try {
        const profileSource = profile || (await refreshProfile());
        if (isMounted) {
          setDisplayName(formatName(profileSource?.firstName, profileSource?.lastName));
        }

        // Reutiliza cache de preferências se já carregadas.
        if (preferencesData && (preferencesLoaded || hasLoadedOnce.current)) {
          setForm({
            roles: normalizeSelection(preferencesData?.roles?.length ? preferencesData.roles : preferencesData?.role ? [preferencesData.role] : []),
            areas: normalizeSelection(preferencesData?.areas),
            skills: normalizeSelection(preferencesData?.skills),
          });
          setLoading(false);
          hasLoadedOnce.current = true;
          return;
        }

        const optionsData = profileOptionsData || (await fetchProfileOptions());
        const preferencesPayload = preferencesData || (await refreshPreferencesData());

        if (!isMounted) return;

        setOptions({
          functions: Array.isArray(optionsData?.functions) ? optionsData.functions : [],
          geoAreas: Array.isArray(optionsData?.geoAreas) ? optionsData.geoAreas : [],
          competences: Array.isArray(optionsData?.competences) ? optionsData.competences : [],
        });

        setForm({
          roles: normalizeSelection(preferencesPayload?.roles?.length ? preferencesPayload.roles : preferencesPayload?.role ? [preferencesPayload.role] : []),
          areas: normalizeSelection(preferencesPayload?.areas),
          skills: normalizeSelection(preferencesPayload?.skills),
        });

        if (!preferencesData) {
          setPreferencesData(preferencesPayload);
          setPreferencesLoaded(true);
        }
        if (!profileOptionsData) setProfileOptionsData(optionsData);
        hasLoadedOnce.current = true;
      } catch (err) {
        if (isMounted) {
          setError(err.message || "Nao foi possivel carregar as preferencias.");
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    loadData();
    return () => {
      isMounted = false;
    };
  }, [profile, refreshProfile, preferencesData, setPreferencesData]);

  const functionOptions = useMemo(() => {
    // Garante que funções já escolhidas continuam visíveis mesmo que não venham da API.
    const list = Array.isArray(options.functions) ? [...options.functions] : [];
    const extras = (form.roles || []).filter((r) => r && !list.includes(r));
    return [...extras, ...list];
  }, [options.functions, form.roles]);

  const handleSubmit = async (event) => {
    // Valida e guarda preferências, sincronizando o contexto local.
    event.preventDefault();
    setFeedback("");
    setError("");

    const validationErrors = validateForm(form);
    if (Object.keys(validationErrors).length > 0) {
      setFieldErrors(validationErrors);
      return;
    }

    setSaving(true);
    try {
      await updateEmployeePreferences({
        role: form.roles?.[0] || "",
        roles: form.roles,
        areas: form.areas,
        skills: form.skills,
      });
      setFeedback("Preferencias atualizadas com sucesso.");
      setPreferencesData({
        ...form,
        role: form.roles?.[0] || "",
      });
    } catch (err) {
      setError(err.message || "Nao foi possivel guardar as preferencias.");
    } finally {
      setSaving(false);
    }
  };

  const clearFieldError = (field) => {
    // Remove erro específico e limpa alertas globais.
    setFieldErrors((prev) => ({ ...prev, [field]: "" }));
    if (error) setError("");
    if (feedback) setFeedback("");
  };

  const handleRoleDropdownChange = (values) => {
    setForm((prev) => ({ ...prev, roles: normalizeSelection(values) }));
    clearFieldError("role");
  };

  const handleAreasChange = (values) => {
    setForm((prev) => ({ ...prev, areas: normalizeSelection(values) }));
    clearFieldError("areas");
  };

  const handleSkillsChange = (values) => {
    setForm((prev) => ({ ...prev, skills: normalizeSelection(values) }));
    clearFieldError("skills");
  };

  return (
    <InfoLayout name={displayName}>
      <div className="mt-6 rounded-xl border border-base-300 bg-base-100 shadow min-h-[55vh]">
        <form onSubmit={handleSubmit}>
          <div className="p-6 max-w-3xl mx-auto">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <MultiSelectDropdown
                  label="Função preferencial"
                  options={functionOptions}
                  selectedOptions={form.roles}
                  onChange={handleRoleDropdownChange}
                  placeholder="Selecione a função"
                  disabled={loading || saving || functionOptions.length === 0}
                />
                {fieldErrors.roles && (
                  <p className="mt-2 text-sm text-error">{fieldErrors.roles}</p>
                )}
              </div>

              <div>
                <MultiSelectDropdown
                  label="Área(s) Geográfica(s)"
                  options={options.geoAreas}
                  selectedOptions={form.areas}
                  onChange={handleAreasChange}
                  placeholder="Adicionar área geográfica"
                  disabled={loading || saving}
                />
                {fieldErrors.areas && (
                  <p className="mt-2 text-sm text-error">{fieldErrors.areas}</p>
                )}
              </div>

              <div className="md:col-span-2">
                <MultiSelectDropdown
                  label="Competência(s)"
                  options={options.competences}
                  selectedOptions={form.skills}
                  onChange={handleSkillsChange}
                  placeholder="Adicionar competência"
                  disabled={loading || saving}
                  maxVisibleChips={3}
                />
                {fieldErrors.skills && (
                  <p className="mt-2 text-sm text-error">{fieldErrors.skills}</p>
                )}
              </div>
            </div>
          </div>

          <div className="px-6 pb-6 flex flex-col items-center gap-3 max-w-3xl mx-auto">
            {error && (
              <div className="alert alert-error w-full text-sm" role="alert">
                {error}
              </div>
            )}
            {feedback && (
              <div className="alert alert-success w-full text-sm" role="status">
                {feedback}
              </div>
            )}
            <div className="w-56">
              <Button
                label={saving ? "A guardar..." : loading ? "A carregar..." : "Guardar"}
                type="submit"
                disabled={saving || loading}
              />
            </div>
          </div>
        </form>
      </div>
    </InfoLayout>
  );
}

function validateForm(form) {
  const errors = {};
  if (!Array.isArray(form.roles) || form.roles.length === 0) {
    errors.role = "Selecione pelo menos uma funcao.";
  }
  if (!Array.isArray(form.areas) || form.areas.length === 0) {
    errors.areas = "Selecione pelo menos uma area geografica.";
  }
  if (!Array.isArray(form.skills) || form.skills.length === 0) {
    errors.skills = "Selecione pelo menos uma competencia.";
  }
  return errors;
}
