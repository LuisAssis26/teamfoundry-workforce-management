import VariableManagementLayout from "./VariableManagementLayout.jsx";
import { VariableManagementProvider } from "./VariableManagementContext.jsx";

export default function VariableManagement() {
  return (
    <VariableManagementProvider>
      <VariableManagementLayout />
    </VariableManagementProvider>
  );
}
