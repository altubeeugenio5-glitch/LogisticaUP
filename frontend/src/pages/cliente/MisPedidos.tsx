
import { useEffect, useState } from "react";

import {
  obtenerMisPedidos,
  obtenerEstadoPedido,
  obtenerDetallesPedidoCliente,
  obtenerRutaPedido,
  type PedidoClienteResponse,
  type EstadoPedidoResponse,
  type PedidoDetalleClienteResponse,
} from "../../api/pedidosApi";

import type { RutaResponse } from "../../api/rutasApi";

import "./MisPedidos.css";

import MapaRuta from "../../components/maps/MapaRuta";

const etapas = [
  "PENDIENTE",
  "CONFIRMADO",
  "EN_PREPARACION",
  "ASIGNADO_A_RUTA",
  "EN_CAMINO",
  "ENTREGADO",
];

function MisPedidos() {
  const [pedidos, setPedidos] = useState<PedidoClienteResponse[]>([]);

  const [seguimientos, setSeguimientos] = useState<
    Record<number, EstadoPedidoResponse>
  >({});

  const [detalles, setDetalles] = useState<
    Record<number, PedidoDetalleClienteResponse[]>
  >({});

  const [pedidoSeleccionado, setPedidoSeleccionado] = useState<number | null>(
    null
  );

  const [detalleSeleccionado, setDetalleSeleccionado] = useState<number | null>(
    null
  );

  const [loading, setLoading] = useState(true);
  const [cargandoSeguimiento, setCargandoSeguimiento] = useState(false);
  const [cargandoDetalle, setCargandoDetalle] = useState(false);

  const [error, setError] = useState("");
  const [errorSeguimiento, setErrorSeguimiento] = useState("");
  const [errorDetalle, setErrorDetalle] = useState("");

  useEffect(() => {
    const cargarPedidos = async () => {
      try {
        const resultado = await obtenerMisPedidos();
        setPedidos(resultado);
      } catch (err) {
        console.error("Error al cargar pedidos:", err);
        setError("No se pudieron cargar tus pedidos.");
      } finally {
        setLoading(false);
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
    setErrorRuta("");
    setCargandoSeguimiento(true);
    setCargandoRuta(true);

    try {
      const resultado = await obtenerEstadoPedido(pedidoId);

      setSeguimientos((anteriores) => ({
        ...anteriores,
        [pedidoId]: resultado,
      }));
    } catch (err) {
      console.error("Error al consultar seguimiento:", err);
      setErrorSeguimiento("No se pudo cargar el seguimiento del pedido.");
    } finally {
      setCargandoSeguimiento(false);
    }

    try {
      const ruta = await obtenerRutaPedido(pedidoId);

      setRutas((anteriores) => ({
        ...anteriores,
        [pedidoId]: ruta,
      }));
    } catch (err) {
      console.error("Error al consultar ruta:", err);
      setErrorRuta("Todavía no hay una ruta disponible para este pedido.");
    } finally {
      setCargandoRuta(false);
    }
  };

  const consultarDetalle = async (pedidoId: number) => {
    if (detalleSeleccionado === pedidoId) {
      setDetalleSeleccionado(null);
      return;
    }

    setDetalleSeleccionado(pedidoId);
    setErrorDetalle("");

    if (detalles[pedidoId]) {
      return;
    }

    setCargandoDetalle(true);

    try {
      const resultado = await obtenerDetallesPedidoCliente(pedidoId);

      setDetalles((anteriores) => ({
        ...anteriores,
        [pedidoId]: resultado,
      }));
    } catch (err) {
      console.error("Error al consultar detalle:", err);
      setErrorDetalle("No se pudo cargar el detalle del pedido.");
    } finally {
      setCargandoDetalle(false);
    }
  };
  const [rutas, setRutas] = useState<Record<number, RutaResponse>>({});
  const [cargandoRuta, setCargandoRuta] = useState(false);
  const [errorRuta, setErrorRuta] = useState("");
  const mostrarEstado = (estado: string) =>
    estado.split("_").join(" ");

  const formatearPrecio = (precio: number) =>
    precio.toLocaleString("es-AR", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });

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

  return (
    <main className="cliente-main mis-pedidos-contenido">
      <h1>Mis pedidos</h1>

      <section className="cliente-pedidos">
        <h2>Historial de pedidos</h2>

        {loading && <p>Cargando pedidos...</p>}

        {error && <p>{error}</p>}

        {!loading && !error && pedidos.length === 0 && (
          <p>Todavía no tenés pedidos registrados.</p>
        )}

        {!loading && !error && pedidos.length > 0 && (
          <div className="mis-pedidos-listado">
            {pedidos.map((pedido) => {
              const seguimiento = seguimientos[pedido.pedidoId];
              const ruta = rutas[pedido.pedidoId];
              console.log("Ruta del pedido", pedido.pedidoId, ruta);
              const seleccionado =
                pedidoSeleccionado === pedido.pedidoId;

              const detalle = detalles[pedido.pedidoId];
              const detalleAbierto =
                detalleSeleccionado === pedido.pedidoId;

              return (
                <article
                  key={pedido.pedidoId}
                  className="mis-pedido-card"
                >
                  <h3>Pedido #{pedido.pedidoId}</h3>

                  <p>
                    Fecha:{" "}
                    {new Date(pedido.fechaCreacion).toLocaleString("es-AR")}
                  </p>

                  <p>
                    Estado:{" "}
                    <span
                      className={`estado-pedido ${obtenerClaseEstado(pedido.estado)}`}
                    >
                      {mostrarEstado(pedido.estado)}
                    </span>
                  </p>

                  <p>
                    Total: ${formatearPrecio(pedido.total)}
                  </p>

                  <button
                    type="button"
                    className="boton-seguimiento"
                    onClick={() => consultarDetalle(pedido.pedidoId)}
                  >
                    {detalleAbierto ? "Ocultar detalle" : "Ver detalle"}
                  </button>

                  <button
                    type="button"
                    className="boton-seguimiento"
                    onClick={() => consultarSeguimiento(pedido.pedidoId)}
                  >
                    {seleccionado
                      ? "Ocultar seguimiento"
                      : "Ver seguimiento"}
                  </button>

                  {detalleAbierto && (
                    <section className="detalle-pedido">
                      <h4>Productos del pedido</h4>

                      {cargandoDetalle && (
                        <p>Cargando productos...</p>
                      )}

                      {errorDetalle && (
                        <p>{errorDetalle}</p>
                      )}

                      {!cargandoDetalle && detalle && (
                        <>
                          {detalle.length === 0 ? (
                            <p>Este pedido no tiene productos registrados.</p>
                          ) : (
                            <div className="detalle-pedido-lista">
                              {detalle.map((producto) => (
                                <div
                                  key={producto.productoId}
                                  className="detalle-pedido-producto"
                                >
                                  <h5>{producto.nombreProducto}</h5>

                                  <p>
                                    Código: {producto.codigoProducto}
                                  </p>

                                  <p>
                                    Cantidad: {producto.cantidad}
                                  </p>

                                  <p>
                                    Precio unitario: $
                                    {formatearPrecio(producto.precioUnitario)}
                                  </p>

                                  <p>
                                    Subtotal: $
                                    {formatearPrecio(producto.subtotal)}
                                  </p>
                                </div>
                              ))}
                            </div>
                          )}
                        </>
                      )}
                    </section>
                  )}

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
                                    {etapas.indexOf(seguimiento.estado) + 1} de{" "}
                                    {etapas.length} etapas
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
                                  const indiceActual = etapas.indexOf(
                                    seguimiento.estado
                                  );

                                  const indiceEtapa = etapas.indexOf(etapa);

                                  const completada =
                                    indiceActual >= indiceEtapa;

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
                                      {completada
                                        ? "Completado"
                                        : "Pendiente"}
                                    </li>
                                  );
                                })}
                              </ol>
                              <div className="seguimiento-mapa">
                                <h4>Ubicación y recorrido del pedido</h4>

                                {cargandoRuta && <p>Cargando recorrido...</p>}

                                {errorRuta && <p>{errorRuta}</p>}

                                {!cargandoRuta && ruta && (
                                  <MapaRuta paradas={ruta.paradas} />
                                )}
                              </div>
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

export default MisPedidos;
