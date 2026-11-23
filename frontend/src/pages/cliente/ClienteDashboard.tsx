
import { useEffect, useState } from "react";

import {
  obtenerMisPedidos,
  obtenerEstadoPedido,
  type PedidoClienteResponse,
  type EstadoPedidoResponse,
} from "../../api/pedidosApi";

const etapas = [
  "PENDIENTE",
  "CONFIRMADO",
  "EN_PREPARACION",
  "ASIGNADO_A_RUTA",
  "EN_CAMINO",
  "ENTREGADO",
];

function ClienteDashboard() {
  const [pedidos, setPedidos] = useState<PedidoClienteResponse[]>([]);
  const [seguimientos, setSeguimientos] = useState<
    Record<number, EstadoPedidoResponse>
  >({});
  const [pedidoSeleccionado, setPedidoSeleccionado] = useState<number | null>(
    null
  );

  const [cargando, setCargando] = useState(true);
  const [cargandoSeguimiento, setCargandoSeguimiento] = useState(false);
  const [error, setError] = useState("");
  const [errorSeguimiento, setErrorSeguimiento] = useState("");

  useEffect(() => {
    const cargarPedidos = async () => {
      try {
        const datos = await obtenerMisPedidos();
        setPedidos(datos);
      } catch {
        setError("No se pudieron cargar tus pedidos.");
      } finally {
        setCargando(false);
      }
    };

    cargarPedidos();
  }, []);

  const consultarSeguimiento = async (pedidoId: number) => {
    if (pedidoSeleccionado === pedidoId) {
      setPedidoSeleccionado(null);
      return;
    }

    setPedidoSeleccionado(pedidoId);
    setErrorSeguimiento("");
    setCargandoSeguimiento(true);

    try {
      const resultado = await obtenerEstadoPedido(pedidoId);

      setSeguimientos((anteriores) => ({
        ...anteriores,
        [pedidoId]: resultado,
      }));
    } catch {
      setErrorSeguimiento("No se pudo cargar el seguimiento del pedido.");
    } finally {
      setCargandoSeguimiento(false);
    }
  };

  const pendientes = pedidos.filter(
    (pedido) =>
      pedido.estado === "PENDIENTE" ||
      pedido.estado === "CONFIRMADO"
  ).length;

  const enPreparacion = pedidos.filter(
    (pedido) =>
      pedido.estado === "EN_PREPARACION" ||
      pedido.estado === "ASIGNADO_A_RUTA"
  ).length;

  const enCamino = pedidos.filter(
    (pedido) => pedido.estado === "EN_CAMINO"
  ).length;

  const obtenerClaseEstado = (estado: string) => {
    switch (estado) {
      case "PENDIENTE":
        return "estado-pendiente";
      case "CONFIRMADO":
        return "estado-confirmado";
      case "EN_PREPARACION":
        return "estado-preparacion";
      case "ASIGNADO_A_RUTA":
        return "estado-asignado";
      case "EN_CAMINO":
        return "estado-en-camino";
      case "ENTREGADO":
        return "estado-entregado";
      case "CANCELADO":
        return "estado-cancelado";
      default:
        return "";
    }
  };

  const mostrarEstado = (estado: string) =>
    estado.split("_").join(" ");

  return (
    <main className="cliente-main">
      <h2>Resumen de actividad</h2>

      <section className="cliente-resumen">
        <div className="cliente-card">
          <h3>Pedidos pendientes</h3>
          <p>{cargando ? "..." : pendientes}</p>
        </div>

        <div className="cliente-card">
          <h3>En preparación</h3>
          <p>{cargando ? "..." : enPreparacion}</p>
        </div>

        <div className="cliente-card">
          <h3>En camino</h3>
          <p>{cargando ? "..." : enCamino}</p>
        </div>
      </section>

      <section className="cliente-pedidos">
        <h2>Mis pedidos</h2>

        {cargando && <p>Cargando pedidos...</p>}

        {error && <p>{error}</p>}

        {!cargando && !error && pedidos.length === 0 && (
          <p>Todavía no tenés pedidos registrados.</p>
        )}

        {!cargando && !error && pedidos.length > 0 && (
          <div className="cliente-listado-pedidos">
            {pedidos.map((pedido) => {
              const seguimiento = seguimientos[pedido.pedidoId];
              const seleccionado =
                pedidoSeleccionado === pedido.pedidoId;

              return (
                <article key={pedido.pedidoId} className="cliente-card">
                  <h3>Pedido #{pedido.pedidoId}</h3>

                  <p>
                    Estado:{" "}
                    <span
                      className={`estado-pedido ${obtenerClaseEstado(pedido.estado)}`}
                    >
                      {mostrarEstado(pedido.estado)}
                    </span>
                  </p>

                  <p>
                    Fecha:{" "}
                    {new Date(pedido.fechaCreacion).toLocaleDateString("es-AR")}
                  </p>

                  <p>
                    Total: ${pedido.total.toLocaleString("es-AR")}
                  </p>

                  <button
                    type="button"
                    className="boton-seguimiento"
                    onClick={() => consultarSeguimiento(pedido.pedidoId)}
                  >
                    {seleccionado
                      ? "Ocultar seguimiento"
                      : "Ver seguimiento"}
                  </button>

                  {seleccionado && (
                    <section className="seguimiento-pedido">
                      <h4>Seguimiento del pedido</h4>

                      {cargandoSeguimiento && (
                        <p>Consultando estado...</p>
                      )}

                      {errorSeguimiento && (
                        <p>{errorSeguimiento}</p>
                      )}
                    
                      {!cargandoSeguimiento && seguimiento && (
                        <>
                          {seguimiento.estado === "CANCELADO" ? (
                            <p>Este pedido fue cancelado.</p>
                          ) : (
                            <>
                              <div className="seguimiento-progreso">
                                <div className="seguimiento-progreso-info">
                                  <strong>Progreso del pedido</strong>
                                  <span>
                                    {etapas.indexOf(seguimiento.estado) + 1} de {etapas.length} etapas
                                  </span>
                                </div>

                                <div className="seguimiento-barra">
                                  <div
                                    className="seguimiento-barra-avance"
                                    style={{
                                      width: `${
                                        ((etapas.indexOf(seguimiento.estado) + 1) /
                                          etapas.length) *
                                        100
                                      }%`,
                                    }}
                                  />
                                </div>
                              </div>

                              <ol>
                                {etapas.map((etapa) => {
                                  const indiceActual = etapas.indexOf(seguimiento.estado);
                                  const indiceEtapa = etapas.indexOf(etapa);

                                  const completada = indiceActual >= indiceEtapa;

                                  return (
                                    <li
                                      key={etapa}
                                      className={
                                        completada
                                          ? "etapa-completada"
                                          : "etapa-pendiente"
                                      }
                                    >
                                      <strong>{mostrarEstado(etapa)}</strong>
                                      {" - "}
                                      {completada ? "Completado" : "Pendiente"}
                                    </li>
                                  );
                                })}
                              </ol>
                            </>
                          )}
                        </>
                      )}
                    </section>
                  )}
                </article>
              );
            })}
          </div>
        )}
      </section>
    </main>
  );
}

export default ClienteDashboard;
