
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import Login from "../pages/Login";
import ProtectedRoute from "./ProtectedRoute";
import RoleRoute from "./RoleRoute";

import ClienteDashboard from "../pages/cliente/ClienteDashboard";
import MisPedidos from "../pages/cliente/MisPedidos";
import ClienteLayout from "../pages/cliente/ClienteLayout";
import NuevoPedido from "../pages/cliente/NuevoPedido";

import AdminLayout from "../pages/admin/AdminLayout";
import PedidosAdmin from "../pages/admin/PedidosAdmin";
import RutasAdmin from "../pages/admin/RutasAdmin";
import RepartidoresAdmin from "../pages/admin/RepartidoresAdmin";

import LogoutButton from "../components/layout/LogoutButton";
import MisRutas from "../pages/repartidor/MisRutas";

function RepartidorPanel() {
  return (
    <main>
      <MisRutas />
      <LogoutButton />
    </main>
  );
}

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />

        <Route element={<ProtectedRoute />}>
          {/* Administrador */}
          <Route element={<RoleRoute allowedRoles={["ADMIN"]} />}>
            <Route path="/admin" element={<AdminLayout />}>
              <Route
                index
                element={<Navigate to="/admin/pedidos" replace />}
              />

              <Route path="pedidos" element={<PedidosAdmin />} />
              <Route path="rutas" element={<RutasAdmin />} />
              <Route path="repartidores" element={<RepartidoresAdmin />} />
            </Route>
          </Route>

          {/* Cliente */}
          <Route element={<RoleRoute allowedRoles={["CLIENTE"]} />}>
            <Route path="/cliente" element={<ClienteLayout />}>
              <Route index element={<ClienteDashboard />} />
              <Route path="pedidos" element={<MisPedidos />} />
              <Route path="nuevo-pedido" element={<NuevoPedido />} />
            </Route>
          </Route>

          {/* Repartidor */}
          <Route element={<RoleRoute allowedRoles={["REPARTIDOR"]} />}>
            <Route
              path="/repartidor"
              element={<RepartidorPanel />}
            />
          </Route>
        </Route>

        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRoutes;
