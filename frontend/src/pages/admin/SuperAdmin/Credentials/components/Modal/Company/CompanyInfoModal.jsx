import Modal from "../../../../../../../components/ui/Modal/Modal.jsx";
import Button from "../../../../../../../components/ui/Button/Button.jsx";

const fields = [
  { key: "companyName", label: "Nome da empresa" },
  { key: "credentialEmail", label: "Email da credencial" },
  { key: "nif", label: "NIF" },
  { key: "website", label: "Website" },
  { key: "country", label: "País" },
  { key: "address", label: "Morada" },
  { key: "responsibleName", label: "Nome do responsável" },
  { key: "responsibleEmail", label: "Email do responsável" },
];

export default function CompanyInfoModal({ company, open, onClose, onReject }) {
  return (
    <Modal
      open={open}
      title="Detalhes da Credencial"
      onClose={onClose}
      actions={
        <div className="w-full flex justify-end gap-3">
          <Button
            label="Recusar"
            variant="error"
            className="max-w-32 text-white"
            onClick={onReject}
          />
          <Button
            label="Fechar"
            variant="outline"
            className="max-w-32 btn btn-secondary"
            onClick={onClose}
          />
        </div>
      }
    >
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {fields.map(({ key, label }) => (
          <label key={key} className="form-control w-full">
            <span className="label-text font-medium">{label}</span>
            <input
              type="text"
              value={company?.[key] ?? ""}
              disabled
              className="input input-bordered w-full"
            />
          </label>
        ))}
      </div>
    </Modal>
  );
}
