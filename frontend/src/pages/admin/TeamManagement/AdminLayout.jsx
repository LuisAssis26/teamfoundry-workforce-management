import React from "react";
import { Outlet } from "react-router-dom";
import { AdminDataProvider } from "./AdminDataContext.jsx";

export default function AdminLayout() {
  return (
      <AdminDataProvider>
        <Outlet />
      </AdminDataProvider>
  );
}
