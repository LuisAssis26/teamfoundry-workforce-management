import { useEffect, useMemo, useRef, useState } from "react";
import { apiFetch } from "../../../../api/auth/client.js";
import CredentialList from "./components/Credential/CredentialList.jsx";
import Button from "../../../../components/ui/Button/Button.jsx";
import { useSuperAdminData } from "../SuperAdminDataContext.jsx";
import CompanyInfoModal from "./components/Modal/Company/CompanyInfoModal.jsx";
import CompanyApprovalModal from "./components/Modal/Company/CompanyApprovalModal.jsx";
import CompanyRejectionModal from "./components/Modal/Company/CompanyRejectionModal.jsx";
import AdminDisableModal from "./components/Modal/Admin/AdminDisableModal.jsx";
import AdminEditModal from "./components/Modal/Admin/AdminEditModal.jsx";
import AdminCreateModal from "./components/Modal/Admin/AdminCreateModal.jsx";

const ROLE_OPTIONS = [
  { value: "admin", label: "Admin" },
  { value: "super-admin", label: "Super Admin" },
];

const getInitialCreateForm = () => ({
  username: "",
  role: "admin",
  password: "",
  confirmPassword: "",
  superAdminPassword: "",
});

const getInitialEditForm = () => ({
  username: "",
  role: "admin",
  changePassword: false,
  password: "",
  confirmPassword: "",
  superAdminPassword: "",
});

const normalizeRole = (role) => {
  if (!role) return "admin";
  const lower = String(role).toLowerCase();
  return lower === "superadmin" ? "super-admin" : lower;
};

