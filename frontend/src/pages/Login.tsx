import { useState } from "react";
import type { FormEvent } from "react";
import { Navigate, useNavigate } from "react-router-dom";

import { login as loginApi } from "../api/authApi";
import { useAuth } from "../context/AuthContext";

import "./Login.css";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const navigate = useNavigate();

  const {
    login,
    usuario,
    isAuthenticated,
    loading: authLoading,
  } = useAuth();

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setError("");
    setIsSubmitting(true);

    try {
      const response = await loginApi({
        email,
        password,
      });

      const usuarioActual = await login(response.token);

      switch (usuarioActual.rol) {
        case "ADMIN":
          navigate("/admin");
          break;

        case "CLIENTE":
          navigate("/cliente");
          break;

        case "REPARTIDOR":
          navigate("/repartidor");
          break;
      }

    } catch {
      setError("No se pudo iniciar sesión. Verificá tus credenciales.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (authLoading) {
    return <p>Verificando sesión...</p>;
  }

  if (isAuthenticated && usuario) {
    switch (usuario.rol) {
      case "ADMIN":
        return <Navigate to="/admin" replace />;

      case "CLIENTE":
        return <Navigate to="/cliente" replace />;

      case "REPARTIDOR":
        return <Navigate to="/repartidor" replace />;

      default:
        return <Navigate to="/login" replace />;
    }
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <h1>Logística</h1>
        <h2>Iniciar sesión</h2>

        <form className="login-form" onSubmit={handleSubmit}>
          <div>
            <label htmlFor="email">Correo electrónico</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="ejemplo@correo.com"
              required
            />
          </div>

          <div>
            <label htmlFor="password">Contraseña</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Ingresá tu contraseña"
              required
            />
          </div>

          {error && <p className="login-error">{error}</p>}

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Ingresando..." : "Ingresar"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;