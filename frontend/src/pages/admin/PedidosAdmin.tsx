
import { useEffect, useState } from "react";

import {
  obtenerPedidosAdmin,
  actualizarEstadoPedidoAdmin,
  type PedidoAdminResponse,
} from "../../api/pedidosApi";

function PedidosAdmin() {
  const [pedidos, setPedidos] = useState<PedidoAdminResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [pedidoSeleccionado, setPedidoSeleccionado] =
    useState<number | null>(null);
  const [pedidoProcesando, setPedidoProcesando] =
    useState<number | null>(null);

  const cargarPedidos = async () => {
    try {
      const resultado = await obtenerPedidosAdmin();
      setPedidos(resultado);
      setError("");
    } catch (err) {
      console.error("Error al cargar pedidos:", err);
      setError("No se pudieron cargar los pedidos.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarPedidos();
  }, []);

  const gestionarEstado = async (
    pedidoId: number,
    nuevoEstado: string
  ) => {
    try {
      setPedidoProcesando(pedidoId);
      setMensaje("");
      setError("");

      await actualizarEstadoPedidoAdmin(pedidoId, nuevoEstado);

      await cargarPedidos();

      setMensaje(
        nuevoEstado === "CONFIRMADO"
          ? `El pedido #${pedidoId} fue confirmado.`
          : `El pedido #${pedidoId} pasó a preparación.`
      );
    } catch (err) {
      console.error("Error al actualizar pedido:", err);
      setError("No se pudo actualizar el estado del pedido.");
    } finally {
      setPedidoProcesando(null);
    }
  };

  const mostrarEstado = (estado: string) =>
    estado.replaceAll("_", " ");

  return (
    <main className="cliente-main">
      <h1>Administración de pedidos</h1>

      <section className="cliente-pedidos">
        <h2>Todos los pedidos</h2>

        {loading && <p>Cargando pedidos...</p>}

        {error && <p>{error}</p>}

        {mensaje && <p>{mensaje}</p>}

        {!loading && !error && pedidos.length === 0 && (
          <p>No hay pedidos registrados.</p>
        )}

        {!loading && pedidos.length > 0 && (
          <div>
            {pedidos.map((pedido) => {
              const seleccionado =
                pedidoSeleccionado === pedido.pedidoId;

              const procesando =
                pedidoProcesando === pedido.pedidoId;

              return (
                <article key={pedido.pedidoId}>
                  <h3>Pedido #{pedido.pedidoId}</h3>

                  <p>
                    Cliente: {pedido.nombreCliente}{" "}
                    {pedido.apellidoCliente}
                  </p>

                  <p>Email: {pedido.emailCliente}</p>

                  <p>
                    Teléfono: {pedido.telefonoCliente || "No registrado"}
                  </p>

                  <p>
                    Fecha:{" "}
                    {new Date(pedido.fechaCreacion).toLocaleString("es-AR")}
                  </p>

                  <p>Estado: {mostrarEstado(pedido.estado)}</p>

                  <p>
                    Total: ${pedido.total.toLocaleString("es-AR")}
                  </p>

                  <p>
                    Ruta: {pedido.rutaId ?? "Sin asignar"}
                  </p>

                  {pedido.estado === "PENDIENTE" && (
                    <button
                      type="button"
                      disabled={procesando}
                      onClick={() =>
                        gestionarEstado(pedido.pedidoId, "CONFIRMADO")
                      }
                    >
                      {procesando ? "Procesando..." : "Confirmar pedido"}
                    </button>
                  )}

                  {pedido.estado === "CONFIRMADO" && (
                    <button
                      type="button"
                      disabled={procesando}
                      onClick={() =>
                        gestionarEstado(
                          pedido.pedidoId,
                          "EN_PREPARACION"
                        )
                      }
                    >
                      {procesando
                        ? "Procesando..."
                        : "Iniciar preparación"}
                    </button>
                  )}

                  <button
                    type="button"
                    onClick={() =>
                      setPedidoSeleccionado(
                        seleccionado ? null : pedido.pedidoId
                      )
                    }
                  >
                    {seleccionado ? "Ocultar detalles" : "Ver detalles"}
                  </button>

                  {seleccionado && (
                    <section>
                      <h4>Productos del pedido</h4>

                      {pedido.detalles.map((detalle) => (
                        <article key={detalle.productoId}>
                          <p>
                            <strong>{detalle.nombreProducto}</strong>
                          </p>

                          <p>Código: {detalle.codigoProducto}</p>
                          <p>Cantidad: {detalle.cantidad}</p>

                          <p>
                            Precio unitario: $
                            {detalle.precioUnitario.toLocaleString("es-AR")}
                          </p>

                          <p>
                            Subtotal: $
                            {detalle.subtotal.toLocaleString("es-AR")}
                          </p>
                        </article>
                      ))}
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

export default PedidosAdmin;
