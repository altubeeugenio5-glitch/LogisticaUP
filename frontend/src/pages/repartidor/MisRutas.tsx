
import { useEffect, useState } from "react";

import {
  obtenerMisRutas,
  confirmarEntrega,
  iniciarRuta,
  completarRuta,
  type RutaResponse,
} from "../../api/rutasApi";

import MapaRuta from "../../components/maps/MapaRuta";

import "./MisRutas.css";

function MisRutas() {
  const [rutas, setRutas] = useState<RutaResponse[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const manejarConfirmarEntrega = async (
    rutaId: number,
    pedidoId: number
  ) => {
    try {
      await confirmarEntrega(rutaId, pedidoId);

      const resultado = await obtenerMisRutas();
      setRutas(resultado);
    } catch (err) {
      console.error("Error al confirmar entrega:", err);
      alert("No se pudo confirmar la entrega.");
    }
  };
  const manejarIniciarRuta = async (rutaId: number) => {
    try {
      await iniciarRuta(rutaId);

      const resultado = await obtenerMisRutas();
      setRutas(resultado);
    } catch (err) {
      console.error("Error al iniciar la ruta:", err);
      alert("No se pudo iniciar la ruta.");
    }
  };
  const manejarCompletarRuta = async (rutaId: number) => {
    try {
      await completarRuta(rutaId);

      const resultado = await obtenerMisRutas();
      setRutas(resultado);
    } catch (err) {
      console.error("Error al completar la ruta:", err);
      alert("No se pudo completar la ruta. Verificá que todos los pedidos estén entregados.");
    }
  };
  useEffect(() => {
    const cargarRutas = async () => {
      try {
        const resultado = await obtenerMisRutas();
        setRutas(resultado);
      } catch (err) {
        console.error("Error al cargar mis rutas:", err);
        setError("No se pudieron cargar tus rutas.");
      } finally {
        setCargando(false);
      }
    };

    cargarRutas();
  }, []);

  if (cargando) {
    return <p>Cargando tus rutas...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <main className="mis-rutas">
      <h1>Mis rutas asignadas</h1>

      {rutas.length === 0 ? (
        <p>No tenés rutas asignadas actualmente.</p>
      ) : (
        rutas.map((ruta) => (
          <section className="ruta-repartidor" key={ruta.id}>
            <h2>{ruta.nombre}</h2>

            <p>
              <strong>Estado:</strong> {ruta.estado}
            </p>
            {ruta.estado === "OPTIMIZADA" && (
              <button
                onClick={() => manejarIniciarRuta(ruta.id)}
              >
                Iniciar ruta
              </button>
            )}
            {ruta.estado === "EN_CURSO" && (
                <button
                  onClick={() => manejarCompletarRuta(ruta.id)}
                  disabled={ruta.pedidos.length === 0 ||
                    ruta.pedidos.some((pedido) => pedido.estado !== "ENTREGADO")}
                >
                  Completar ruta
                </button>
            )}
            <p>
              <strong>Pedidos asignados:</strong>{" "}
              {ruta.pedidosIds.length}
            </p>

            <h3>Pedidos de la ruta</h3>

          <div className="pedidos-ruta">
            {ruta.pedidos.map((pedido) => (
              <div className="pedido-ruta" key={pedido.pedidoId}>
                <p>
                  <strong>Pedido #{pedido.pedidoId}</strong>
                </p>

                <p>
                  Estado: {pedido.estado}
                </p>

                {pedido.estado === "EN_CAMINO" && (
                  <button
                    onClick={() =>
                      manejarConfirmarEntrega(ruta.id, pedido.pedidoId)
                    }
                  >
                    Confirmar entrega
                  </button>
                )}

                {pedido.estado === "ENTREGADO" && (
                  <p>
                    Entregado:{" "}
                    {pedido.fechaEntrega
                      ? new Date(pedido.fechaEntrega).toLocaleString("es-AR")
                      : "Fecha no disponible"}
                  </p>
                )}
              </div>
            ))}
          </div>

          <h3>Recorrido</h3>

          <MapaRuta paradas={ruta.paradas} />
          </section>
        ))
      )}
    </main>
  );
}

export default MisRutas;
