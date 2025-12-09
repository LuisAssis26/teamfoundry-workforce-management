import { useCompanyProfile } from "./CompanyProfileContext.jsx";
import { SettingsView } from "../Settings/Settings.jsx";

/**
 * Wrapper para reutilizar o Settings base no contexto da empresa.
 * Oculta o toggle de "Receber ofertas" e mantém desativação indisponível.
 */
export default function CompanySettings() {
  const { companyProfile } = useCompanyProfile();

  return (
    <SettingsView
      profileEmail={companyProfile?.email}
      showReceiveOffers={false}
      allowDeactivate={false}
    />
  );
}