export default function Credenciais() {
  const {
    credentials: {
      companies: {
        data: businessCompanies = [],
        loading: isLoadingCompanies,
        loaded: companiesLoaded,
        error: companyError,
        refresh: refreshCompanies,
        setData: setBusinessCompanies,
      },
      admins: {
        data: adminCredentials = [],
        loading: isLoadingAdmins,
        loaded: adminsLoaded,
        error: adminError,
        refresh: refreshAdmins,
        setData: setAdminCredentials,
      },
    },
    staffing: {
      requests: {
        data: workRequests = [],
        loading: isLoadingRequests,
        loaded: requestsLoaded,
        refresh: refreshRequests,
      },
    },
  } = useSuperAdminData();

  const [createAdminError, setCreateAdminError] = useState(null);
  const [isCreatingAdmin, setIsCreatingAdmin] = useState(false);

  const [editAdminError, setEditAdminError] = useState(null);
  const [isSavingAdmin, setIsSavingAdmin] = useState(false);

  const [disableAdminError, setDisableAdminError] = useState(null);
  const [isDisablingAdmin, setIsDisablingAdmin] = useState(false);
  const [disableForm, setDisableForm] = useState({ superAdminPassword: "" });

  const [companyInModal, setCompanyInModal] = useState(null);
  const [companyPendingRejection, setCompanyPendingRejection] = useState(null);
  const [companyPendingApproval, setCompanyPendingApproval] = useState(null);
  const [approveCompanyError, setApproveCompanyError] = useState(null);
  const [isApprovingCompany, setIsApprovingCompany] = useState(false);
  const [approveForm, setApproveForm] = useState({ superAdminPassword: "" });

  const [rejectCompanyError, setRejectCompanyError] = useState(null);
  const [isRejectingCompany, setIsRejectingCompany] = useState(false);
  const [rejectForm, setRejectForm] = useState({ superAdminPassword: "" });

  const [adminPendingDisable, setAdminPendingDisable] = useState(null);

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [adminBeingEdited, setAdminBeingEdited] = useState(null);

  const [editForm, setEditForm] = useState(getInitialEditForm);
  const [createForm, setCreateForm] = useState(getInitialCreateForm);

  const initialCompaniesLoad = useRef(false);
  useEffect(() => {
    if (companiesLoaded || initialCompaniesLoad.current) return;
    initialCompaniesLoad.current = true;
    refreshCompanies().catch(() => {});
  }, [companiesLoaded, refreshCompanies]);

  const initialAdminsLoad = useRef(false);
  useEffect(() => {
    if (adminsLoaded || initialAdminsLoad.current) return;
    initialAdminsLoad.current = true;
    refreshAdmins().catch(() => {});
  }, [adminsLoaded, refreshAdmins]);

  const initialRequestsLoad = useRef(false);
  useEffect(() => {
    if (requestsLoaded || initialRequestsLoad.current) return;
    initialRequestsLoad.current = true;
    refreshRequests().catch(() => {});
  }, [requestsLoaded, refreshRequests]);

  const businessFields = useMemo(
    () => [
      { key: "companyName", label: "Nome Empresa:" },
      { key: "credentialEmail", label: "Email Credencial:" },
      { key: "nif", label: "NIF:" },
      { key: "country", label: "Pais:" },
    ],
    []
  );

  const adminFields = useMemo(
    () => [
      { key: "username", label: "Username:" },
      {
        key: "role",
        label: "Cargo:",
        getValue: (admin) => (admin.role === "super-admin" ? "Super Admin" : "Admin"),
      },
      {
        key: "openRequests",
        label: "Requisições:",
        getValue: (admin) => admin.openRequests ?? 0,
      },
      {
        key: "openSlots",
        label: "Mão de obra associada:",
        getValue: (admin) => admin.openSlots ?? 0,
      },
    ],
    []
  );

  const adminRequestStats = useMemo(() => {
    const stats = {};
    workRequests.forEach((req) => {
      if (!req?.responsibleAdminId) return;
      const key = String(req.responsibleAdminId);
      if (!stats[key]) stats[key] = { open: 0, openSlots: 0 };
      const state = (req.state || "").toUpperCase();
      if (state !== "COMPLETE") {
        stats[key].open += 1;
        stats[key].openSlots += Number(req.workforceNeeded ?? 0);
      }
    });
    return stats;
  }, [workRequests]);

  const adminsWithStats = useMemo(
    () =>
      adminCredentials.map((admin) => {
        const stats = adminRequestStats[String(admin.id)] ?? {};
        return {
          ...admin,
          openRequests: stats.open ?? 0,
          openSlots: stats.openSlots ?? 0,
        };
      }),
    [adminCredentials, adminRequestStats]
  );

  const handleViewMore = (company) => setCompanyInModal(company);

  const handleAccept = (id) => {
    const company = businessCompanies.find((item) => item.id === id);
    if (!company) return;
    setApproveCompanyError(null);
    setApproveForm({ superAdminPassword: "" });
    setCompanyPendingApproval(company);
  };

const handleRejectClick = () => {
  setCompanyPendingRejection(companyInModal);
  setRejectCompanyError(null);
  setRejectForm({ superAdminPassword: "" });
};

  const handleEdit = (admin) => {
    if (!admin) return;

    setAdminBeingEdited(admin);
    setEditAdminError(null);
    setEditForm({
      username: admin.username,
      role: admin.role,
      changePassword: false,
      password: "",
      confirmPassword: "",
      superAdminPassword: "",
    });
    setIsEditModalOpen(true);
  };

  const handleDisable = (adminId) => {
    const admin = adminsWithStats.find((item) => item.id === adminId);
    if (!admin) return;

    if ((admin.openRequests ?? 0) > 0) {
      setDisableAdminError("Não é possível desativar: administrador possui requisições atribuídas.");
      return;
    }

    setAdminPendingDisable(admin);
    setDisableAdminError(null);
    setDisableForm({ superAdminPassword: "" });
  };

  const handleCreate = () => {
    setCreateAdminError(null);
    setCreateForm(getInitialCreateForm());
    setIsCreateModalOpen(true);
  };

  const handleEditFieldChange = (field, value) => {
    setEditForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleCreateFieldChange = (field, value) => {
    setCreateForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleApproveFieldChange = (value) => {
    setApproveForm({ superAdminPassword: value });
  };

  const handleDisableFieldChange = (value) => {
    setDisableForm({ superAdminPassword: value });
  };

  const handleToggleChangePassword = () => {
    setEditForm((prev) => ({
      ...prev,
      changePassword: !prev.changePassword,
      password: "",
      confirmPassword: "",
    }));
  };

  const handleCloseEditModal = () => {
    setIsEditModalOpen(false);
    setAdminBeingEdited(null);
  };

  const handleCloseCreateModal = () => setIsCreateModalOpen(false);

  const handleSaveAdmin = async () => {
    if (!adminBeingEdited) return;

    setEditAdminError(null);

    if (!editForm.username.trim()) {
      setEditAdminError("Username é obrigatório.");
      return;
    }

    if (!editForm.superAdminPassword.trim()) {
      setEditAdminError("Informe a password do Super Admin para confirmar.");
      return;
    }

    if (editForm.changePassword) {
      if (!editForm.password.trim() || !editForm.confirmPassword.trim()) {
        setEditAdminError(
            "Para alterar a password, preencha e confirme o novo valor."
        );
        return;
      }
      if (editForm.password !== editForm.confirmPassword) {
        setEditAdminError("As passwords devem coincidir.");
        return;
      }
    }

    setIsSavingAdmin(true);
    try {
      const payload = {
        username: editForm.username.trim(),
        role: editForm.role === "super-admin" ? "SUPERADMIN" : "ADMIN",
        password: editForm.changePassword ? editForm.password : null,
        superAdminPassword: editForm.superAdminPassword.trim(),
      };

      const resp = await apiFetch(
          `/api/super-admin/credentials/admins/${adminBeingEdited.id}`,
          {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          }
      );

      if (!resp.ok) {
        const message = (await resp.json().catch(() => null))?.error;
        throw new Error(message || "Não foi possível atualizar o administrador.");
      }

      const updated = await resp.json();
      setAdminCredentials((prev) =>
          prev.map((admin) =>
              admin.id === adminBeingEdited.id
                  ? {
                    ...updated,
                    role: normalizeRole(updated.role),
                  }
                  : admin
          )
      );

      handleCloseEditModal();
    } catch (error) {
      setEditAdminError(error.message || "Erro inesperado ao editar administrador.");
    } finally {
      setIsSavingAdmin(false);
    }
  };

  const handleSaveNewAdmin = async () => {
    setCreateAdminError(null);

    if (!createForm.username.trim() || !createForm.password.trim()) {
      setCreateAdminError("Username e password são obrigatórios.");
      return;
    }

    if (createForm.password !== createForm.confirmPassword) {
      setCreateAdminError("As passwords devem coincidir.");
      return;
    }

    if (!createForm.superAdminPassword.trim()) {
      setCreateAdminError(
          "Informe a password do Super Admin para criar um administrador."
      );
      return;
    }

    if (!createForm.superAdminPassword.trim()) {
      setCreateAdminError("Informe a password do Super Admin para criar um administrador.");
      return;
    }

    setIsCreatingAdmin(true);
    try {
      const payload = {
        username: createForm.username.trim(),
        password: createForm.password,
        role: createForm.role === "super-admin" ? "SUPERADMIN" : "ADMIN",
        superAdminPassword: createForm.superAdminPassword.trim(),
      };

      const resp = await apiFetch("/api/super-admin/credentials/admins", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!resp.ok) {
        const message = (await resp.json().catch(() => null))?.error;
        throw new Error(message || "Não foi possível criar o administrador.");
      }

      const created = await resp.json();
      setAdminCredentials((prev) => [
        ...prev,
        {
          ...created,
          role: normalizeRole(created.role),
        },
      ]);
      setCreateForm(getInitialCreateForm());
      setIsCreateModalOpen(false);
    } catch (error) {
      setCreateAdminError(error.message || "Erro inesperado ao criar administrador.");
    } finally {
      setIsCreatingAdmin(false);
    }
  };

const handleConfirmReject = async () => {
  if (!companyPendingRejection) return;

  const password = rejectForm.superAdminPassword.trim();
  if (!password) {
    setRejectCompanyError("Informe a password do Super Admin para confirmar.");
    return;
  }

  setRejectCompanyError(null);
  setIsRejectingCompany(true);

  try {
    const resp = await apiFetch(
      `/api/super-admin/credentials/companies/${companyPendingRejection.id}/reject`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ superAdminPassword: password }),
      }
    );

    if (!resp.ok) {
      const message = (await resp.json().catch(() => null))?.error;
      throw new Error(message || "Não foi possível recusar a empresa.");
    }

    setBusinessCompanies((prev) =>
      prev.filter((company) => company.id !== companyPendingRejection.id)
    );
    setCompanyPendingRejection(null);
    setRejectForm({ superAdminPassword: "" });
    // Fecha o modal de detalhes caso ainda esteja aberto para essa empresa.
    setCompanyInModal((current) =>
      current?.id === companyPendingRejection.id ? null : current
    );
  } catch (error) {
    setRejectCompanyError(error.message || "Erro inesperado ao recusar empresa.");
  } finally {
    setIsRejectingCompany(false);
  }
};


