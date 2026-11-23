
import api from "./axios";
import type { RutaResponse } from "./rutasApi";
export interface DetallePedidoRequest {
  productoId: number;
  cantidad: number;
}

export interface CrearPedidoRequest {
  detalles: DetallePedidoRequest[];
}

export interface PedidoCreadoResponse {
  id: number;
  estado: string;
  fechaCreacion: string;
  total: number;
}

export interface PedidoClienteResponse {
  pedidoId: number;
  estado: string;
  fechaCreacion: string;
  total: number;
}

export interface EstadoPedidoResponse {
  pedidoId: number;
  estado: string;
  fechaCreacion: string;
  total: number;
}

export const crearPedido = async (
  pedido: CrearPedidoRequest
): Promise<PedidoCreadoResponse> => {
  const response = await api.post<PedidoCreadoResponse>(
    "/api/pedidos",
    pedido
  );

  return response.data;
};

export const obtenerMisPedidos = async (): Promise<PedidoClienteResponse[]> => {
  const response = await api.get<PedidoClienteResponse[]>(
    "/api/pedidos/mis-pedidos/estado"
  );

  return response.data;
};

export const obtenerEstadoPedido = async (
  pedidoId: number
): Promise<EstadoPedidoResponse> => {
  const response = await api.get<EstadoPedidoResponse>(
    `/api/pedidos/${pedidoId}/estado`
  );

  return response.data;
};


export interface PedidoDetalleAdminResponse {
  productoId: number;
  nombreProducto: string;
  codigoProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}


export interface PedidoDetalleClienteResponse {
  productoId: number;
  nombreProducto: string;
  codigoProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export const obtenerDetallesPedidoCliente = async (
  pedidoId: number
): Promise<PedidoDetalleClienteResponse[]> => {
  const response = await api.get<PedidoDetalleClienteResponse[]>(
    `/api/pedidos/${pedidoId}/detalles`
  );

  return response.data;
};

export const obtenerRutaPedido = async (
  pedidoId: number
): Promise<RutaResponse> => {
  const response = await api.get<RutaResponse>(
    `/api/pedidos/${pedidoId}/ruta`
  );

  return response.data;
};

export interface PedidoAdminResponse {
  pedidoId: number;
  clienteId: number;
  nombreCliente: string;
  apellidoCliente: string;
  emailCliente: string;
  telefonoCliente: string | null;
  estado: string;
  fechaCreacion: string;
  total: number;
  rutaId: number | null;
  detalles: PedidoDetalleAdminResponse[];
}

export const obtenerPedidosAdmin = async (): Promise<PedidoAdminResponse[]> => {
  const response = await api.get<PedidoAdminResponse[]>("/api/pedidos");
  return response.data;
};


export const actualizarEstadoPedidoAdmin = async (
  pedidoId: number,
  estado: string
): Promise<void> => {
  await api.patch(
    `/api/pedidos/${pedidoId}/estado`,
    null,
    {
      params: { estado },
    }
  );
};
