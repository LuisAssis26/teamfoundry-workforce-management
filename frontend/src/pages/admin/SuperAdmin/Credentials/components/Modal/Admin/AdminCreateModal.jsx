import Modal from "../../../../../../../components/ui/Modal/Modal.jsx";
import Button from "../../../../../../../components/ui/Button/Button.jsx";

const ROLE_OPTIONS = [
  { value: "admin", label: "Admin" },
  { value: "super-admin", label: "Super Admin" },
];

export default function AdminCreateModal({
  open,
  form,
  error,
  loading = false,
  onClose,
  onSave,
  onFieldChange,
}) {
  return (
    <Modal
      open={open}
      title="Criar administrador"
      onClose={onClose}
      actions={
        <div className="flex w-full justify-end gap-3">
          <Button
            label="Cancelar"
            variant="outline"
            className="max-w-32 btn btn-secondary"
            onClick={onClose}
            disabled={loading}
          />
          <Button
            label={loading ? "Criando..." : "Criar"}
            variant="primary"
            className="max-w-32"
            onClick={onSave}
            disabled={loading}
          />
        </div>
      }
    >
      <div className="space-y-4">
        {error && (
          <div className="alert alert-error shadow">
            <span>{error}</span>
          </div>
        )}

        <label className="form-control w-full">
          <span className="label-text font-medium">Username</span>
          <input
            type="text"
            value={form.username}
            onChange={(event) => onFieldChange("username", event.target.value)}
            className="input input-bordered w-full"
            disabled={loading}
          />
        </label>

        <label className="form-control w-full">
          <span className="label-text font-medium">Cargo</span>
          <select
            className="select select-bordered w-full"
            value={form.role}
            onChange={(event) => onFieldChange("role", event.target.value)}
            disabled={loading}
          >
            {ROLE_OPTIONS.map(({ value, label }) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <label className="form-control w-full">
            <span className="label-text font-medium">Password</span>
            <input
              type="password"
              value={form.password}
              onChange={(event) => onFieldChange("password", event.target.value)}
              className="input input-bordered w-full"
              disabled={loading}
            />
          </label>

          <label className="form-control w-full">
            <span className="label-text font-medium">Repetir password</span>
            <input
              type="password"
              value={form.confirmPassword}
              onChange={(event) => onFieldChange("confirmPassword", event.target.value)}
              className="input input-bordered w-full"
              disabled={loading}
            />
          </label>

          <label className="form-control w-full md:col-span-2">
            <span className="label-text font-medium">Password Super Admin</span>
            <input
              type="password"
              value={form.superAdminPassword}
              onChange={(event) => onFieldChange("superAdminPassword", event.target.value)}
              className="input input-bordered w-full"
              disabled={loading}
            />
          </label>
        </div>
      </div>
    </Modal>
  );
}
