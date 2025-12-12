import { useRef, useState } from "react";
import {
  deleteEmployeeProfilePicture,
  uploadEmployeeProfilePicture,
} from "../../../../../api/profile/employeeProfile.js";
import { useEmployeeProfile } from "../../EmployeeProfileContext.jsx";

const formatName = (first, last) => {
  if (!first && !last) return "";
  return `${first ?? ""} ${last ?? ""}`.trim();
};

const MAX_IMAGE_MB = 3;

export default function ProfileHeader({ name }) {
  const { profile, loadingProfile, setProfile } = useEmployeeProfile();
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");
  const [showConfirm, setShowConfirm] = useState(false);
  const inputRef = useRef(null);

  const displayName = name || formatName(profile?.firstName, profile?.lastName);
  const showPlaceholder = loadingProfile && !displayName;
  const imageUrl = profile?.profilePictureUrl ?? null;

  const openPicker = () => {
    if (uploading) return;
    inputRef.current?.click();
  };

  const fileToBase64 = (file) =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });

  const handleFileChange = async (event) => {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;
    const allowedTypes = ["image/jpeg", "image/png", "image/webp", "image/jpg"];
    if (!allowedTypes.includes(file.type)) {
      setError("Apenas fotografias (jpg, png, webp) são permitidas.");
      return;
    }
    if (file.size > MAX_IMAGE_MB * 1024 * 1024) {
      setError(`Imagem demasiado grande (>${MAX_IMAGE_MB}MB).`);
      return;
    }
    setError("");
    setUploading(true);
    try {
      const base64 = await fileToBase64(file);
      const updatedProfile = await uploadEmployeeProfilePicture({
        file: base64,
        fileName: file.name,
      });
      setProfile(updatedProfile);
    } catch (err) {
      setError(err.message || "Falha ao atualizar a foto de perfil.");
    } finally {
      setUploading(false);
    }
  };

  const handleRemove = async (event) => {
    event.stopPropagation();
    if (!imageUrl || uploading) return;
    setError("");
    setShowConfirm(true);
  };

  return (
    <div className="flex flex-col items-center mt-4">
      <div className="relative">
        <button
          type="button"
          className="w-28 h-28 rounded-full border-4 border-primary text-primary flex items-center justify-center text-5xl overflow-hidden bg-base-100 focus-visible:outline focus-visible:outline-2 focus-visible:outline-primary/70"
          onClick={openPicker}
          disabled={uploading}
        >
          {imageUrl ? (
            <img
              src={imageUrl}
              alt="Foto de perfil"
              className="w-full h-full object-cover"
            />
          ) : (
            <i className="bi bi-person" aria-hidden="true" />
          )}
          {uploading && (
            <span className="absolute inset-0 flex items-center justify-center bg-base-100/70">
              <span className="loading loading-ring text-primary" />
            </span>
          )}
        </button>
        {imageUrl && (
          <button
            type="button"
            className="btn btn-ghost btn-xs absolute -right-2 -top-2 hover:bg-transparent active:bg-transparent focus:bg-transparent"
            onClick={handleRemove}
            disabled={uploading}
          >
            <i className="bi bi-x-lg" aria-hidden="true" />
          </button>
        )}
        <input
          ref={inputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          className="hidden"
          onChange={handleFileChange}
        />
      </div>
      <button
        type="button"
        className="btn btn-link btn-xs mt-2"
        onClick={openPicker}
        disabled={uploading}
      >
        {imageUrl ? "Alterar foto" : "Adicionar foto"}
      </button>
      {error && <p className="text-error text-sm mt-1">{error}</p>}
      {showConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-4">
          <div
            className="absolute inset-0 bg-black/40"
            onClick={() => setShowConfirm(false)}
            aria-hidden="true"
          />
          <div className="relative bg-base-100 rounded-xl shadow-lg p-5 w-full max-w-sm">
            <h4 className="text-lg font-semibold mb-2">Remover foto?</h4>
            <p className="text-sm text-base-content/70 mb-4">
              Tem a certeza que quer eliminar a foto de perfil?
            </p>
            <div className="flex justify-end gap-2">
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={() => setShowConfirm(false)}
              >
                Cancelar
              </button>
              <button
                type="button"
                className="btn btn-error btn-sm"
                onClick={async () => {
                  if (!imageUrl) return;
                  setUploading(true);
                  try {
                    await deleteEmployeeProfilePicture();
                    setProfile((prev) => (prev ? { ...prev, profilePictureUrl: null } : prev));
                    setShowConfirm(false);
                  } catch (err) {
                    setError(err.message || "Falha ao remover a foto.");
                  } finally {
                    setUploading(false);
                  }
                }}
              >
                Eliminar
              </button>
            </div>
          </div>
        </div>
      )}
      <h1 className="mt-4 text-2xl md:text-3xl font-semibold min-h-[1.5rem]">
        {showPlaceholder ? (
          <span
            className="inline-block w-40 h-4 rounded-full bg-base-300 animate-pulse"
            aria-hidden="true"
          />
        ) : (
          displayName || " "
        )}
      </h1>
    </div>
  );
}
