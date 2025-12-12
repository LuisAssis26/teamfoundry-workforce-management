import Modal from "../../../../../../../components/ui/Modal/Modal.jsx";
import Button from "../../../../../../../components/ui/Button/Button.jsx";

export default function AdminDisableModal({
  open,
  admin,
  password,
  onPasswordChange,
  onConfirm,
  onCancel,
  loading = false,
  error,
}) {
  return (
    <Modal
      open={open}
      title="Tem certeza?"
      onClose={onCancel}
      actions={
        <div className="flex w-full justify-end gap-3">
          <Button
            label="Cancelar"
            variant="outline"
            className="max-w-32 btn btn-secondary"
            onClick={onCancel}
            disabled={loading}
          />
          <Button
            label={loading ? "Desativando..." : "Sim"}
            variant="danger"
            className="max-w-32 btn-error text-white"
            onClick={onConfirm}
            disabled={loading}
          />
        </div>
      }
    >
      {error && (
        <div className="alert alert-error mb-4">
          <span>{error}</span>
        </div>
      )}

      <div className="space-y-4">
        <p className="text-base-content">
          Ao confirmar, o administrador "{admin?.username}" será desativado. Deseja continuar?
        </p>

        <label className="form-control w-full">
          <span className="label-text font-medium">Password Super Admin</span>
          <input
            type="password"
            value={password}
            onChange={(event) => onPasswordChange(event.target.value)}
            className="input input-bordered w-full"
            disabled={loading}
          />
        </label>
      </div>
    </Modal>
  );
}
