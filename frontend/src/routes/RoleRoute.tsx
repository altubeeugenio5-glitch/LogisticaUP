
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

interface RoleRouteProps {
  allowedRoles: ("ADMIN" | "CLIENTE" | "REPARTIDOR")[];
}

function RoleRoute({ allowedRoles }: RoleRouteProps) {
  const { usuario, loading } = useAuth();

  if (loading) {
    return <p>Verificando permisos...</p>;
  }

  if (!usuario) {
    return <Navigate to="/login" replace />;
  }

  if (!allowedRoles.includes(usuario.rol)) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

export default RoleRoute;
