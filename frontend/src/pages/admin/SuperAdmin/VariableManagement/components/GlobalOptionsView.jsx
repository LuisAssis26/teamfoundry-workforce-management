import Modal from "../../../../../components/ui/Modal/Modal.jsx";
import { useVariableManagement } from "../VariableManagementContext.jsx";

const manageTitle = (type) => {
  switch (type) {
    case "functions":
      return "Funções";
    case "competences":
      return "Competências";
    case "geoAreas":
      return "Áreas geográficas";
    case "activitySectors":
      return "Setores de atividade";
    default:
      return "Itens";
  }
};

const filteredManageItems = (type, options, search) => {
  const list = (options && options[type]) || [];
  const query = (search || "").toLowerCase().trim();
  if (!query) return list;
  return list.filter((item) => (item.name || "").toLowerCase().includes(query));
};

function OptionCard({ type, title, onManage }) {
  return (
    <div className="rounded-2xl border border-base-300 bg-base-100 p-4 space-y-3 shadow-sm w-full">
      <div className="flex items-center justify-between gap-3">
        <div>
          <h3 className="text-xl font-semibold">{title}</h3>
          <p className="text-base text-base-content/70">Gerir {title.toLowerCase()} disponíveis.</p>
        </div>
        <button type="button" className="btn btn-sm btn-primary" onClick={() => onManage(type)}>
          Gerir
        </button>
      </div>
    </div>
  );
}

