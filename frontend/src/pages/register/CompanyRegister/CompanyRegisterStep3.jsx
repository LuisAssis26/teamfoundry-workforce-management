import React, { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import InputField from "../../../components/ui/Input/InputField.jsx";
import Button from "../../../components/ui/Button/Button.jsx";
import MultiSelectDropdown from "../../../components/ui/MultiSelect/MultiSelectDropdown.jsx";
import { fetchCompanyOptions } from "../../../api/company/company.js";

const DESCRIPTION_MAX_CHARS = 600;

/**
 * Passo 3 do registo de empresa: dados institucionais.
 */
export default function CompanyRegisterStep3() {
  const { companyData, updateStepData, completeStep, goToStep } = useOutletContext();
  const [companyName, setCompanyName] = useState(companyData.company?.companyName || "");
  const [nif, setNif] = useState(companyData.company?.nif || "");
  const [activitySectors, setActivitySectors] = useState(companyData.company?.activitySectors || []);
  const [country, setCountry] = useState(companyData.company?.country || "");
  const [address, setAddress] = useState(companyData.company?.address || "");
  const [website, setWebsite] = useState(companyData.company?.website || "");
  const [description, setDescription] = useState(companyData.company?.description || "");
  const [errors, setErrors] = useState({});
  const [options, setOptions] = useState({ activitySectors: [], countries: [] });
  const [loadingOptions, setLoadingOptions] = useState(true);
  const descriptionLength = description.length;

  useEffect(() => {
    let isMounted = true;
    setLoadingOptions(true);
    fetchCompanyOptions()
      .then((data) => {
        if (!isMounted) return;
        setOptions({
          activitySectors: data?.activitySectors ?? [],
          countries: data?.countries ?? [],
        });
      })
      .catch((error) => {
        console.error("Falha ao carregar opções de empresa", error);
        if (isMounted) {
          setOptions({ activitySectors: [], countries: [] });
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoadingOptions(false);
        }
      });
    return () => {
      isMounted = false;
    };
  }, []);

  const validate = () => {
    const newErrors = {};
    if (!companyName.trim()) newErrors.companyName = "Informe o nome da empresa.";
    if (!nif || Number.isNaN(Number(nif))) newErrors.nif = "Informe um NIF numérico.";
    if (activitySectors.length === 0) newErrors.activitySectors = "Selecione pelo menos um setor.";
    if (!country) newErrors.country = "Selecione um país.";
    if (!address.trim()) newErrors.address = "Informe a morada.";
    return newErrors;
  };

  const handleDescriptionChange = (event) => {
    const value = event.target.value;
    if (value.length <= DESCRIPTION_MAX_CHARS) {
      setDescription(value);
    } else {
      setDescription(value.slice(0, DESCRIPTION_MAX_CHARS));
    }
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    updateStepData("company", {
      companyName: companyName.trim(),
      nif: Number(nif),
      activitySectors,
      country,
      address: address.trim(),
      website: website.trim(),
      description,
    });
    completeStep(3, 4);
  };

  return (
    <section className="flex h-full flex-col">
      <div>
        <p className="text-sm font-semibold text-primary uppercase tracking-wide">Passo 3 de 4</p>
        <h1 className="mt-2 text-3xl font-bold text-accent">Informações da Empresa</h1>
        <p className="mt-4 text-base text-base-content/70">
          Conte-nos um pouco sobre a empresa e as áreas em que atua.
        </p>
      </div>

      <form className="mt-8 flex-1 space-y-6" onSubmit={handleSubmit}>
        <InputField label="Nome da empresa" value={companyName} onChange={(e) => setCompanyName(e.target.value)} error={errors.companyName} />
        <InputField label="NIF" value={nif} onChange={(e) => setNif(e.target.value.replace(/[^0-9]/g, ""))} error={errors.nif} />

        <MultiSelectDropdown
          label="Áreas de atividade"
          options={options.activitySectors}
          selectedOptions={activitySectors}
          onChange={setActivitySectors}
          placeholder={loadingOptions ? "A carregar..." : "Selecione os setores"}
          disabled={loadingOptions}
        />
        {errors.activitySectors && <p className="text-error text-sm">{errors.activitySectors}</p>}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="form-control w-full">
            <label className="label">
              <span className="label-text font-medium">País</span>
            </label>
            <select
              className="select select-bordered"
              value={country}
              onChange={(e) => setCountry(e.target.value)}
            >
              <option value="">Selecione o país</option>
              {options.countries.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
            {errors.country && <p className="text-error text-sm mt-1">{errors.country}</p>}
          </div>

          <InputField label="Website" value={website} onChange={(e) => setWebsite(e.target.value)} />
        </div>

        <InputField label="Morada" value={address} onChange={(e) => setAddress(e.target.value)} error={errors.address} />

        <div className="form-control flex flex-col">
          <label className="label justify-between items-center gap-2">
            <span className="label-text font-medium">Descrição</span>
            <span className={`text-xs ${descriptionLength >= DESCRIPTION_MAX_CHARS ? "text-error" : "text-base-content/60"}`}>
              {descriptionLength}/{DESCRIPTION_MAX_CHARS} caracteres
            </span>
          </label>
          <textarea
            className="textarea textarea-bordered w-full resize-none"
            rows={4}
            placeholder="Conte-nos sobre a empresa"
            value={description}
            onChange={handleDescriptionChange}
          />
        </div>

        <div className="mt-10 grid grid-cols-2 gap-4">
          <Button label="Voltar" variant="outline" type="button" onClick={() => goToStep(2)} />
          <Button label="Avançar" variant="primary" type="submit" />
        </div>
      </form>
    </section>
  );
}
