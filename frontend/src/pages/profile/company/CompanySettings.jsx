import { useCompanyProfile } from "./CompanyProfileContext.jsx";
import { SettingsView } from "../Settings/Settings.jsx";
import { fetchCompanyRequests } from "../../../api/profile/companyRequests.js";
import { deactivateCompanyAccount } from "../../../api/profile/companyProfile.js";

/**
 * Wrapper para reutilizar o Settings base no contexto da empresa.
 */
export default function CompanySettings() {
  const { companyProfile } = useCompanyProfile();

  return (
    <SettingsView
      profileEmail={companyProfile?.email}
      showReceiveOffers={false}
      allowDeactivate
      onValidateDeactivate={async () => {
        const requests = await fetchCompanyRequests();
        const hasActive =
          Array.isArray(requests) &&
          requests.some((req) => (req.state || "").toUpperCase() !== "COMPLETE");
        return hasActive ? "Não é possível desativar a conta com requisições pendentes ou ativas." : "";
      }}
      onDeactivate={(password) => deactivateCompanyAccount(password)}
    />
  );
}
