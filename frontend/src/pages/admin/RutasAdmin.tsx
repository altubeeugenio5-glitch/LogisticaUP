
import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import {
  obtenerRutas,
  crearRuta,
  asignarRepartidor,
  desasignarRepartidor,
  asignarPedido,
  retirarPedido,
  optimizarRuta,
  type RutaResponse,
  type CrearRutaRequest,
} from "../../api/rutasApi";

import {
  obtenerRepartidoresDisponibles,
  type RepartidorAdminResponse,
} from "../../api/repartidoresApi";

import {
  obtenerPedidosAdmin,
  type PedidoAdminResponse,
} from "../../api/pedidosApi";

import "./RutasAdmin.css";
import MapaRuta from "../../components/maps/MapaRuta";

function RutasAdmin() {
  const [rutas, setRutas] = useState<RutaResponse[]>([]);
  const [repartidores, setRepartidores] = useState<
    RepartidorAdminResponse[]
  >([]);
  const [pedidos, setPedidos] = useState<PedidoAdminResponse[]>([]);

  const [repartidorSeleccionado, setRepartidorSeleccionado] = useState<
    Record<number, string>
  >({});

  const [pedidoSeleccionado, setPedidoSeleccionado] = useState<
    Record<number, string>
  >({});

  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");

  const [nombre, setNombre] = useState("");

  const [origenDireccion, setOrigenDireccion] = useState("");
  const [origenLongitud, setOrigenLongitud] = useState("");
  const [origenLatitud, setOrigenLatitud] = useState("");

  const [entregas, setEntregas] = useState([
    {
      id: crypto.randomUUID(),
      direccion: "",
      longitud: "",
      latitud: "",
    },
  ]);

  const [destinoDireccion, setDestinoDireccion] = useState("");
  const [destinoLongitud, setDestinoLongitud] = useState("");
  const [destinoLatitud, setDestinoLatitud] = useState("");

  const [guardando, setGuardando] = useState(false);

  const estadosAsignables = [
    "PLANIFICADA",
    "PENDIENTE_OPTIMIZACION",
    "OPTIMIZADA",
  ];

  const pedidosDisponibles = pedidos.filter(
    (pedido) =>
      pedido.rutaId === null &&
      ["CONFIRMADO", "EN_PREPARACION"].includes(pedido.estado)
  );

  const cargarRutas = async () => {
    try {
      const resultado = await obtenerRutas();
      setRutas(resultado);
    } catch (error) {
      console.error("Error al cargar las rutas:", error);
      setError("No se pudieron cargar las rutas.");
    }
  };

  const cargarRepartidores = async () => {
    try {
      const resultado = await obtenerRepartidoresDisponibles();

      setRepartidores(
        resultado.filter((repartidor) => repartidor.activo)
      );
    } catch (error) {
      console.error("Error al cargar repartidores:", error);
    }
  };

  const cargarPedidos = async () => {
    try {
      const resultado = await obtenerPedidosAdmin();
      setPedidos(resultado);
    } catch (error) {
      console.error("Error al cargar los pedidos:", error);
      setError("No se pudieron cargar los pedidos.");
    }
  };

  const actualizarDatos = async () => {
    setError("");

    await Promise.all([
      cargarRutas(),
      cargarRepartidores(),
      cargarPedidos(),
    ]);

    setCargando(false);
  };

  useEffect(() => {
    actualizarDatos();
  }, []);

  const handleCrearRuta = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setError("");
    setMensaje("");

    const request: CrearRutaRequest = {
      nombre: nombre.trim(),
      paradas: [
        {
          orden: 1,
          tipo: "ORIGEN",
          direccion: origenDireccion.trim(),
          longitud: Number(origenLongitud),
          latitud: Number(origenLatitud),
        },
        ...entregas.map((entrega, index) => ({
          orden: index + 2,
          tipo: "ENTREGA" as const,
          direccion: entrega.direccion.trim(),
          longitud: Number(entrega.longitud),
          latitud: Number(entrega.latitud),
        })),
        {
          orden: entregas.length + 2,
          tipo: "DESTINO",
          direccion: destinoDireccion.trim(),
          longitud: Number(destinoLongitud),
          latitud: Number(destinoLatitud),
        },
      ],
    };

    try {
      setGuardando(true);

      await crearRuta(request);

      setMensaje("La ruta se creó correctamente.");

      setNombre("");
      setOrigenDireccion("");
      setOrigenLongitud("");
      setOrigenLatitud("");
      setEntregas([
        {
          id: crypto.randomUUID(),
          direccion: "",
          longitud: "",
          latitud: "",
        },
      ]);
      setDestinoDireccion("");
      setDestinoLongitud("");
      setDestinoLatitud("");

      await cargarRutas();
    } catch (error) {
      console.error("Error al crear la ruta:", error);
      setError("No se pudo crear la ruta. Revisá los datos ingresados.");
    } finally {
      setGuardando(false);
    }
  };

  const handleAsignarPedido = async (rutaId: number) => {
    const pedidoId = Number(pedidoSeleccionado[rutaId]);

    if (!pedidoId) {
      setError("Seleccioná un pedido para asignar.");
      return;
    }

    try {
      setError("");
      setMensaje("");

      await asignarPedido(rutaId, pedidoId);

      setMensaje(
        `El pedido ${pedidoId} se asignó correctamente a la ruta ${rutaId}.`
      );

      setPedidoSeleccionado((anterior) => ({
        ...anterior,
        [rutaId]: "",
      }));

      await Promise.all([cargarRutas(), cargarPedidos()]);
    } catch (error) {
      console.error("Error al asignar el pedido:", error);
      setError("No se pudo asignar el pedido a la ruta.");
    }
  };

  const handleRetirarPedido = async (
    rutaId: number,
    pedidoId: number
  ) => {
    try {
      setError("");
      setMensaje("");

      await retirarPedido(rutaId, pedidoId);

      setMensaje(
        `El pedido ${pedidoId} se retiró correctamente de la ruta ${rutaId}.`
      );

      await Promise.all([cargarRutas(), cargarPedidos()]);
    } catch (error) {
      console.error("Error al retirar el pedido:", error);
      setError("No se pudo retirar el pedido de la ruta.");
    }
  };

  return (
    <main className="admin-rutas">
      <div className="admin-rutas-header">
        <div>
          <h2>Gestión de rutas</h2>
          <p>Administrá recorridos, repartidores y pedidos.</p>
        </div>

        <button onClick={actualizarDatos}>
          Actualizar
        </button>
      </div>

      <section className="admin-rutas-contenido">
        <h3>Crear nueva ruta</h3>

        <form onSubmit={handleCrearRuta}>
          <label>Nombre de la ruta</label>
          <input
            type="text"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="Ejemplo: Ruta zona norte"
            maxLength={120}
            required
          />

          <h4>Parada 1: Origen</h4>

          <label>Dirección del origen</label>
          <input
            value={origenDireccion}
            onChange={(e) => setOrigenDireccion(e.target.value)}
            placeholder="Depósito - San Nicolás"
            required
          />

          <label>Latitud</label>
          <input
            type="number"
            step="any"
            min="-90"
            max="90"
            value={origenLatitud}
            onChange={(e) => setOrigenLatitud(e.target.value)}
            required
          />

          <label>Longitud</label>
          <input
            type="number"
            step="any"
            min="-180"
            max="180"
            value={origenLongitud}
            onChange={(e) => setOrigenLongitud(e.target.value)}
            required
          />
          <h4>Paradas de entrega</h4>

          {entregas.map((entrega, index) => (
            <div key={entrega.id}>
              <h4>Parada {index + 2}: Entrega</h4>

              <label>Dirección de entrega</label>
              <input
                value={entrega.direccion}
                onChange={(e) =>
                  setEntregas((anteriores) =>
                    anteriores.map((item) =>
                      item.id === entrega.id
                        ? { ...item, direccion: e.target.value }
                        : item
                    )
                  )
                }
                placeholder="Dirección del cliente"
                required
              />

              <label>Latitud</label>
              <input
                type="number"
                step="any"
                min="-90"
                max="90"
                value={entrega.latitud}
                onChange={(e) =>
                  setEntregas((anteriores) =>
                    anteriores.map((item) =>
                      item.id === entrega.id
                        ? { ...item, latitud: e.target.value }
                        : item
                    )
                  )
                }
                required
              />

              <label>Longitud</label>
              <input
                type="number"
                step="any"
                min="-180"
                max="180"
                value={entrega.longitud}
                onChange={(e) =>
                  setEntregas((anteriores) =>
                    anteriores.map((item) =>
                      item.id === entrega.id
                        ? { ...item, longitud: e.target.value }
                        : item
                    )
                  )
                }
                required
              />

              <button
                type="button"
                disabled={entregas.length === 1}
                onClick={() =>
                  setEntregas((anteriores) =>
                    anteriores.length > 1
                      ? anteriores.filter((item) => item.id !== entrega.id)
                      : anteriores
                  )
                }
              >
                Eliminar entrega
              </button>
            </div>
          ))}

          <button
            type="button"
            onClick={() =>
              setEntregas((anteriores) => [
                ...anteriores,
                {
                  id: crypto.randomUUID(),
                  direccion: "",
                  longitud: "",
                  latitud: "",
                },
              ])
            }
          >
            + Agregar otra entrega
          </button>
          <h4>Destino final</h4>

          <label>Dirección del destino</label>
          <input
            value={destinoDireccion}
            onChange={(e) => setDestinoDireccion(e.target.value)}
            placeholder="Destino final"
            required
          />

          <label>Latitud</label>
          <input
            type="number"
            step="any"
            min="-90"
            max="90"
            value={destinoLatitud}
            onChange={(e) => setDestinoLatitud(e.target.value)}
            required
          />

          <label>Longitud</label>
          <input
            type="number"
            step="any"
            min="-180"
            max="180"
            value={destinoLongitud}
            onChange={(e) => setDestinoLongitud(e.target.value)}
            required
          />
          <button type="submit" disabled={guardando}>
            {guardando ? "Creando ruta..." : "Crear ruta"}
          </button>
        </form>

        {mensaje && <p>{mensaje}</p>}
        {error && <p>{error}</p>}
      </section>

      <section className="admin-rutas-contenido">
        <h3>Rutas registradas</h3>

        {cargando && <p>Cargando rutas...</p>}

        {!cargando && rutas.length === 0 && (
          <p>Todavía no hay rutas registradas.</p>
        )}

        {rutas.map((ruta) => {
          const permiteModificar = estadosAsignables.includes(ruta.estado);

          return (
            <article className="admin-ruta-card" key={ruta.id}>
              <h4>
                Ruta #{ruta.id} - {ruta.nombre}
              </h4>

              <p>
                <strong>Estado:</strong> {ruta.estado}
              </p>
              {["PLANIFICADA", "OPTIMIZADA"].includes(ruta.estado) &&
              ruta.repartidorId !== null &&
              ruta.pedidosIds.length > 0 && (
                <button
                  type="button"
                  onClick={async () => {
                    try {
                      setError("");
                      setMensaje("");

                      await optimizarRuta(ruta.id);

                      setMensaje(
                        `Se solicitó la optimización de la ruta ${ruta.id}.`
                      );

                      await cargarRutas();
                    } catch (error) {
                      console.error("Error al optimizar la ruta:", error);
                      setError("No se pudo solicitar la optimización de la ruta.");
                    }
                  }}
                >
                  Optimizar ruta
                </button>
              )}
              <div className="admin-ruta-asignacion">
                <p>
                  <strong>Repartidor:</strong>{" "}
                  {ruta.repartidorId ?? "Sin asignar"}
                </p>

                {ruta.repartidorId === null &&
                  permiteModificar && (
                    <div className="admin-ruta-asignacion-controles">
                      <select
                        value={repartidorSeleccionado[ruta.id] ?? ""}
                        onChange={(e) =>
                          setRepartidorSeleccionado((anterior) => ({
                            ...anterior,
                            [ruta.id]: e.target.value,
                          }))
                        }
                      >
                        <option value="">Seleccionar repartidor</option>

                        {repartidores.map((repartidor) => (
                          <option
                            key={repartidor.id}
                            value={repartidor.id}
                          >
                            {repartidor.nombre} {repartidor.apellido} - ID{" "}
                            {repartidor.id}
                          </option>
                        ))}
                      </select>

                      <button
                        type="button"
                        disabled={!repartidorSeleccionado[ruta.id]}
                        onClick={async () => {
                          try {
                            setError("");
                            setMensaje("");

                            await asignarRepartidor(
                              ruta.id,
                              Number(repartidorSeleccionado[ruta.id])
                            );

                            setMensaje(
                              "Repartidor asignado correctamente."
                            );

                            await Promise.all([
                              cargarRutas(),
                              cargarRepartidores(),
                            ]);

                            setRepartidorSeleccionado((anterior) => ({
                              ...anterior,
                              [ruta.id]: "",
                            }));
                          } catch (error) {
                            console.error(
                              "Error al asignar repartidor:",
                              error
                            );
                            setError("No se pudo asignar el repartidor.");
                          }
                        }}
                      >
                        Asignar repartidor
                      </button>
                    </div>
                  )}
              </div>

              {ruta.repartidorId !== null && permiteModificar && (
                <button
                  type="button"
                  onClick={async () => {
                    try {
                      setError("");
                      setMensaje("");

                      await desasignarRepartidor(ruta.id);

                      setMensaje(
                        `Repartidor desasignado correctamente de la ruta ${ruta.id}.`
                      );

                      await Promise.all([
                        cargarRutas(),
                        cargarRepartidores(),
                      ]);
                    } catch (error) {
                      console.error(
                        "Error al desasignar repartidor:",
                        error
                      );
                      setError("No se pudo desasignar el repartidor.");
                    }
                  }}
                >
                  Desasignar repartidor
                </button>
              )}

              <p>
                <strong>Pedidos:</strong>{" "}
                {ruta.pedidosIds.length > 0
                  ? ruta.pedidosIds.join(", ")
                  : "Sin pedidos"}
              </p>

              {permiteModificar && ruta.repartidorId !== null && (
                <div className="admin-ruta-pedidos">
                  <h5>Asignar pedido</h5>

                  <select
                    value={pedidoSeleccionado[ruta.id] ?? ""}
                    onChange={(e) =>
                      setPedidoSeleccionado((anterior) => ({
                        ...anterior,
                        [ruta.id]: e.target.value,
                      }))
                    }
                  >
                    <option value="">Seleccionar pedido disponible</option>

                    {pedidosDisponibles.map((pedido) => (
                      <option
                        key={pedido.pedidoId}
                        value={pedido.pedidoId}
                      >
                        Pedido #{pedido.pedidoId} -{" "}
                        {pedido.nombreCliente} {pedido.apellidoCliente} -{" "}
                        {pedido.estado} - ${pedido.total}
                      </option>
                    ))}
                  </select>

                  <button
                    type="button"
                    disabled={!pedidoSeleccionado[ruta.id]}
                    onClick={() => handleAsignarPedido(ruta.id)}
                  >
                    Asignar pedido
                  </button>

                  {ruta.pedidosIds.length > 0 && (
                    <div>
                      <h5>Pedidos asignados</h5>

                      {ruta.pedidosIds.map((pedidoId) => (
                        <div key={pedidoId}>
                          <span>Pedido #{pedidoId}</span>

                          <button
                            type="button"
                            onClick={() =>
                              handleRetirarPedido(ruta.id, pedidoId)
                            }
                          >
                            Retirar pedido
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              <p>
                <strong>Paradas:</strong> {ruta.paradas.length}
              </p>

              <div className="admin-ruta-paradas">
                {ruta.paradas.map((parada) => (
                  <div key={parada.id}>
                    <strong>
                      {parada.orden}. {parada.tipo}
                    </strong>

                    <span>{parada.direccion}</span>

                    <span>
                      Coordenadas: {parada.longitud}, {parada.latitud}
                    </span>
                  </div>
                ))}
              </div>
              <div className="admin-ruta-mapa">
                <h5>Mapa del recorrido</h5>
                <MapaRuta paradas={ruta.paradas} />
              </div>
            </article>
          );
        })}
      </section>
    </main>
  );
}

export default RutasAdmin;