export default function GlobalOptionsView() {
  const {
    combinedOptionsError,
    handleOptionsErrorClose,
    isGlobalOptionsLoading,
    manageModal,
    setManageModal,
    setOptionModal,
    globalOptions,
    openOptionModal,
    optionModal,
    closeOptionModal,
    handleOptionSubmit,
    optionLabels,
    deleteModal,
    setDeleteModal,
    openDeleteModal,
    closeDeleteModal,
    confirmDeleteOption,
  } = useVariableManagement();

  const openManage = (type) => setManageModal({ open: true, type, search: "" });

  return (
    <div className="space-y-10 pt-4">
      {combinedOptionsError && (
        <div className="alert alert-error shadow flex justify-between">
          <span>{combinedOptionsError}</span>
          <button type="button" className="btn btn-ghost btn-xs" onClick={handleOptionsErrorClose}>
            Fechar
          </button>
        </div>
      )}

      <div className="space-y-6">
        {isGlobalOptionsLoading ? (
          <div className="flex min-h-[200px] items-center justify-center">
            <span className="loading loading-spinner loading-lg text-primary" />
          </div>
        ) : (
          <div className="space-y-6">
            <section className="space-y-3 card bg-base-100 shadow-lg p-4">
              <div className="flex items-center gap-2">
                <h3 className="text-2xl font-semibold">Funcionário</h3>
                <span className="badge badge-ghost text-xs">Funções, Competências, Áreas geográficas</span>
              </div>
              <div className="space-y-3">
                <OptionCard type="functions" title="Funções" onManage={openManage} />
                <OptionCard type="competences" title="Competências" onManage={openManage} />
                <OptionCard type="geoAreas" title="Áreas geográficas" onManage={openManage} />
              </div>
            </section>
            <section className="space-y-3 card bg-base-100 shadow-lg p-4">
              <div className="flex items-center gap-2">
                <h3 className="text-2xl font-semibold">Empresa</h3>
                <span className="badge badge-ghost text-xs">Setores de atividade</span>
              </div>
              <div className="space-y-3">
                <OptionCard type="activitySectors" title="Setores de atividade" onManage={openManage} />
              </div>
            </section>
          </div>
        )}
      </div>

      {manageModal.open && (
        <Modal
          open
          title={`Gerir ${manageTitle(manageModal.type)}`}
          onClose={() => setManageModal({ open: false, type: null, search: "" })}
          actions={
            <div className="flex justify-center w-full mt-4">
              <button type="button" className="btn btn-primary" onClick={() => openOptionModal(manageModal.type)}>
                Adicionar
              </button>
            </div>
          }
        >
          <div className="space-y-5 min-h-[360px]">
            <div className="form-control w-full max-w-md gap-2">
              <span className="label-text font-semibold">Pesquisar</span>
              <input
                type="text"
                className="input input-bordered flex-1 min-w-[220px]"
                placeholder="Digite para filtrar"
                value={manageModal.search}
                onChange={(e) => setManageModal((prev) => ({ ...prev, search: e.target.value }))}
              />
            </div>
            <div className="max-h-[320px] overflow-auto space-y-2 pr-1">
              {filteredManageItems(manageModal.type, globalOptions, manageModal.search).map((item) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between rounded-xl border border-base-200 bg-base-100 px-3 py-2"
                >
                  <span className="font-medium">{item.name}</span>
                  <button
                    type="button"
                    className="btn btn-xs btn-outline btn-error btn-square transition-all duration-150 hover:scale-105"
                    title="Apagar"
                    onClick={() => openDeleteModal(manageModal.type, item)}
                  >
                    <i className="bi bi-x-lg text-error" />
                  </button>
                </div>
              ))}
              {filteredManageItems(manageModal.type, globalOptions, manageModal.search).length === 0 && (
                <p className="text-sm text-base-content/60 text-center py-6">Nenhum item encontrado.</p>
              )}
            </div>
            <div className="pt-1" />
          </div>
        </Modal>
      )}

      {optionModal.open && (
        <Modal
          open
          title={`Adicionar ${optionLabels[optionModal.type] || "item"}`}
          onClose={closeOptionModal}
          actions={
            <>
              <button type="button" className="btn btn-ghost" onClick={closeOptionModal}>
                Cancelar
              </button>
              <button type="submit" form="function-form" className="btn btn-primary" disabled={optionModal.saving}>
                {optionModal.saving ? (
                  <>
                    <span className="loading loading-spinner loading-sm" />
                    A guardar...
                  </>
                ) : (
                  "Adicionar"
                )}
              </button>
            </>
          }
        >
          <form id="function-form" className="space-y-4" onSubmit={handleOptionSubmit}>
            <label className="form-control w-full gap-2">
              <span className="label-text font-semibold">Nome da {optionLabels[optionModal.type] || "opção"}</span>
              <input
                id="option-name"
                type="text"
                className="input input-bordered w-full"
                value={optionModal.name}
                onChange={(e) => setOptionModal((prev) => ({ ...prev, name: e.target.value }))}
                required
              />
            </label>
            <p className="text-sm text-base-content/60">
              Insira o nome exatamente como deseja que apareça para os utilizadores.
            </p>
          </form>
        </Modal>
      )}

      {deleteModal.open && (
        <Modal
          open
          title="Confirmar apagamento"
          onClose={closeDeleteModal}
          actions={
            <>
              <button type="button" className="btn btn-ghost" onClick={closeDeleteModal} disabled={deleteModal.saving}>
                Cancelar
              </button>
              <button type="submit" form="delete-form" className="btn btn-outline btn-error" disabled={deleteModal.saving}>
                {deleteModal.saving ? (
                  <>
                    <span className="loading loading-spinner loading-sm" />
                    A apagar...
                  </>
                ) : (
                  "Apagar"
                )}
              </button>
            </>
          }
        >
          <form id="delete-form" className="space-y-4" onSubmit={confirmDeleteOption}>
            <p className="text-base-content/80">
              Para apagar <strong>{deleteModal.record?.name}</strong>, digite a password do super admin.
            </p>
            <label className="flex flex-col gap-2 items-start">
              <span className="label-text font-semibold">Password do super admin:</span>
              <input
                type="password"
                className="input input-bordered"
                value={deleteModal.password}
                onChange={(e) => setDeleteModal((prev) => ({ ...prev, password: e.target.value }))}
                required
              />
            </label>
            {deleteModal.error && <p className="text-sm text-error">{deleteModal.error}</p>}
          </form>
        </Modal>
      )}
    </div>
  );
}
