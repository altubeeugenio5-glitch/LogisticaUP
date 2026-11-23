import { NavLink } from "react-router-dom";

function ClienteSidebar() {
  return (
    <aside className="cliente-sidebar">
      <div className="sidebar-logo">
        <h2>LOGÍSTICA</h2>
      </div>

      <nav className="sidebar-nav">
        <NavLink
          to="/cliente"
          end
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <span>⌂</span>
          Inicio
        </NavLink>

        <NavLink
          to="/cliente/pedidos"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <span>▤</span>
          Mis pedidos
        </NavLink>

        <NavLink
          to="/cliente/nuevo-pedido"
          className={({ isActive }) =>
            isActive ? "sidebar-link active" : "sidebar-link"
          }
        >
          <span>＋</span>
          Nuevo pedido
        </NavLink>

        <div className="sidebar-section-title">
          MI CUENTA
        </div>

        <span className="sidebar-link disabled">
          <span>♙</span>
          Mi perfil
        </span>
      </nav>
    </aside>
  );
}

export default ClienteSidebar;