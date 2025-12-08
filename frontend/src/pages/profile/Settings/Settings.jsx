import { useEffect, useState } from "react";
import Button from "../../../components/ui/Button/Button.jsx";
import ForgotPassword from "../../../components/ui/Modal/ForgotPassword.jsx";
import { useEmployeeProfile } from "../Employee/EmployeeProfileContext.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";
import { deactivateEmployeeAccount } from "../../../api/profile/employeeProfile.js";
import Modal from "../../../components/ui/Modal/Modal.jsx";
import { listEmployeeJobs } from "../../../api/profile/profileJobs.js";

export default function Settings() {
  const { profile } = useEmployeeProfile();
  const { logout } = useAuthContext();
  const [receiveOffers, setReceiveOffers] = useState(true);
  const [theme, setTheme] = useState(() => {
    const stored = localStorage.getItem("tf-theme-mode");
    if (stored === "dark" || stored === "light") return stored;
    const current = document.documentElement.getAttribute("data-theme");
    return current === "foundry-dark" ? "dark" : "light";
  });
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [showDeactivateModal, setShowDeactivateModal] = useState(false);
  const [deactivatePassword, setDeactivatePassword] = useState("");
  const [confirmDeactivation, setConfirmDeactivation] = useState(false);
  const [deactivateError, setDeactivateError] = useState("");
  const [deactivateBlockMessage, setDeactivateBlockMessage] = useState("");

  const applyTheme = (mode) => {
    const dataTheme = mode === "dark" ? "foundry-dark" : "foundry";
    document.documentElement.setAttribute("data-theme", dataTheme);
    localStorage.setItem("tf-theme-mode", mode);
  };

  useEffect(() => {
    applyTheme(theme);
  }, [theme]);

  return (
    <>
      <section className="w-full space-y-6">
        <header className="flex items-center justify-center">
          <h2 className="text-3xl font-semibold text-center sm:text-center md:text-left w-full">Definições</h2>
        </header>

        <div className="rounded-xl border border-base-300 bg-base-100 shadow p-6 space-y-6">
          <div className="space-y-3">
            <h3 className="text-xl font-semibold text-center md:text-left">Conta</h3>

            <div className="flex items-center justify-between gap-3 rounded-lg border border-base-200 bg-base-200/60 px-4 py-3">
              <div>
                <p className="font-medium">Receber ofertas</p>
                <p className="text-xs text-base-content/70">Ativar ou desativar notificações de novas ofertas.</p>
              </div>
              <input
                type="checkbox"
                className="toggle toggle-primary"
                checked={receiveOffers}
                onChange={() => setReceiveOffers((v) => !v)}
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div className="rounded-lg border border-base-200 bg-base-200/60 px-4 py-3 flex flex-col gap-2">
                <div>
                  <p className="font-medium">Mudar password</p>
                  <p className="text-xs text-base-content/70">Altere a sua palavra-passe em segurança.</p>
                </div>
                <Button
                  label="Mudar password"
                  variant="primary"
                  className="w-full sm:w-auto"
                  onClick={() => setShowPasswordModal(true)}
                />
              </div>

              <div className="rounded-lg border border-base-200 bg-base-200/60 px-4 py-3 flex flex-col gap-2">
                <div>
                  <p className="font-medium text-error">Excluir conta</p>
                  <p className="text-xs text-base-content/70">Eliminar a conta e dados associados.</p>
                </div>
                <Button
                  label="Excluir conta"
                  variant="outline"
                  className="w-full sm:w-auto btn-error"
                  onClick={async () => {
                    setDeactivateBlockMessage("");
                    try {
                      const history = await listEmployeeJobs();
                      const hasActive =
                        Array.isArray(history) &&
                        history.some((job) => {
                          const status = (job.status || "").toUpperCase();
                          const endDate = job.endDate ? new Date(job.endDate) : null;
                          const inProgress = endDate ? endDate >= new Date() : true;
                          return status !== "CLOSED" || inProgress;
                        });
                      if (hasActive) {
                        setDeactivateBlockMessage("Não é possível desativar a conta com serviços aceites/ativos.");
                        return;
                      }
                      setShowDeactivateModal(true);
                    } catch (err) {
                      setDeactivateBlockMessage(err.message || "Não foi possível verificar os serviços.");
                    }
                  }}
                />
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <h3 className="text-xl font-semibold text-center md:text-left">Aplicação</h3>
            <div className="flex items-center justify-between gap-3 rounded-lg border border-base-200 bg-base-200/60 px-4 py-3">
              <div>
                <p className="font-medium">Tema</p>
                <p className="text-xs text-base-content/70">Escolha entre claro ou escuro.</p>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs text-base-content/70">Light</span>
                <input
                  type="checkbox"
                  className="toggle toggle-primary"
                  checked={theme === "dark"}
                  onChange={(e) => {
                    const next = e.target.checked ? "dark" : "light";
                    setTheme(next);
                  }}
                />
                <span className="text-xs text-base-content/70">Dark</span>
              </div>
            </div>
          </div>
        </div>
      </section>
      {deactivateBlockMessage && (
        <div className="alert alert-warning mt-4">
          <span>{deactivateBlockMessage}</span>
        </div>
      )}
      <ForgotPassword
        open={showPasswordModal}
        onClose={() => setShowPasswordModal(false)}
        initialEmail={profile?.email || ""}
      />
      <Modal
        open={showDeactivateModal}
        onClose={() => {
          setShowDeactivateModal(false);
          setDeactivatePassword("");
          setConfirmDeactivation(false);
          setDeactivateError("");
        }}
        title="Eliminar conta"
        className="max-w-md"
      >
        <div className="space-y-4">
          <p className="text-sm text-base-content/70">
            Esta ação é irreversível. Confirme a password para desativar o acesso à conta.
          </p>
          <input
            type="password"
            className="input input-bordered w-full"
            placeholder="Password"
            value={deactivatePassword}
            onChange={(e) => setDeactivatePassword(e.target.value)}
          />
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              className="checkbox checkbox-sm"
              checked={confirmDeactivation}
              onChange={(e) => setConfirmDeactivation(e.target.checked)}
            />
            <span>Confirmo que pretendo desativar o acesso desta conta.</span>
          </label>
          {deactivateError && <p className="text-error text-sm">{deactivateError}</p>}
          <div className="flex justify-end gap-2">
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={() => {
                setShowDeactivateModal(false);
                setDeactivatePassword("");
                setConfirmDeactivation(false);
                setDeactivateError("");
              }}
            >
              Cancelar
            </button>
            <button
              type="button"
              className="btn btn-error btn-sm"
              onClick={async () => {
                if (!confirmDeactivation) {
                  setDeactivateError("Confirme que compreende que esta ação é irreversível.");
                  return;
                }
                if (!deactivatePassword.trim()) {
                  setDeactivateError("Introduza a sua password.");
                  return;
                }
                try {
                  setDeactivateError("");
                  await deactivateEmployeeAccount(deactivatePassword.trim());
                  logout();
                  window.location.href = "/";
                } catch (err) {
                  setDeactivateError(err.message || "Não foi possível desativar a conta.");
                }
              }}
            >
              Desativar conta
            </button>
          </div>
        </div>
      </Modal>
    </>
  );
}
