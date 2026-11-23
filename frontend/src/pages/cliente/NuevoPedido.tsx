
import { useEffect, useState } from "react";

import {
  obtenerProductos,
  type Producto,
} from "../../api/productosApi";

import { crearPedido } from "../../api/pedidosApi";

interface ItemCarrito {
  producto: Producto;
  cantidad: number;
}

function NuevoPedido() {
  const [productos, setProductos] = useState<Producto[]>([]);
  const [cantidades, setCantidades] = useState<Record<number, number>>({});
  const [carrito, setCarrito] = useState<ItemCarrito[]>([]);
  const [loading, setLoading] = useState(true);
  const [confirmando, setConfirmando] = useState(false);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");

  useEffect(() => {
    const cargarProductos = async () => {
      try {
        const resultado = await obtenerProductos();

        setProductos(
          resultado.filter(
            (producto) => producto.activo && producto.stock > 0
          )
        );
      } catch {
        setError("No se pudieron cargar los productos.");
      } finally {
        setLoading(false);
      }
    };

    cargarProductos();
  }, []);

  const cambiarCantidad = (productoId: number, valor: number) => {
    setCantidades((actuales) => ({
      ...actuales,
      [productoId]: valor,
    }));
  };

  const agregarAlCarrito = (producto: Producto) => {
    const cantidadSeleccionada = cantidades[producto.id] || 1;

    const itemExistente = carrito.find(
      (item) => item.producto.id === producto.id
    );

    const cantidadActual = itemExistente?.cantidad || 0;
    const nuevaCantidad = cantidadActual + cantidadSeleccionada;

    if (nuevaCantidad > producto.stock) {
      setMensaje("No hay stock suficiente para esa cantidad.");
      return;
    }

    if (itemExistente) {
      setCarrito((actual) =>
        actual.map((item) =>
          item.producto.id === producto.id
            ? { ...item, cantidad: nuevaCantidad }
            : item
        )
      );
    } else {
      setCarrito((actual) => [
        ...actual,
        {
          producto,
          cantidad: cantidadSeleccionada,
        },
      ]);
    }

    setMensaje(`${producto.nombre} agregado al carrito.`);

    setCantidades((actuales) => ({
      ...actuales,
      [producto.id]: 1,
    }));
  };

  const eliminarDelCarrito = (productoId: number) => {
    setCarrito((actual) =>
      actual.filter((item) => item.producto.id !== productoId)
    );

    setMensaje("Producto eliminado del carrito.");
  };

  const calcularTotal = () => {
    return carrito.reduce(
      (total, item) =>
        total + item.producto.precio * item.cantidad,
      0
    );
  };

  const confirmarPedido = async () => {
    if (carrito.length === 0) {
      setMensaje("Agregá al menos un producto al carrito.");
      return;
    }

    try {
      setConfirmando(true);
      setError("");
      setMensaje("");

      const pedido = {
        detalles: carrito.map((item) => ({
          productoId: item.producto.id,
          cantidad: item.cantidad,
        })),
      };

      const resultado = await crearPedido(pedido);

        setCarrito([]);
        setMensaje(`¡Pedido #${resultado.id} creado correctamente!`);

        try {
        const productosActualizados = await obtenerProductos();

        setProductos(
            productosActualizados.filter(
            (producto) => producto.activo && producto.stock > 0
            )
        );
        } catch (error) {
        console.error("No se pudo actualizar el catálogo:", error);
        }
    } catch (err: unknown) {
      console.error("Error al confirmar pedido:", err);
      setError(
        "No se pudo confirmar el pedido. Verificá el stock disponible e intentá nuevamente."
      );
    } finally {
      setConfirmando(false);
    }
  };

  return (
    <main className="cliente-main">
      <h1>Nuevo pedido</h1>

      <section className="cliente-pedidos">
        <h2>Catálogo de productos</h2>

        {loading && <p>Cargando productos...</p>}
        {error && <p>{error}</p>}
        {mensaje && <p>{mensaje}</p>}

        {!loading && !error && productos.length === 0 && (
          <p>No hay productos disponibles.</p>
        )}

        {!loading && productos.length > 0 && (
          <div>
            {productos.map((producto) => (
              <div key={producto.id}>
                <h3>{producto.nombre}</h3>

                <p>{producto.descripcion}</p>

                <p>Código: {producto.codigo}</p>

                <p>
                  Precio: ${producto.precio.toLocaleString("es-AR")}
                </p>

                <p>Stock disponible: {producto.stock}</p>

                <label htmlFor={`cantidad-${producto.id}`}>
                  Cantidad:
                </label>

                <input
                  id={`cantidad-${producto.id}`}
                  type="number"
                  min="1"
                  max={producto.stock}
                  value={cantidades[producto.id] || 1}
                  onChange={(e) =>
                    cambiarCantidad(
                      producto.id,
                      Math.max(
                        1,
                        Math.min(
                          Number(e.target.value) || 1,
                          producto.stock
                        )
                      )
                    )
                  }
                />

                <button
                  type="button"
                  disabled={confirmando}
                  onClick={() => agregarAlCarrito(producto)}
                >
                  Agregar al carrito
                </button>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="cliente-pedidos">
        <h2>Mi carrito</h2>

        {carrito.length === 0 ? (
          <p>Tu carrito está vacío.</p>
        ) : (
          carrito.map((item) => (
            <div key={item.producto.id}>
              <h3>{item.producto.nombre}</h3>

              <p>Cantidad: {item.cantidad}</p>

              <p>
                Subtotal: $
                {(item.producto.precio * item.cantidad).toLocaleString("es-AR")}
              </p>

              <button
                type="button"
                disabled={confirmando}
                onClick={() => eliminarDelCarrito(item.producto.id)}
              >
                Eliminar
              </button>
            </div>
          ))
        )}

        <h3>
          Total del pedido: $
          {calcularTotal().toLocaleString("es-AR")}
        </h3>

        <button
          type="button"
          disabled={carrito.length === 0 || confirmando}
          onClick={confirmarPedido}
        >
          {confirmando ? "Confirmando..." : "Confirmar pedido"}
        </button>
      </section>
    </main>
  );
}

export default NuevoPedido;
