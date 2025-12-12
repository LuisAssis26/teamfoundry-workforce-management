import React, { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import InputField from "../../../components/ui/Input/InputField.jsx";
import PhoneInput from "../../../components/ui/Input/PhoneInput.jsx";
import Button from "../../../components/ui/Button/Button.jsx";
import { registerStep2 } from "../../../api/auth/auth.js";

const phoneRegex = /^\+\d{7,20}$/;
const nifRegex = /^\d{9}$/;

/**
 * Passo 2: coleta dados pessoais e envia para o backend.
 * Inclui upload opcional de CV (convertido em base64 antes do envio).
 */
export default function EmployeeRegisterStep2() {
    const { registerData, updateStepData, completeStep, goToStep } = useOutletContext();
    const emailFromStep1 = registerData.credentials?.email || "";

    const [formData, setFormData] = useState({
        firstName: registerData.personal?.firstName || "",
        lastName: registerData.personal?.lastName || "",
        birthDate: registerData.personal?.birthDate || "",
        phone: registerData.personal?.phone || "",
        nationality: registerData.personal?.nationality || "",
        nif: registerData.personal?.nif || "",
    });
    const [cvFile, setCvFile] = useState(registerData.personal?.cvFile || "");
    const [cvFileName, setCvFileName] = useState(registerData.personal?.cvFileName || "");

    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [serverError, setServerError] = useState("");

    // Mantém o formulário sincronizado com o que já foi preenchido caso o utilizador volte atrás.
    useEffect(() => {
        setFormData({
            firstName: registerData.personal?.firstName || "",
            lastName: registerData.personal?.lastName || "",
            birthDate: registerData.personal?.birthDate || "",
            phone: registerData.personal?.phone || "",
            nationality: registerData.personal?.nationality || "",
            nif: registerData.personal?.nif || "",
        });
        setCvFile(registerData.personal?.cvFile || "");
        setCvFileName(registerData.personal?.cvFileName || "");
    }, [
        registerData.personal?.birthDate,
        registerData.personal?.firstName,
        registerData.personal?.lastName,
        registerData.personal?.nationality,
        registerData.personal?.phone,
        registerData.personal?.nif,
        registerData.personal?.cvFile,
        registerData.personal?.cvFileName,
    ]);

    // Helper genérico para inputs controlados.
    const updateField = (field) => (event) => {
        setFormData((prev) => ({ ...prev, [field]: event.target.value }));
    };

    // Converte o CV para base64 e guarda o nome original para mostrar/reenviar.
    const handleCvChange = async (event) => {
        const file = event.target.files?.[0];
        if (!file) {
            setCvFile("");
            setCvFileName("");
            return;
        }
        const base64 = await fileToBase64(file);
        setCvFile(base64);
        setCvFileName(file.name);
    };

    const fileToBase64 = (file) =>
        new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });

    // Regras mínimas para garantir que os dados são coerentes antes de enviar ao backend.
    const validateFields = () => {
        const newErrors = {};

        if (!formData.firstName.trim()) {
            newErrors.firstName = "O primeiro nome é obrigatório.";
        }

        if (!formData.lastName.trim()) {
            newErrors.lastName = "O apelido é obrigatório.";
        }

        if (!formData.birthDate) {
            newErrors.birthDate = "A data de nascimento é obrigatória.";
        }

        if (!formData.phone.trim()) {
            newErrors.phone = "O número de telemóvel é obrigatório.";
        } else if (!phoneRegex.test(formData.phone.trim())) {
            newErrors.phone = "Insira um número de telemóvel válido.";
        }

        if (!formData.nationality.trim()) {
            newErrors.nationality = "A nacionalidade é obrigatória.";
        }

        if (!formData.nif.trim()) {
            newErrors.nif = "O NIF é obrigatório.";
        } else if (!nifRegex.test(formData.nif.trim())) {
            newErrors.nif = "NIF deve ter 9 dígitos.";
        }

        return newErrors;
    };

    // Persiste os dados pessoais e avança para o passo seguinte se o backend confirmar.
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
            await registerStep2({
                ...formData,
                email: emailFromStep1,
                nif: Number(formData.nif),
                cvFile,
                cvFileName,
            });

            updateStepData("personal", { ...formData, cvFile, cvFileName });
            completeStep(2, 3);
        } catch (error) {
            console.error("Erro ao guardar informações pessoais:", error);
            setServerError(error.message || "Ocorreu um erro inesperado.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <section className="flex h-full flex-col">
            <div>
                <p className="text-sm font-semibold text-primary uppercase tracking-wide">
                    Passo 2 de 4
                </p>
                <h1 className="mt-2 text-3xl font-bold text-accent">
                    Informações Pessoais
                </h1>
                <p className="mt-4 text-base text-base-content/70">
                    Complete os seus dados pessoais para personalizar a sua experiência na plataforma.
                </p>
            </div>

            <form className="mt-8 flex-1 space-y-6" onSubmit={handleSubmit} noValidate>
                {serverError && (
                    <div className="alert alert-error">
                        <span>{serverError}</span>
                    </div>
                )}

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                    <InputField
                        label="Primeiro nome"
                        placeholder="Insira o seu primeiro nome"
                        value={formData.firstName}
                        onChange={updateField("firstName")}
                        error={errors.firstName}
                    />
                    <InputField
                        label="Apelido"
                        placeholder="Insira o seu apelido"
                        value={formData.lastName}
                        onChange={updateField("lastName")}
                        error={errors.lastName}
                    />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                    <InputField
                        label="Data de nascimento"
                        type="date"
                        value={formData.birthDate}
                        onChange={updateField("birthDate")}
                        error={errors.birthDate}
                    />
                    <InputField
                    label="Nacionalidade"
                    placeholder="Ex.: Portuguesa"
                    value={formData.nationality}
                    onChange={updateField("nationality")}
                    error={errors.nationality}
                />
                    
                </div>
                <PhoneInput
                        label="Telemóvel"
                        value={formData.phone}
                        onChange={(val) => setFormData((prev) => ({ ...prev, phone: val }))}
                        error={errors.phone}
                />

                <InputField
                    label="NIF"
                    placeholder="Ex.: 123456789"
                    value={formData.nif}
                    onChange={updateField("nif")}
                    error={errors.nif}
                />

                <div>
                    <label className="label">
                        <span className="label-text font-medium">Curriculum (opcional)</span>
                    </label>
                    <input
                        type="file"
                        accept=".pdf,.doc,.docx"
                        className="file-input file-input-bordered w-full"
                        onChange={handleCvChange}
                    />
                    {cvFileName && (
                        <p className="mt-2 text-sm text-base-content/70">Ficheiro selecionado: {cvFileName}</p>
                    )}
                </div>

                <div className="mt-10 grid grid-cols-2 gap-4">
                    <Button
                        label="← Voltar"
                        variant="outline"
                        className="btn-outline border-base-300"
                        onClick={() => goToStep(1)}
                    />
                    <Button
                        label={loading ? "A guardar..." : "Avançar"}
                        type="submit"
                        variant="primary"
                        disabled={loading}
                    />
                </div>
            </form>
        </section>
    );
}
