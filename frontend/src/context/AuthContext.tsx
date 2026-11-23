
import {
  createContext,
  useContext,
  useState,
  useEffect,
  type ReactNode,
} from "react";

import { obtenerMiUsuario } from "../api/usuariosApi";
import type { Usuario } from "../api/usuariosApi";
import { obtenerEmailDelToken } from "../utils/jwt";

interface AuthContextType {
  token: string | null;
  usuario: Usuario | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (token: string) => Promise<Usuario>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(
    () => localStorage.getItem("token")
  );

  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [loading, setLoading] = useState(true);

  // RECUPERAR SESIÓN AL INICIAR LA APLICACIÓN
  useEffect(() => {
    const recuperarSesion = async () => {
      if (!token) {
        setLoading(false);
        return;
      }

      try {
        obtenerEmailDelToken(token);

        const usuarioEncontrado = await obtenerMiUsuario();

        if (!usuarioEncontrado.activo) {
          logout();
          return;
        }

        setUsuario(usuarioEncontrado);

      } catch {
        logout();

      } finally {
        setLoading(false);
      }
    };

    recuperarSesion();
  }, [token]);

  // INICIAR SESIÓN
  const login = async (newToken: string): Promise<Usuario> => {
    setLoading(true);

    try {
      localStorage.setItem("token", newToken);

      obtenerEmailDelToken(newToken);

      const usuarioEncontrado = await obtenerMiUsuario();

      if (!usuarioEncontrado.activo) {
        throw new Error("Usuario inactivo");
      }

      setToken(newToken);
      setUsuario(usuarioEncontrado);

      return usuarioEncontrado;

    } catch (error) {
      logout();
      throw error;

    } finally {
      setLoading(false);
    }
  };

  // CERRAR SESIÓN
  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
    setUsuario(null);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        usuario,
        isAuthenticated: !!token && !!usuario,
        loading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// HOOK PARA UTILIZAR LA AUTENTICACIÓN
export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth debe utilizarse dentro de AuthProvider");
  }

  return context;
}
