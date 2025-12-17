import React, { StrictMode } from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App.jsx";
import { AuthProvider } from "./auth/AuthContext.jsx";
import { EmployeeProfileProvider } from "./pages/profile/Employee/EmployeeProfileContext.jsx";
import { NotificationProvider } from "./context/NotificationContext.jsx";
import "bootstrap-icons/font/bootstrap-icons.css"; // Importa icones do Bootstrap
import "./index.css";

// Aplica tema guardado (foundry light / foundry-dark)
const storedThemeMode = localStorage.getItem("tf-theme-mode");
if (storedThemeMode === "dark") {
    document.documentElement.setAttribute("data-theme", "foundry-dark");
} else if (storedThemeMode === "light") {
    document.documentElement.setAttribute("data-theme", "foundry");
}

// Habilita estilo global que converte alerts do DaisyUI em toasts flutuantes.
document.body.classList.add("toastify-alerts");

ReactDOM.createRoot(document.getElementById("root")).render(
    <StrictMode>
        <BrowserRouter>
            <AuthProvider>
                <EmployeeProfileProvider>
                    <NotificationProvider>
                        <App />
                    </NotificationProvider>
                </EmployeeProfileProvider>
            </AuthProvider>
        </BrowserRouter>
    </StrictMode>
);
