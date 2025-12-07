import { useEffect, useState } from "react";
import PropTypes from "prop-types";
import Modal from "../../../../components/ui/Modal/Modal.jsx";
import Button from "../../../../components/ui/Button/Button.jsx";
import { fetchAdminEmployeeProfile } from "../../../../api/admin/adminEmployees.js";

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
    return () => { canceled = true; };
  }, [open, employeeId, fallback]);

  const data = profile || fallback || {};

  return (
      <Modal
          open={open}
          onClose={onClose}
          title=""
          className="w-full max-w-4xl max-h-[80vh] overflow-y-auto bg-[#F0F0F0] mx-auto"
          actions={
            <>
              <Button label="Fechar" variant="ghost" fullWidth={false} onClick={onClose} />
              <Button label="Escolher" variant="primary" fullWidth={false} />
            </>
          }
      >
        <div className="space-y-6 px-2 lg:px-0">
          <div className="flex items-center justify-center px-4">
            <h2 className="text-center text-3xl font-bold text-[#1F2959] lg:text-4xl">
              Perfil de Funcionário
            </h2>
          </div>

          {error && (
              <div className="alert alert-error shadow">
                <span>{error}</span>
              </div>
          )}

          {loading ? (
              <div className="p-6 text-center text-base-content/70">Carregando perfil...</div>
          ) : (
              <>
                <div className="flex flex-col gap-4 lg:flex-row lg:items-center">
                  <div className="flex h-44 w-44 shrink-0 items-center justify-center rounded-full bg-[#1F2959] text-white shadow-inner">
                    <i className="bi bi-person-fill text-6xl" aria-hidden="true" />
                  </div>
                  <div className="w-full">
                    <SectionCard title="Informações Pessoais">
                      <div className="grid gap-3 md:grid-cols-2">
                        <InfoRow label="Nome" value={data.firstName || data.name || "N/A"} />
                        <InfoRow label="Apelido" value={data.lastName || "N/A"} />
                        <InfoRow label="Data de Nascimento" value={data.birthDate || "N/A"} />
                        <InfoRow label="Nº de Telemóvel" value={data.phone || "N/A"} />
                        <InfoRow label="Nacionalidade" value={data.nationality || "N/A"} />
                        <InfoRow label="NIF" value={data.nif || "N/A"} />
                      </div>
                    </SectionCard>
                  </div>
                </div>

                <SectionCard title="Preferências de Trabalho">
                  <div className="flex flex-col gap-3 md:flex-row md:gap-10">
                    <div className="space-y-2">
                      <InfoRow label="Função" value={data.preferredRole || data.role || "N/A"} />
                      <InfoRow label="Área geográfica" value={data.areas?.join(", ") || data.preference || "N/A"} />
                    </div>
                    <div className="space-y-2">
                      <InfoRow label="Competências" value={(data.skills || []).join(", ") || "N/A"} accent />
                    </div>
                  </div>
                </SectionCard>

                <SectionCard title="Trabalhos Anteriores">
                  <div className="space-y-3">
                    {data.experiences?.length ? (
                        data.experiences.map((exp, idx) => (
                            <WorkHistoryCard key={idx} experience={exp} />
                        ))
                    ) : (
                        <p className="text-sm text-base-content/70 px-2">Sem registros.</p>
                    )}
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
      <section className="rounded-xl border border-[#111827] bg-white shadow-md">
        <header className="border-b border-[#E5E7EB] px-4 py-3">
          <h3 className="text-xl font-semibold text-[#1F2959] lg:text-2xl">{title}</h3>
        </header>
        <div className="p-4 lg:p-6">{children}</div>
      </section>
  );
}

function InfoRow({ label, value, accent = false }) {
  return (
      <p className="text-base leading-relaxed text-[#111827]">
        <span className="font-semibold text-[#1F2959]">{label}: </span>
        <span className={accent ? "text-[#1F2959]" : "text-[#111827]"}>{value}</span>
      </p>
  );
}

function WorkHistoryCard({ experience }) {
  const value = typeof experience === "string" ? experience : "";
  return (
      <div className="flex flex-col gap-3 rounded-lg border border-[#111827] bg-[#F5F5F5] p-4 shadow-sm md:flex-row md:items-center md:justify-between">
        <p className="text-sm text-[#111827]">{value || "Experiência não informada."}</p>
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
