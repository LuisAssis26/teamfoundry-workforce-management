import AppHomeLayout from "./AppHomeLayout.jsx";
import { AppHomeProvider } from "./AppHomeContext.jsx";

export default function AppHomeManager({ onUnauthorized }) {
  return (
    <AppHomeProvider onUnauthorized={onUnauthorized}>
      <AppHomeLayout />
    </AppHomeProvider>
  );
}
