import api from "./axios";

export interface Usuario {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  rol: "ADMIN" | "CLIENTE" | "REPARTIDOR";
  activo: boolean;
}

export const obtenerMiUsuario = async (): Promise<Usuario> => {
  const response = await api.get<Usuario>("/api/usuarios/me");

  return response.data;
};