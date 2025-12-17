import { useEffect, useState } from "react";
import PropTypes from "prop-types";
import Modal from "../../../../../components/ui/Modal/Modal.jsx";
import Button from "../../../../../components/ui/Button/Button.jsx";
import { fetchAdminEmployeeProfile } from "../../../../../api/admin/adminEmployees.js";

export default function EmployeeProfileModal({ open, onClose, employeeId, fallback }) {
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let canceled = false;
    async function load() {
      if (!open || !employeeId) return;
      setLoading(true);
      setError("");
      try {
        const data = await fetchAdminEmployeeProfile(employeeId);
        if (!canceled) setProfile(data);
      } catch (err) {
        if (!canceled) setError(err.message || "Erro ao carregar perfil.");
        if (!canceled && fallback) setProfile(fallback);
      } finally {
        if (!canceled) setLoading(false);
      }
    }
    load();
    return () => {
      canceled = true;
    };
  }, [open, employeeId, fallback]);

  const data = profile || fallback || {};
  const documents = {
    curriculum: { url: data.curriculumUrl, name: data.curriculumFileName || "Curriculo" },
    idFront: { url: data.identificationFrontUrl, name: data.identificationFrontFileName || "Identifica??o (frente)" },
    idBack: { url: data.identificationBackUrl, name: data.identificationBackFileName || "Identifica??o (verso)" },
  };
  const certifications = data.certifications || [];

  return (
    <Modal
      open={open}
      onClose={onClose}
      title=""
      className="w-full min-w-6xl max-h-[80vh] overflow-y-auto bg-base-200 mx-auto"
      actions={
        <>
          <Button label="Fechar" variant="ghost" fullWidth={false} onClick={onClose} />
          <Button label="Escolher" variant="primary" fullWidth={false} />
        </>
      }
    >
      <div className="space-y-6 px-2 lg:px-0">
        <div className="flex items-center justify-center px-4">
          <h2 className="text-center text-3xl font-bold text-primary lg:text-4xl">Perfil de Funcionário</h2>
        </div>

        {error && (
          <div className="alert alert-error shadow">
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className="p-6 text-center text-base-content/70">A carregar perfil...</div>
        ) : (
          <>
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center">
              <div className="flex h-44 w-44 shrink-0 items-center justify-center rounded-full bg-primary text-primary-content shadow-inner overflow-hidden">
                {data.profilePictureUrl ? (
                  <img src={data.profilePictureUrl} alt="Foto de perfil" className="h-full w-full object-cover" />
                ) : (
                  <i className="bi bi-person-fill text-6xl" aria-hidden="true" />
                )}
              </div>
              <div className="w-full">
                <SectionCard title="Informações Pessoais">
                  <div className="grid gap-3 md:grid-cols-2">
                    <InfoRow label="Nome" value={data.firstName || data.name || "N/A"} />
                    <InfoRow label="Apelido" value={data.lastName || "N/A"} />
                    <InfoRow label="Data de Nascimento" value={data.birthDate || "N/A"} />
                    <InfoRow label="N.º de Telemóvel" value={data.phone || "N/A"} />
                    <InfoRow label="Nacionalidade" value={data.nationality || "N/A"} />
                    <InfoRow label="Email" value={data.email || "N/A"} />
                  </div>
                </SectionCard>
              </div>
            </div>

            <div className="grid w-full gap-4 md:grid-cols-2">
              <SectionCard title="Preferências de Trabalho">
                <div className="flex flex-col gap-3 md:flex-row md:gap-10">
                  <div className="space-y-2">
                    <InfoRow label="Função" value={data.preferredRole || data.role || "N/A"} />
                    <InfoRow label="Área geográfica" value={data.areas?.join(", ") || data.preference || "N/A"} />
                  </div>
                </div>
              </SectionCard>

              <SectionCard title="Competências">
                <div className="space-y-2">
                  <InfoRow label="Competências" value={(data.skills || []).join(", ") || "N/A"} accent />
                </div>
              </SectionCard>
            </div>

            <SectionCard title="Trabalhos Anteriores">
              <div className="space-y-3">
                {data.experiences?.length ? (
                  data.experiences.map((exp, idx) => <WorkHistoryCard key={idx} experience={exp} />)
                ) : (
                  <p className="px-2 text-sm text-base-content/70">Sem registros.</p>
                )}
              </div>
            </SectionCard>

            <SectionCard title="Documentos">
              <div className="grid gap-3 md:grid-cols-2">
                <DocRow label="Curriculo" doc={documents.curriculum} />
                <DocRow label="Frente" doc={documents.idFront} />
                <DocRow label="Verso" doc={documents.idBack} />
              </div>
            </SectionCard>

            <SectionCard title="Certificações">
              <div className="space-y-3">
                {certifications.length === 0 && (
                  <p className="text-sm text-base-content/70">Sem certificações registadas.</p>
                )}
                {certifications.map((cert) => (
                  <div
                    key={`${cert.id}-${cert.name}`}
                    className="rounded-lg border border-base-300 bg-base-100 p-4 shadow-sm"
                  >
                    <p className="text-base font-semibold text-primary">{cert.name || "Certifica??o"}</p>
                    <p className="text-sm text-base-content/80">{cert.institution || "Institui??o n?o informada"}</p>
                    <p className="text-sm text-base-content/80">
                      {cert.completionDate ? `Conclu?do em ${cert.completionDate}` : "Data n?o informada"}
                    </p>
                    {cert.certificateUrl ? (
                      <a
                        href={cert.certificateUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-ghost btn-sm mt-2"
                      >
                        <i className="bi bi-download mr-2" aria-hidden="true" />
                        Baixar certificado
                      </a>
                    ) : null}
                    {cert.description && (
                      <p className="mt-2 text-sm text-base-content/90">{cert.description}</p>
                    )}
                  </div>
                ))}
              </div>
            </SectionCard>
          </>
        )}
      </div>
    </Modal>
  );
}

