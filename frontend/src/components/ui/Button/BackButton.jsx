import PropTypes from "prop-types";
import { useNavigate } from "react-router-dom";

/**
 * Botão de voltar reutilizável com cursor pointer e suporte a destino customizado.
 */
export default function BackButton({ to, label = "Voltar", className = "" }) {
  const navigate = useNavigate();

  const handleClick = () => {
    if (to) {
      navigate(to);
    } else {
      navigate(-1);
    }
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      className={`inline-flex items-center gap-2 text-sm font-semibold text-primary transition hover:text-primary-focus cursor-pointer ${className}`}
    >
      <span aria-hidden="true">&larr;</span>
      {label}
    </button>
  );
}

BackButton.propTypes = {
  to: PropTypes.string,
  label: PropTypes.string,
  className: PropTypes.string,
};
