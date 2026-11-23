
import api from "./axios";

export interface RepartidorAdminResponse {
  id: number;
  usuarioId: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string | null;
  disponible: boolean;
  activo: boolean;
}

export interface CrearRepartidorRequest {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  rol: "REPARTIDOR";
}

export interface RepartidorCreadoResponse {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  rol: string;
  activo: boolean;
}

export const obtenerRepartidores = async (): Promise<RepartidorAdminResponse[]> => {
  const response = await api.get<RepartidorAdminResponse[]>(
    "/api/repartidores"
  );
  return response.data;
};

export const obtenerRepartidoresDisponibles = async (): Promise<RepartidorAdminResponse[]> => {
  const response = await api.get<RepartidorAdminResponse[]>(
    "/api/repartidores/disponibilidad",
    {
      params: { disponible: true },
    }
  );
  return response.data;
};

export const crearRepartidor = async (
  request: CrearRepartidorRequest
): Promise<RepartidorCreadoResponse> => {
  const response = await api.post<RepartidorCreadoResponse>(
    "/api/repartidores",
    request
  );
  return response.data;
};

// Activar disponibilidad
export const activarDisponibilidadRepartidor = async (
  repartidorId: number
): Promise<void> => {
  await api.patch(
    `/api/repartidores/${repartidorId}/disponibilidad/activar`
  );
};

// Desactivar disponibilidad
export const desactivarDisponibilidadRepartidor = async (
  repartidorId: number
): Promise<void> => {
  await api.patch(
    `/api/repartidores/${repartidorId}/disponibilidad/desactivar`
  );
};