function SectionCard({ title, children }) {
  return (
    <section className="rounded-xl border border-base-300 bg-base-100 shadow-md">
      <header className="border-b border-base-200 px-4 py-3">
        <h3 className="text-xl font-semibold text-primary lg:text-2xl">{title}</h3>
      </header>
      <div className="p-4 lg:p-6">{children}</div>
    </section>
  );
}

function InfoRow({ label, value, accent = false }) {
  return (
    <p className="text-base leading-relaxed text-base-content">
      <span className="font-semibold text-primary">{label}: </span>
      <span className={accent ? "text-primary" : "text-base-content"}>{value}</span>
    </p>
  );
}

function WorkHistoryCard({ experience }) {
  const value = typeof experience === "string" ? experience : "";
  return (
    <div className="flex flex-col gap-3 rounded-lg border border-base-300 bg-base-100 p-4 shadow-sm md:flex-row md:items-center md:justify-between">
      <p className="text-sm text-base-content">{value || "Experi?ncia n?o informada."}</p>
    </div>
  );
}

function DocRow({ label, doc }) {
  const hasFile = !!doc?.url;
  const fileName = doc?.name || label;
  return (
    <div className="flex items-center justify-between gap-3 rounded-lg border border-base-200 bg-base-100 px-4 py-2">
      <div>
        <p className="text-sm font-semibold text-primary">{label}</p>
        <p className="text-xs text-base-content/70">{hasFile ? fileName : "N/A"}</p>
      </div>
      {hasFile ? (
        <a href={doc.url} target="_blank" rel="noreferrer">
          <i className="bi bi-download mr-2" aria-hidden="true" />
          Ver
        </a>
      ) : (
        <span className="badge badge-ghost text-xs">Não enviado</span>
      )}
    </div>
  );
}

EmployeeProfileModal.propTypes = {
  open: PropTypes.bool,
  onClose: PropTypes.func,
  employeeId: PropTypes.number,
  fallback: PropTypes.object,
};

SectionCard.propTypes = { title: PropTypes.string.isRequired, children: PropTypes.node };
InfoRow.propTypes = { label: PropTypes.string.isRequired, value: PropTypes.any, accent: PropTypes.bool };
WorkHistoryCard.propTypes = { experience: PropTypes.any };
DocRow.propTypes = { label: PropTypes.string.isRequired, doc: PropTypes.object };
