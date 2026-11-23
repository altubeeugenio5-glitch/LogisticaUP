
import api from "./axios";

export interface ParadaRutaResponse {
  id: number;
  orden: number;
  tipo: string;
  direccion: string;
  longitud: number;
  latitud: number;
}
export interface PedidoRutaResponse {
  pedidoId: number;
  estado: string;
  fechaEntrega: string | null;
}
export interface RutaResponse {
  id: number;
  nombre: string;
  estado: string;
  fechaCreacion: string;
  fechaInicio: string | null;
  fechaFinalizacion: string | null;
  fechaCancelacion: string | null;
  repartidorId: number | null;
  pedidosIds: number[];
  pedidos: PedidoRutaResponse[];
  paradas: ParadaRutaResponse[];
}

export interface CrearParadaRutaRequest {
  orden: number;
  tipo: "ORIGEN" | "ENTREGA" | "DESTINO";
  direccion: string;
  longitud: number;
  latitud: number;
}

export interface CrearRutaRequest {
  nombre: string;
  paradas: CrearParadaRutaRequest[];
}

export interface AsignarRepartidorRutaRequest {
  repartidorId: number;
}

export interface AsignarPedidoRutaRequest {
  pedidoId: number;
}

export const obtenerRutas = async (): Promise<RutaResponse[]> => {
  const response = await api.get<RutaResponse[]>("/api/rutas");
  return response.data;
};

export const crearRuta = async (
  request: CrearRutaRequest
): Promise<RutaResponse> => {
  const response = await api.post<RutaResponse>(
    "/api/rutas",
    request
  );

  return response.data;
};

export const optimizarRuta = async (
  rutaId: number
): Promise<void> => {
  await api.post(`/api/rutas/${rutaId}/optimizar`);
};

export const iniciarRuta = async (
  rutaId: number
): Promise<void> => {
  await api.patch(`/api/rutas/${rutaId}/iniciar`);
};

export const completarRuta = async (
  rutaId: number
): Promise<void> => {
  await api.patch(`/api/rutas/${rutaId}/completar`);
};
export const confirmarEntrega = async (
  rutaId: number,
  pedidoId: number
): Promise<void> => {
  await api.patch(
    `/api/rutas/${rutaId}/pedidos/${pedidoId}/entregar`
  );
};
export const cancelarRuta = async (
  rutaId: number
): Promise<void> => {
  await api.put(`/api/rutas/${rutaId}/cancelar`);
};

export const asignarRepartidor = async (
  rutaId: number,
  repartidorId: number
): Promise<void> => {
  await api.patch(
    `/api/rutas/${rutaId}/repartidor`,
    { repartidorId }
  );
};

export const desasignarRepartidor = async (
  rutaId: number
): Promise<void> => {
  await api.delete(`/api/rutas/${rutaId}/repartidor`);
};

export const asignarPedido = async (
  rutaId: number,
  pedidoId: number
): Promise<void> => {
  await api.patch(
    `/api/rutas/${rutaId}/pedidos`,
    { pedidoId }
  );
};

export const retirarPedido = async (
  rutaId: number,
  pedidoId: number
): Promise<void> => {
  await api.delete(
    `/api/rutas/${rutaId}/pedidos/${pedidoId}`
  );
};
export const obtenerMisRutas = async (): Promise<RutaResponse[]> => {
  const response = await api.get<RutaResponse[]>(
    "/api/rutas/mis-rutas"
  );

  return response.data;
};