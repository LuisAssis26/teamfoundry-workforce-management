import Modal from "../../../../../../../components/ui/Modal/Modal.jsx";
import Button from "../../../../../../../components/ui/Button/Button.jsx";

export default function CompanyApprovalModal({
  open,
  company,
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
      title="Aprovar credencial"
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
            label={loading ? "Aprovando..." : "Confirmar"}
            variant="primary"
            className="max-w-32"
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
          Ao confirmar, a credencial da empresa "{company?.companyName}" será aprovada.
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
