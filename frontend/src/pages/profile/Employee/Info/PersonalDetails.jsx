import { useEffect, useMemo, useState } from "react";
import InputField from "../../../../components/ui/Input/InputField.jsx";
import Button from "../../../../components/ui/Button/Button.jsx";
import InfoLayout from "./InfoLayout.jsx";
import { updateEmployeeProfile } from "../../../../api/profile/employeeProfile.js";
import { useEmployeeProfile } from "../EmployeeProfileContext.jsx";
import { formatName } from "../utils/profileUtils.js";
import PhoneInput from "../../../../components/ui/Input/PhoneInput.jsx";
import SelectDropdown from "../../../../components/ui/Dropdown/SelectDropdown.jsx";

const genderOptions = [
  { value: "MALE", label: "Masculino" },
  { value: "FEMALE", label: "Feminino" },
  { value: "OTHER", label: "Outro" },
];

export default function PersonalDetails() {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    birthDate: "",
    gender: "",
    nationality: "",
    nif: "",
    phone: "",
  });
  const { profile, refreshProfile, personalData, setPersonalData } = useEmployeeProfile();
  const [displayName, setDisplayName] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState("");
  const [error, setError] = useState("");

  const genderPlaceholder = useMemo(() => "Selecione o género", []);

  useEffect(() => {
    let isMounted = true;
    // Prefere cache do contexto; só chama refreshProfile se ainda não houver dados guardados.
    async function syncProfile() {
      try {
        const source = personalData || profile || (await refreshProfile());
        if (!isMounted || !source) return;
        setFormData({
          firstName: source?.firstName ?? "",
          lastName: source?.lastName ?? "",
          birthDate: source?.birthDate ?? "",
          gender: source?.gender ?? "",
          nationality: source?.nationality ?? "",
          nif: source?.nif?.toString() ?? "",
          phone: source?.phone ?? "",
        });
        setDisplayName(formatName(source?.firstName, source?.lastName));
        if (!personalData) setPersonalData(source);
      } catch (err) {
        if (isMounted) {
          setError(err.message || "Não foi possível carregar o perfil.");
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    syncProfile();
    return () => {
      isMounted = false;
    };
  }, [profile, refreshProfile]);

  const handleChange = (field) => (event) => {
    const { value } = event.target;
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (feedback) setFeedback("");
    if (error) setError("");
  };

  const handlePhoneChange = (value) => {
    setFormData((prev) => ({ ...prev, phone: value }));
    if (feedback) setFeedback("");
    if (error) setError("");
  };

  const handleSubmit = async (event) => {
    // Persiste alterações e atualiza o cache no contexto para evitar flicker entre tabs.
    event.preventDefault();
    setSaving(true);
    setFeedback("");
    setError("");
    try {
      const payload = {
        ...formData,
        nif: formData.nif ? Number(formData.nif) : null,
      };
      const response = await updateEmployeeProfile(payload);
      setFeedback("Dados atualizados com sucesso!");
      setDisplayName(formatName(response?.firstName, response?.lastName));
      setPersonalData(response);
      refreshProfile();
    } catch (err) {
      setError(err.message || "Não foi possível guardar as alterações.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <InfoLayout name={displayName}>
      <div className="mt-6 rounded-xl border border-base-300 bg-base-100 shadow">
        <form onSubmit={handleSubmit}>
          <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-4">
            <InputField
              label="Nome"
              placeholder="Introduza o nome"
              value={formData.firstName}
              onChange={handleChange("firstName")}
              disabled={loading || saving}
            />
            <InputField
              label="Apelido"
              placeholder="Introduza o apelido"
              value={formData.lastName}
              onChange={handleChange("lastName")}
              disabled={loading || saving}
            />
            <InputField
              label="Data de Nascimento"
              type="date"
              value={formData.birthDate}
              onChange={handleChange("birthDate")}
              disabled={loading || saving}
            />
            <SelectDropdown
              label="Género"
              value={formData.gender || ""}
              onChange={(val) => {
                setFormData((prev) => ({ ...prev, gender: val }));
                if (feedback) setFeedback("");
                if (error) setError("");
              }}
              options={genderOptions}
              placeholder={genderPlaceholder}
              disabled={loading || saving}
            />
            <InputField
              label="Nacionalidade"
              placeholder="Ex.: Portuguesa"
              value={formData.nationality}
              onChange={handleChange("nationality")}
              disabled={loading || saving}
            />
            <InputField
              label="NIF"
              placeholder="123456789"
              value={formData.nif}
              onChange={handleChange("nif")}
              disabled={loading || saving}
            />
            <PhoneInput
              label="Contacto"
              placeholder="912 345 678"
              value={formData.phone}
              onChange={handlePhoneChange}
              disabled={loading || saving}
              className="md:col-span-2"
            />
          </div>

          <div className="px-6 pb-6 flex flex-col items-center gap-3">
            {error && (
              <div className="alert alert-error w-full max-w-md text-sm" role="alert">
                {error}
              </div>
            )}
            {feedback && (
              <div className="alert alert-success w-full max-w-md text-sm" role="status">
                {feedback}
              </div>
            )}
            <div className="w-56">
              <Button label={saving ? "A guardar..." : "Guardar"} type="submit" disabled={saving || loading} />
            </div>
          </div>
        </form>
      </div>
    </InfoLayout>
  );
}

