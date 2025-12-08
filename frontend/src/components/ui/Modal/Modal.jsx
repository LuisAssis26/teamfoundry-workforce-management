import PropTypes from "prop-types";

export default function Modal({ open, title, onClose, children, actions, className = "" }) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto px-4 py-6">
      <div
        className="absolute inset-0 bg-[#111827]/80 backdrop-blur-sm"
        onClick={onClose}
        aria-hidden="true"
      />
      <div className={`relative bg-base-100 text-base-content rounded-2xl shadow-xl w-full max-w-xl mx-4 p-5 ${className}`}>
        {title && (
          <h3 className="text-3xl font-semibold mb-4 text-primary">{title}</h3>
        )}
        <div>{children}</div>
        {actions && <div className="mt-6 flex justify-end gap-3">{actions}</div>}
        <button
          type="button"
          className="absolute top-3 right-3 btn btn-ghost btn-sm"
          onClick={onClose}
          aria-label="Fechar"
        >
          <i className="bi bi-x-lg" />
        </button>
      </div>
    </div>
  );
}

Modal.propTypes = {
  open: PropTypes.bool,
  title: PropTypes.string,
  onClose: PropTypes.func,
  children: PropTypes.node,
  actions: PropTypes.node,
  className: PropTypes.string,
};

