import { Outlet } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

import ClienteSidebar from "../../components/layout/ClienteSidebar";
import LogoutButton from "../../components/layout/LogoutButton";

import "./ClienteDashboard.css";

function ClienteLayout() {
  const { usuario } = useAuth();

  return (
    <div className="cliente-dashboard">
      <div className="cliente-layout">

        <ClienteSidebar />

        <div className="cliente-content">

          <header className="cliente-header">
            <div>
              <h1>Panel del cliente</h1>

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

export default ClienteLayout;