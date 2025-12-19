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
  invited = false,
  photoUrl = null,
  onSelect,
}) {
  const [open, setOpen] = useState(false);
  const hasExperiences = experiences.length > 0;
  const lastExperience = hasExperiences ? experiences[experiences.length - 1] : null;
  const displayedSkills = skills.slice(0, 3).join(", ");
  const hasMoreSkills = skills.length > 3;
  const preferredRoles = Array.isArray(role) ? role.join(", ") : role;

  const buttonLabel = accepted
    ? "Aceite"
    : invited
      ? "Enviado"
      : selected
        ? "Selecionado"
        : "Escolher";
  const buttonClasses = accepted
    ? "btn btn-success btn-sm text-success-content cursor-not-allowed"
    : invited
      ? "btn btn-neutral btn-sm text-neutral-content cursor-not-allowed"
      : selected
        ? "btn btn-accent btn-sm text-primary-content"
        : "btn btn-neutral btn-sm text-neutral-content";
  const borderColor = selected ? "border-success" : "border-base-300";

  return (
    <>
      <article
        className={`flex h-full flex-col items-start gap-4 rounded-2xl border ${borderColor} bg-base-100 p-5 shadow`}
      >
        <div className="flex items-center gap-3">
          <div>
            <p className="text-lg font-semibold text-base-content">{name}</p>
          </div>
        </div>

        <dl className="space-y-4 text-sm text-base-content w-full">
          <InfoLine label="Funções preferenciais" value={preferredRoles || "N/A"} />
          <InfoLine label="Preferência geográfica" value={city || "N/A"} />
          {skills.length > 0 && (
            <InfoLine
              label="Competências"
              value={hasMoreSkills ? `${displayedSkills}, ...` : displayedSkills}
            />
          )}
        </dl>

        <div className="space-y-2 text-sm w-full">
          <p className="font-semibold text-primary">Última experiência:</p>
          {lastExperience ? (
            <p className="text-base-content/70">{lastExperience}</p>
          ) : (
            <p className="text-base-content/70">Sem experiências anteriores.</p>
          )}
        </div>

        <div className="mt-auto flex w-full flex-wrap justify-center gap-3">
          <button
            type="button"
            className="flex-1 rounded-xl btn btn-primary btn-sm py-2 text-xs font-semibold shadow"
            onClick={() => setOpen(true)}
          >
            Ver mais
          </button>
          <button
            type="button"
            className={`flex-1 rounded-xl py-2 text-xs font-semibold shadow ${buttonClasses}`}
            onClick={!accepted && !invited ? onSelect : undefined}
            disabled={accepted || invited}
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
    <div className="flex flex-col items-start gap-1">
      <dt className="font-semibold text-primary">{label}:</dt>
      <dd className="text-base-content">{value}</dd>
    </div>
  );
}
