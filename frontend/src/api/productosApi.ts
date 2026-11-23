import api from "./axios";

export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  stockMinimo: number;
  activo: boolean;
}

export const obtenerProductos = async (): Promise<Producto[]> => {
  const response = await api.get<Producto[]>("/api/productos");

  return response.data;
};