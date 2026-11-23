
import { Outlet } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

import AdminSidebar from "../../components/layout/AdminSidebar";
import LogoutButton from "../../components/layout/LogoutButton";

import "./AdminLayout.css";

function AdminLayout() {
  const { usuario } = useAuth();

  return (
    <div className="admin-dashboard">
      <div className="admin-layout">

        <AdminSidebar />

        <div className="admin-content">

          <header className="admin-header">
            <div>
              <h1>Panel administrativo</h1>

              <p>
                Bienvenido, {usuario?.nombre} {usuario?.apellido}
              </p>
            </div>

            <LogoutButton />
          </header>

          <Outlet />

        </div>
      </div>
    </div>
  );
}

export default AdminLayout;
