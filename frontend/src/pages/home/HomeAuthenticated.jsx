import { HomeAuthenticatedProvider } from "./HomeAuthenticated/HomeAuthenticatedContext.jsx";
import HomeAuthenticatedLayout from "./HomeAuthenticated/HomeAuthenticatedLayout.jsx";

export default function HomeAuthenticated() {
  return (
    <HomeAuthenticatedProvider>
      <HomeAuthenticatedLayout />
    </HomeAuthenticatedProvider>
  );
}
