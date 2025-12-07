import { useEffect, useState } from "react";
import DropZone from "../../../../components/ui/Upload/DropZone.jsx";
import {
  deleteEmployeeCurriculum,
  fetchEmployeeCurriculum,
  uploadEmployeeCurriculum,
  uploadIdentificationDocument,
  deleteIdentificationDocument,
} from "../../../../api/profile/employeeProfile.js";

const DEFAULT_NAMES = {
  cv: "curriculo.pdf",
  idFront: "documento-frente.pdf",
  idBack: "documento-verso.pdf",
};

const ALLOWED_DOC_TYPES = [
  "application/pdf",
  "image/png",
  "image/jpeg",
  "image/jpg",
  "image/webp",
];
const MAX_DOC_MB = 5;

const DOC_ITEMS = [
  { key: "idFront", label: "Documento de Identificação (frente)", type: "IDENTIFICATION_FRONT" },
  { key: "idBack", label: "Documento de Identificação (verso)", type: "IDENTIFICATION_BACK" },
  { key: "cv", label: "Curriculo" },
];

export default function Documentos() {
  const [docs, setDocs] = useState({
    idFront: { url: null, fileName: DEFAULT_NAMES.idFront },
    idBack: { url: null, fileName: DEFAULT_NAMES.idBack },
    cv: { url: null, fileName: DEFAULT_NAMES.cv },
  });
  const [loading, setLoading] = useState(true);
  const [processingKey, setProcessingKey] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = () => {
    setLoading(true);
    fetchEmployeeCurriculum()
      .then((data) => {
        applyProfileData(data);
      })
      .catch((err) => {
        setError(err.message || "Nao foi possivel carregar os documentos.");
      })
      .finally(() => setLoading(false));
  };

  const applyProfileData = (data, overrides = {}) => {
    setDocs((prev) => ({
      idFront: {
        url: data?.identificationFrontUrl ?? null,
        fileName:
          overrides.idFront ??
          (data?.identificationFrontUrl ? prev.idFront.fileName : DEFAULT_NAMES.idFront),
      },
      idBack: {
        url: data?.identificationBackUrl ?? null,
        fileName:
          overrides.idBack ??
          (data?.identificationBackUrl ? prev.idBack.fileName : DEFAULT_NAMES.idBack),
      },
      cv: {
        url: data?.curriculumUrl ?? null,
        fileName: overrides.cv ?? (data?.curriculumUrl ? prev.cv.fileName : DEFAULT_NAMES.cv),
      },
    }));
  };

  const handleCvSelect = async (file) => {
    if (!file) return;
    setError("");
    setProcessingKey("cv");
    try {
      const base64 = await fileToBase64(file);
      const data = await uploadEmployeeCurriculum({ file: base64, fileName: file.name });
      applyProfileData(data, { cv: file.name });
    } catch (err) {
      setError(err.message || "Falha ao carregar o curriculo.");
    } finally {
      setProcessingKey(null);
    }
  };

  const handleCvDelete = async () => {
    if (!docs.cv.url) return;
    setError("");
    setProcessingKey("cv");
    try {
      await deleteEmployeeCurriculum();
      setDocs((prev) => ({
        ...prev,
        cv: { url: null, fileName: DEFAULT_NAMES.cv },
      }));
    } catch (err) {
      setError(err.message || "Falha ao eliminar o curriculo.");
    } finally {
      setProcessingKey(null);
    }
  };

  const handleIdentificationSelect = (key, type) => async (file) => {
    if (!file) return;
    setError("");
    setProcessingKey(key);
    try {
      const base64 = await fileToBase64(file);
      const data = await uploadIdentificationDocument({
        file: base64,
        fileName: file.name,
        type,
      });
      const overrideKey = key === "idFront" ? { idFront: file.name } : { idBack: file.name };
      applyProfileData(data, overrideKey);
    } catch (err) {
      setError(err.message || "Falha ao carregar o documento.");
    } finally {
      setProcessingKey(null);
    }
  };

  const handleIdentificationDelete = (key, type) => async () => {
    if (!docs[key].url) return;
    setError("");
    setProcessingKey(key);
    try {
      await deleteIdentificationDocument(type);
      setDocs((prev) => ({
        ...prev,
        [key]: { url: null, fileName: key === "idFront" ? DEFAULT_NAMES.idFront : DEFAULT_NAMES.idBack },
      }));
    } catch (err) {
      setError(err.message || "Falha ao eliminar o documento.");
    } finally {
      setProcessingKey(null);
    }
  };

  const fileToBase64 = (file) =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });

  return (
    <section className="w-full">
      <div className="flex items-center gap-3 mb-6">
        <h2 className="text-3xl font-semibold">Upload de Documentos</h2>
      </div>

      <div className="rounded-xl border border-base-300 bg-base-100 shadow p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-10 gap-y-6">
          {DOC_ITEMS.map((item) => {
            const doc = docs[item.key];
            const isCv = item.key === "cv";
            const disabled = loading || processingKey === item.key;
            const hasFile = !!doc?.url;
            return (
              <DropZone
                key={item.key}
                label={item.label}
                onSelect={
                  isCv
                    ? handleCvSelect
                    : handleIdentificationSelect(item.key, item.type)
                }
                hasFile={hasFile}
                fileName={doc?.fileName}
                onRemove={
                  hasFile
                    ? isCv
                      ? handleCvDelete
                      : handleIdentificationDelete(item.key, item.type)
                    : undefined
                }
                disabled={disabled}
                allowedTypes={ALLOWED_DOC_TYPES}
                maxSizeMB={MAX_DOC_MB}
                onError={setError}
              />
            );
          })}
        </div>
        {error && <p className="text-error text-sm mt-4">{error}</p>}
      </div>
    </section>
  );
}
