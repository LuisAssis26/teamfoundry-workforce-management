import { useState } from "react";
import EmployeeProfileModal from "./EmployeeProfileModal.jsx";

export default function EmployeeCard({
  id,
  name,
  role,
  city,
  skills = [],
  experiences = [],
  selected = false,
  accepted = false,
  onSelect,
}) {
  const [open, setOpen] = useState(false);
  const hasExperiences = experiences.length > 0;
  const lastExperience = hasExperiences ? experiences[experiences.length - 1] : null;
  const displayedSkills = skills.slice(0, 3).join(", ");
  const hasMoreSkills = skills.length > 3;
  const preferredRoles = Array.isArray(role) ? role.join(", ") : role;

  const buttonLabel = accepted ? "Aceite" : selected ? "Selecionado" : "Escolher";
  const buttonClasses = accepted
    ? "bg-[#1CA74F] cursor-not-allowed"
    : selected
    ? "bg-[#60678E]"
    : "bg-[#1F2959]";

  return (
    <>
      <article
        className={`flex h-full flex-col items-center gap-4 rounded-2xl border ${
          accepted ? "border-[#1CA74F]" : selected ? "border-[#1CA74F]" : "border-[#31A15F]"
        } bg-[#F5F5F5] p-5 shadow text-center`}
      >
        <div className="flex items-center justify-center gap-3">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#1F2959] text-white shadow-inner text-2xl">
            <i className="bi bi-person-fill" aria-hidden="true"></i>
          </div>
          <div>
            <p className="text-lg font-semibold text-[#111827]">{name}</p>
          </div>
        </div>

        <dl className="space-y-4 text-sm text-[#111827] w-full">
          <InfoLine label="Funcoes preferidas" value={preferredRoles || "N/A"} />
          <InfoLine label="Preferencia geografica" value={city || "N/A"} />
          {skills.length > 0 && (
            <InfoLine
              label="Competencias"
              value={hasMoreSkills ? `${displayedSkills}, ...` : displayedSkills}
            />
          )}
        </dl>

        <div className="space-y-2 text-sm w-full">
          <p className="font-semibold text-[#1F2959]">Ultima experiencia:</p>
          {lastExperience ? (
            <p className="text-[#8A93C2]">{lastExperience}</p>
          ) : (
            <p className="text-[#8A93C2]">Sem experiencias anteriores.</p>
          )}
        </div>

        <div className="mt-auto flex w-full flex-wrap justify-center gap-3">
          <button
            type="button"
            className="flex-1 rounded-xl bg-[#1F2959] py-2 text-sm font-semibold text-white shadow"
            onClick={() => setOpen(true)}
          >
            Ver mais
          </button>
          <button
            type="button"
            className={`flex-1 rounded-xl py-2 text-sm font-semibold text-white shadow ${buttonClasses}`}
            onClick={!accepted ? onSelect : undefined}
            disabled={accepted}
          >
            {buttonLabel}
          </button>
        </div>
      </article>

      {typeof EmployeeProfileModal !== "undefined" && (
        <EmployeeProfileModal
          open={open}
          onClose={() => setOpen(false)}
          employeeId={id}
          fallback={{
            name,
            role,
            city,
            skills,
            experiences: lastExperience ? [lastExperience] : [],
          }}
        />
      )}
    </>
  );
}

function InfoLine({ label, value }) {
  return (
    <div className="flex flex-col items-center gap-1">
      <dt className="font-semibold text-[#1F2959]">{label}:</dt>
      <dd className="text-[#111827]">{value}</dd>
    </div>
  );
}
