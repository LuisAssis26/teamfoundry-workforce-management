import { useRef } from "react";
import PropTypes from "prop-types";

export default function DropZone({
  label,
  onSelect,
  disabled = false,
  hasFile = false,
  fileName,
  onRemove,
  allowedTypes,
  maxSizeMB,
  onError,
}) {
  const inputRef = useRef(null);

  const handleFile = (file) => {
    if (!file || disabled) return;
    const validation = validateFile(file);
    if (validation) {
      onError?.(validation);
      return;
    }
    onSelect(file);
  };

  const validateFile = (file) => {
    if (allowedTypes?.length && !allowedTypes.includes(file.type)) {
      return "Formato inválido. São permitidos PDF ou imagens.";
    }
    if (maxSizeMB && file.size > maxSizeMB * 1024 * 1024) {
      return `Ficheiro demasiado grande (>${maxSizeMB}MB).`;
    }
    return null;
  };

  function handleDrop(e) {
    e.preventDefault();
    if (disabled) return;
    const file = e.dataTransfer.files?.[0] || null;
    handleFile(file);
  }
  function prevent(e) { e.preventDefault(); }

  return (
    <div className="relative">
      {label && (
        <label className="label w-full mb-2">
          <span className="label-text font-medium w-full text-center">{label}</span>
        </label>
      )}
      {hasFile && onRemove && (
        <button
          type="button"
          className="btn btn-ghost btn-xs absolute right-2 top-[38px] z-10"
          onClick={(e) => { e.stopPropagation(); onRemove(); }}
        >
          <i className="bi bi-x" />
        </button>
      )}
      <div
        className={`border rounded-xl p-6 text-center bg-base-200/70 ${hasFile ? "border-base-300 bg-base-200" : "border-dashed border-base-300 bg-base-50"} ${disabled ? "cursor-not-allowed opacity-60" : "cursor-pointer"}`}
        onDragOver={prevent}
        onDragEnter={prevent}
        onDrop={handleDrop}
        onClick={() => !disabled && inputRef.current?.click()}
      >
        {hasFile ? (
          <div className="flex flex-col items-center gap-2 text-base-content">
            <span className="font-semibold text-sm truncate w-full">{fileName || "ficheiro.pdf"}</span>
            <button
              type="button"
              className="btn btn-sm btn-accent btn-outline"
              onClick={(e) => {
                e.stopPropagation();
                inputRef.current?.click();
              }}
            >
              Alterar
            </button>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-2 text-base-content/80">
            <span className="text-sm" >Arraste ficheiro aqui / clique para escolher</span>
            <i className="bi bi-plus-square text-2xl" />
          </div>
        )}
        <input
          ref={inputRef}
          type="file"
          className="hidden"
          disabled={disabled}
          accept={allowedTypes?.join(",")}
          onChange={(e) => handleFile(e.target.files?.[0] || null)}
        />
      </div>
    </div>
  );
}

DropZone.propTypes = {
  label: PropTypes.string,
  onSelect: PropTypes.func.isRequired,
  disabled: PropTypes.bool,
  hasFile: PropTypes.bool,
  fileName: PropTypes.string,
  onRemove: PropTypes.func,
  allowedTypes: PropTypes.arrayOf(PropTypes.string),
  maxSizeMB: PropTypes.number,
  onError: PropTypes.func,
};
