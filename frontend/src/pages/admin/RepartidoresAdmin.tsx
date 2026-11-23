
import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import {
  obtenerRepartidores,
  crearRepartidor,
  activarDisponibilidadRepartidor,
  desactivarDisponibilidadRepartidor,
  type RepartidorAdminResponse,
} from "../../api/repartidoresApi";

import "./RepartidoresAdmin.css";

function RepartidoresAdmin() {
  const [repartidores, setRepartidores] = useState<RepartidorAdminResponse[]>([]);

  const [nombre, setNombre] = useState("");
  const [apellido, setApellido] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [repartidorProcesando, setRepartidorProcesando] = useState<number | null>(null);

  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");

  const cargarRepartidores = async () => {
    try {
      setError("");
      const resultado = await obtenerRepartidores();
      setRepartidores(resultado);
    } catch (error) {
      console.error("Error al cargar repartidores:", error);
      setError("No se pudieron cargar los repartidores.");
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarRepartidores();
  }, []);

  const handleCrearRepartidor = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();
    setError("");
    setMensaje("");

    if (password.length < 8 || password.length > 72) {
      setError("La contraseña debe tener entre 8 y 72 caracteres.");
      return;
    }

    try {
      setGuardando(true);

      await crearRepartidor({
        nombre: nombre.trim(),
        apellido: apellido.trim(),
        email: email.trim(),
        password,
        rol: "REPARTIDOR",
      });

      setMensaje("Repartidor registrado correctamente.");
      setNombre("");
      setApellido("");
      setEmail("");
      setPassword("");

      await cargarRepartidores();
    } catch (error) {
      console.error("Error al crear repartidor:", error);
      setError(
        "No se pudo registrar el repartidor. Verificá los datos y que el email no esté registrado."
      );
    } finally {
      setGuardando(false);
    }
  };

  const handleCambiarDisponibilidad = async (
    repartidor: RepartidorAdminResponse
  ) => {
    setError("");
    setMensaje("");

    try {
      setRepartidorProcesando(repartidor.id);

      if (repartidor.disponible) {
        await desactivarDisponibilidadRepartidor(repartidor.id);
        setMensaje("Disponibilidad desactivada correctamente.");
      } else {
        await activarDisponibilidadRepartidor(repartidor.id);
        setMensaje("Disponibilidad activada correctamente.");
      }

      await cargarRepartidores();
    } catch (error) {
      console.error("Error al cambiar disponibilidad:", error);
      setError(
        "No se pudo cambiar la disponibilidad. Verificá que la cuenta del repartidor esté activa y que no tenga una ruta en curso."
      );
    } finally {
      setRepartidorProcesando(null);
    }
  };

  return (
    <div className="admin-repartidores">
      <header className="admin-repartidores-header">
        <div>
          <h1>Gestión de repartidores</h1>
          <p>Registrar y consultar repartidores de la plataforma.</p>
        </div>

        <button type="button" onClick={cargarRepartidores}>
          Actualizar listado
        </button>
      </header>

      {mensaje && <p className="mensaje-exito">{mensaje}</p>}
      {error && <p className="mensaje-error">{error}</p>}

      <section className="admin-repartidores-formulario">
        <h2>Registrar nuevo repartidor</h2>

        <form onSubmit={handleCrearRepartidor}>
          <div>
            <label htmlFor="nombre">Nombre</label>
            <input
              id="nombre"
              type="text"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              maxLength={100}
              required
            />
          </div>

          <div>
            <label htmlFor="apellido">Apellido</label>
            <input
              id="apellido"
              type="text"
              value={apellido}
              onChange={(e) => setApellido(e.target.value)}
              maxLength={100}
              required
            />
          </div>

          <div>
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              maxLength={254}
              required
            />
          </div>

          <div>
            <label htmlFor="password">Contraseña inicial</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={8}
              maxLength={72}
              required
            />
          </div>

          <button type="submit" disabled={guardando}>
            {guardando ? "Registrando..." : "Registrar repartidor"}
          </button>
        </form>
      </section>

      <section className="admin-repartidores-listado">
        <h2>Repartidores registrados</h2>

        {cargando ? (
          <p>Cargando repartidores...</p>
        ) : repartidores.length === 0 ? (
          <p>No hay repartidores registrados.</p>
        ) : (
          <div className="admin-repartidores-tabla">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre</th>
                  <th>Email</th>
                  <th>Teléfono</th>
                  <th>Cuenta</th>
                  <th>Disponibilidad</th>
                  <th>Acciones</th>
                </tr>
              </thead>

              <tbody>
                {repartidores.map((repartidor) => (
                  <tr key={repartidor.id}>
                    <td>{repartidor.id}</td>
                    <td>
                      {repartidor.nombre} {repartidor.apellido}
                    </td>
                    <td>{repartidor.email}</td>
                    <td>{repartidor.telefono ?? "Sin teléfono"}</td>
                    <td>
                      {repartidor.activo ? "Activa" : "Inactiva"}
                    </td>
                    <td>
                      {repartidor.disponible ? "Disponible" : "No disponible"}
                    </td>
                    <td>
                      <button
                        type="button"
                        disabled={
                          !repartidor.activo ||
                          repartidorProcesando === repartidor.id
                        }
                        onClick={() =>
                          handleCambiarDisponibilidad(repartidor)
                        }
                      >
                        {repartidorProcesando === repartidor.id
                          ? "Procesando..."
                          : repartidor.disponible
                            ? "Desactivar"
                            : "Activar"}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default RepartidoresAdmin;
