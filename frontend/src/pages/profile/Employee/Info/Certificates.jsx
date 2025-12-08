import { useEffect, useState } from "react";
import InfoLayout from "./InfoLayout.jsx";
import Button from "../../../../components/ui/Button/Button.jsx";
import InputField from "../../../../components/ui/Input/InputField.jsx";
import Modal from "../../../../components/ui/Modal/Modal.jsx";
import CertificateCard from "./components/CertificateCard.jsx";
import {
  createEmployeeCertification,
  listEmployeeCertifications,
  updateEmployeeCertification,
  deleteEmployeeCertification,
} from "../../../../api/profile/employeeCertifications.js";
import { useEmployeeProfile } from "../EmployeeProfileContext.jsx";
import { formatName } from "../utils/profileUtils.js";
import DropZone from "../../../../components/ui/Upload/DropZone.jsx";

const initialForm = {
  name: "",
  institution: "",
  location: "",
  completionDate: "",
  description: "",
  file: null,
};

const ALLOWED_CERT_TYPES = [
  "application/pdf",
  "image/png",
  "image/jpeg",
  "image/jpg",
  "image/webp",
];
const MAX_CERT_MB = 5;

export default function Certificates() {
  const [open, setOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(initialForm);
  const [errors, setErrors] = useState({});
  const [deleting, setDeleting] = useState(false);
  const [educations, setEducations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const { profile, refreshProfile, educationData, setEducationData } = useEmployeeProfile();
  const [displayName, setDisplayName] = useState("");

  useEffect(() => {
    let isMounted = true;
    async function loadEducation() {
      try {
        const data = educationData || (await listEmployeeCertifications());
        if (!isMounted) return;
        setEducations(Array.isArray(data) ? data : []);
        if (!educationData) setEducationData(data);
      } catch (error) {
        if (isMounted) {
          setErrorMessage(error.message || "Nuo foi poss??vel carregar as forma????es.");
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    // Usa o nome vindo do contexto ou, se não existir, faz refresh.
    async function loadProfileName() {
      try {
        const profileSource = profile || (await refreshProfile());
        if (!isMounted) return;
        setDisplayName(formatName(profileSource?.firstName, profileSource?.lastName));
      } catch {
      }
    }

    loadEducation();
    loadProfileName();
    return () => {
      isMounted = false;
    };
  }, []);

  const handleChange = (event) => {
    // Atualiza campo simples e limpa erro daquele campo.
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: "" }));
  };

  const validateForm = () => {
    // Validação mínima antes do upload.
    const newErrors = {};
    if (!form.name.trim()) newErrors.name = "O nome da formação é obrigatório.";
    if (!form.institution.trim()) newErrors.institution = "A instituição é obrigatória.";
    if (!form.completionDate) newErrors.completionDate = "Indique a data de conclusão.";
    if (!form.file) newErrors.file = "É necessário anexar o certificado.";
    return newErrors;
  };

  const handleSubmit = async () => {
    // Envia a formação com certificado; em sucesso fecha modal e atualiza lista local.
    const validationErrors = validateForm();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setSaving(true);
    setErrors({});
    setErrorMessage("");

    try {
      const payload = await buildPayload(form);
      if (editingId) {
        const updated = await updateEmployeeCertification(editingId, payload);
        setEducations((prev) => prev.map((e) => (e.id === editingId ? updated : e)));
        setSuccessMessage("Certificação atualizada com sucesso.");
      } else {
        const created = await createEmployeeCertification(payload);
        setEducations((prev) => [created, ...prev]);
        setSuccessMessage("Certificação adicionada com sucesso.");
      }
      closeModal();
    } catch (error) {
      setErrorMessage(error.message || "Não foi possível guardar a formação.");
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (education) => {
    setEditingId(education.id);
    setForm({
      name: education.name || "",
      institution: education.institution || "",
      location: education.location || "",
      completionDate: education.completionDate || "",
      description: education.description || "",
      file: null,
    });
    setOpen(true);
  };

  const handleDelete = async () => {
    if (!editingId) return;
    setDeleting(true);
    setErrorMessage("");
    try {
      await deleteEmployeeCertification(editingId);
      setEducations((prev) => prev.filter((e) => e.id !== editingId));
      setSuccessMessage("Certificação removida com sucesso.");
      closeModal();
    } catch (err) {
      setErrorMessage(err.message || "Não foi possível remover a certificação.");
    } finally {
      setDeleting(false);
    }
  };

  const closeModal = () => {
    setOpen(false);
    setEditingId(null);
    setForm(initialForm);
    setErrors({});
  };

  const openModal = () => {
    setSuccessMessage("");
    setErrorMessage("");
    setForm(initialForm);
    setEditingId(null);
    setErrors({});
    setOpen(true);
  };

  return (
    <InfoLayout name={displayName}>
      <div className="mt-6 rounded-xl border border-base-300 bg-base-100 shadow min-h-[55vh]">
        <div className="p-4 md:p-6 space-y-4">
          {errorMessage && (
            <div className="alert alert-error text-sm" role="alert">
              {errorMessage}
            </div>
          )}
          {successMessage && (
            <div className="alert alert-success text-sm" role="status">
              {successMessage}
            </div>
          )}

          <div className="flex justify-end">
            <Button label="Adicionar Certificação" onClick={openModal} />
          </div>

          {loading ? (
            <SkeletonList />
          ) : educations.length === 0 ? (
            <EmptyState />
          ) : (
            <div className="space-y-4 max-w-3xl mx-auto">
              {educations.map((education) => (
                <CertificateCard key={education.id} education={education} onEdit={handleEdit} />
              ))}
            </div>
          )}
        </div>
      </div>

      <Modal
        open={open}
        onClose={closeModal}
        title="Nova Certificação"
        className="max-w-3xl max-h-[90vh] overflow-y-auto"
        actions={
          <>
            {editingId && (
              <button
                type="button"
                className="btn btn-error btn-outline"
                onClick={handleDelete}
                disabled={saving || deleting}
              >
                {deleting ? "A eliminar..." : "Eliminar"}
              </button>
            )}
            <button type="button" className="btn btn-neutral" onClick={closeModal} disabled={saving || deleting}>
              Cancelar
            </button>
            <button type="button" className="btn btn-primary" onClick={handleSubmit} disabled={saving || deleting}>
              {saving ? "A guardar..." : editingId ? "Atualizar" : "Adicionar"}
            </button>
          </>
        }
      >
        <div className="space-y-4">
          <InputField
            label="Nome da Certificação"
            name="name"
            placeholder="Ex.: Ensino Secundário"
            value={form.name}
            onChange={handleChange}
            error={errors.name}
          />
          <InputField
            label="Instituição"
            name="institution"
            placeholder="Ex.: Escola A"
            value={form.institution}
            onChange={handleChange}
            error={errors.institution}
          />
          <InputField
            label="Local (opcional)"
            name="location"
            placeholder="Cidade ou país"
            value={form.location}
            onChange={handleChange}
          />
          <InputField
            label="Data de Conclusão"
            name="completionDate"
            type="date"
            value={form.completionDate}
            max={new Date().toISOString().split("T")[0]}
            onChange={handleChange}
            error={errors.completionDate}
          />
          <InputField
            label="Descrição (opcional)"
            name="description"
            as="textarea"
            rows={3}
            placeholder="Notas adicionais sobre a formação"
            value={form.description}
            onChange={handleChange}
          />

          <div>
            <DropZone
              label="Certificado"
              onSelect={(file) => {
                setForm((prev) => ({ ...prev, file }));
                setErrors((prev) => ({ ...prev, file: "" }));
              }}
              onRemove={() => {
                setForm((prev) => ({ ...prev, file: null }));
              }}
              hasFile={Boolean(form.file)}
              fileName={form.file?.name}
              disabled={saving || deleting}
              allowedTypes={ALLOWED_CERT_TYPES}
              maxSizeMB={MAX_CERT_MB}
              onError={(msg) => setErrors((prev) => ({ ...prev, file: msg }))}
            />
            {errors.file && <p className="mt-2 text-sm text-error">{errors.file}</p>}
          </div>
        </div>
      </Modal>
    </InfoLayout>
  );
}

function SkeletonList() {
  return (
    <div className="animate-pulse space-y-3">
      <div className="h-20 bg-base-200 rounded-xl" />
      <div className="h-20 bg-base-200 rounded-xl" />
      <div className="h-20 bg-base-200 rounded-xl" />
    </div>
  );
}

function EmptyState() {
  return (
    <div className="text-center text-base-content/70 py-12 border border-dashed border-base-300 rounded-xl">
      Ainda não adicionou certificações. Clique em “Adicionar Certificação” para registar a primeira.
    </div>
  );
}

async function buildPayload(form) {
  const certificateFile = form.file ? await fileToBase64(form.file) : null;
  const fileName =
    form.file?.name && form.file.name.trim()
      ? form.file.name
      : (form.name ? `${form.name.trim().replace(/\s+/g, "_")}` : "certificado") + ".pdf";
  return {
    name: form.name.trim(),
    institution: form.institution.trim(),
    location: form.location?.trim() || null,
    completionDate: form.completionDate,
    description: form.description?.trim() || null,
    certificateFile,
    certificateFileName: fileName,
  };
}

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}


