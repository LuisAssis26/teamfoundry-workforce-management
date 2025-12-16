import { Outlet } from "react-router-dom";
import { AdminDataProvider } from "./AdminDataContext.jsx";
import AdminNavbar from "../../../components/sections/AdminNavbar.jsx";

export default function AdminLayout() {
  return (
      
      <AdminDataProvider>
        <AdminNavbar />
        <Outlet />
      </AdminDataProvider>
  );
}
