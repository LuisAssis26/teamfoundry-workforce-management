import EmployeeCard from "./EmployeeCard.jsx";

export default function CandidatesPanel({
  employees,
  isLoading,
  selectedIds,
  onToggle,
  onSendInvites,
  isInviting,
  disabled,
  page,
  totalPages,
  onPageChange,
}) {
  const canPrev = page > 1;
  const canNext = page < totalPages;

  return (
    <section className="flex-1 rounded-2xl border border-base-200 bg-base-100 p-4 shadow-inner space-y-4">
      <div className="flex items-center justify-between">
        <span className="text-sm text-base-content/70">Selecionados: {selectedIds.length}</span>
        <button
          type="button"
          className="btn btn-primary btn-sm"
          disabled={disabled || isLoading || isInviting || selectedIds.length === 0}
          onClick={onSendInvites}
        >
          {isInviting ? "Enviando..." : disabled ? "Concluída" : "Enviar propostas"}
        </button>
      </div>

      {isLoading ? (
        <div className="py-8 text-center text-base-content/70">Carregando candidatos...</div>
      ) : employees.length === 0 ? (
        <div className="py-8 text-center text-base-content/70">Nenhum candidato encontrado.</div>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {employees.map((employee) => (
              <EmployeeCard
                key={employee.id}
                {...employee}
                onSelect={() => onToggle(employee.id, employee.accepted, employee.invited)}
              />
            ))}
          </div>
          <div className="flex items-center justify-center gap-3 pt-2">
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              disabled={!canPrev}
              onClick={() => onPageChange(page - 1)}
            >
              Anterior
            </button>
            <span className="text-sm text-base-content/80">
              Página {page} de {totalPages}
            </span>
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              disabled={!canNext}
              onClick={() => onPageChange(page + 1)}
            >
              Próxima
            </button>
          </div>
        </>
      )}
    </section>
  );
}