const handleCancelReject = () => {
  setRejectCompanyError(null);
  setRejectForm({ superAdminPassword: "" });
  setCompanyPendingRejection(null);
};


  const handleConfirmApprove = async () => {
    if (!companyPendingApproval) return;

    const password = approveForm.superAdminPassword.trim();
    if (!password) {
      setApproveCompanyError("Informe a password do Super Admin para confirmar.");
      return;
    }

    setApproveCompanyError(null);
    setIsApprovingCompany(true);

    try {
      const resp = await apiFetch(
          `/api/super-admin/credentials/companies/${companyPendingApproval.id}/approve`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ superAdminPassword: password }),
          }
      );

      if (!resp.ok) {
        const message = (await resp.json().catch(() => null))?.error;
        throw new Error(message || "Não foi possível aprovar a empresa.");
      }

      setBusinessCompanies((prev) =>
          prev.filter((company) => company.id !== companyPendingApproval.id)
      );
      setCompanyPendingApproval(null);
      setApproveForm({ superAdminPassword: "" });
    } catch (error) {
      setApproveCompanyError(error.message || "Erro inesperado ao aprovar empresa.");
    } finally {
      setIsApprovingCompany(false);
    }
  };

  const handleCancelApprove = () => {
    setApproveCompanyError(null);
    setApproveForm({ superAdminPassword: "" });
    setCompanyPendingApproval(null);
  };

  const handleConfirmDisable = async () => {
    if (!adminPendingDisable) return;

    const password = disableForm.superAdminPassword.trim();
    if (!password) {
      setDisableAdminError("Informe a password do Super Admin para confirmar.");
      return;
    }

    setDisableAdminError(null);
    setIsDisablingAdmin(true);

    try {
      const resp = await apiFetch(
          `/api/super-admin/credentials/admins/${adminPendingDisable.id}`,
          {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ superAdminPassword: password }),
          }
      );

      if (!resp.ok) {
        const message = (await resp.json().catch(() => null))?.error;
        throw new Error(message || "Não foi possível desativar o administrador.");
      }

      setAdminCredentials((prev) =>
          prev.filter((admin) => admin.id !== adminPendingDisable.id)
      );
      setAdminPendingDisable(null);
      setDisableForm({ superAdminPassword: "" });
    } catch (error) {
      setDisableAdminError(
          error.message || "Erro inesperado ao desativar administrador."
      );
    } finally {
      setIsDisablingAdmin(false);
    }
  };

  const handleCancelDisable = () => {
    setDisableAdminError(null);
    setDisableForm({ superAdminPassword: "" });
    setAdminPendingDisable(null);
  };

  return (
      <>
        <section className="space-y-12">
          {companyError && (
              <div className="alert alert-error shadow">
                <span>{companyError}</span>
              </div>
          )}

          {isLoadingCompanies ? (
              <div className="bg-base-100 border border-base-200 rounded-3xl shadow-xl p-8">
                <p className="text-base-content font-semibold">
                  Carregando credenciais empresariais...
                </p>
              </div>
          ) : (
              <CredentialList
                  title="Credenciais Empresariais"
                  companies={businessCompanies}
                  fieldConfig={businessFields}
                  onViewMore={handleViewMore}
                  onAccept={handleAccept}
                  onSearch={(value) => console.log("Pesquisar empresa:", value)}
                  searchPlaceholder="Pesquisar empresa"
                  viewVariant="primary"
                  acceptVariant="secondary"
                  viewButtonClassName="text-white"
                  acceptButtonClassName="btn-outline"
              />
          )}

          {adminError && (
              <div className="alert alert-error shadow">
                <span>{adminError}</span>
              </div>
          )}
          {disableAdminError && !adminPendingDisable && (
              <div className="alert alert-warning shadow">
                <span>{disableAdminError}</span>
              </div>
          )}

          {isLoadingAdmins ? (
              <div className="bg-base-100 border border-base-200 rounded-3xl shadow-xl p-8">
                <p className="text-base-content font-semibold">
                  Carregando credenciais administrativas...
                </p>
              </div>
          ) : (
              <CredentialList
                  title="Credenciais Administrativas"
                  companies={adminsWithStats}
                  fieldConfig={adminFields}
                  onViewMore={handleEdit}
                  onAccept={handleDisable}
                  viewLabel="Editar"
                  acceptLabel="Desativar"
                  viewVariant="primary"
                  acceptVariant="secondary"
                  acceptButtonClassName="btn-outline"
                  headerActions={
                    <Button
                        label="Criar"
                        variant=""
                        className="w-full md:w-auto btn-outline"
                        onClick={handleCreate}
                    />
                  }
              />
          )}
        </section>

        <CompanyInfoModal
          open={Boolean(companyInModal)}
          company={companyInModal}
          onClose={() => setCompanyInModal(null)}
          onReject={handleRejectClick}
        />

        <CompanyRejectionModal
          open={Boolean(companyPendingRejection)}
          company={companyPendingRejection}
          password={rejectForm.superAdminPassword}
          onPasswordChange={(value) => setRejectForm({ superAdminPassword: value })}
          onConfirm={handleConfirmReject}
          onCancel={handleCancelReject}
          loading={isRejectingCompany}
          error={rejectCompanyError}
        />

        <CompanyApprovalModal
          open={Boolean(companyPendingApproval)}
          company={companyPendingApproval}
          password={approveForm.superAdminPassword}
          onPasswordChange={handleApproveFieldChange}
          onConfirm={handleConfirmApprove}
          onCancel={handleCancelApprove}
          loading={isApprovingCompany}
          error={approveCompanyError}
        />

        <AdminDisableModal
          open={Boolean(adminPendingDisable)}
          admin={adminPendingDisable}
          password={disableForm.superAdminPassword}
          onPasswordChange={handleDisableFieldChange}
          onConfirm={handleConfirmDisable}
          onCancel={handleCancelDisable}
          loading={isDisablingAdmin}
          error={disableAdminError}
        />

        <AdminEditModal
          open={isEditModalOpen}
          form={editForm}
          error={editAdminError}
          loading={isSavingAdmin}
          onClose={handleCloseEditModal}
          onSave={handleSaveAdmin}
          onFieldChange={handleEditFieldChange}
          onToggleChangePassword={handleToggleChangePassword}
        />

        <AdminCreateModal
          open={isCreateModalOpen}
          form={createForm}
          error={createAdminError}
          loading={isCreatingAdmin}
          onClose={handleCloseCreateModal}
          onSave={handleSaveNewAdmin}
          onFieldChange={handleCreateFieldChange}
        />
      </>
  );
}
