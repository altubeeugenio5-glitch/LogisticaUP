
import { NavLink } from "react-router-dom";

function AdminSidebar() {
  return (
    <aside className="admin-sidebar">
      <div className="admin-sidebar-logo">
        <h2>LOGÍSTICA</h2>
        <p>Administración</p>
      </div>

      <nav className="admin-sidebar-nav">
        <NavLink
          to="/admin/pedidos"
          className={({ isActive }) =>
            isActive ? "admin-sidebar-link active" : "admin-sidebar-link"
          }
        >
          <span>▤</span>
          Pedidos
        </NavLink>

        <NavLink
          to="/admin/rutas"
          className={({ isActive }) =>
            isActive ? "admin-sidebar-link active" : "admin-sidebar-link"
          }
        >
          <span>⌖</span>
          Gestión de rutas
        </NavLink>

        <NavLink
          to="/admin/repartidores"
          className={({ isActive }) =>
            isActive ? "admin-sidebar-link active" : "admin-sidebar-link"
          }
        >
          <span>♙</span>
          Repartidores
        </NavLink>
      </nav>
    </aside>
  );
}

export default AdminSidebar;
