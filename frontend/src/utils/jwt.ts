
export function obtenerEmailDelToken(token: string): string {
  try {
    const partes = token.split(".");

    if (partes.length !== 3) {
      throw new Error("Formato JWT inválido");
    }

    const payloadBase64 = partes[1]
      .replace(/-/g, "+")
      .replace(/_/g, "/");

    const payload = JSON.parse(
      atob(payloadBase64)
    );

    if (typeof payload.sub !== "string") {
      throw new Error("El JWT no contiene un email válido");
    }

    return payload.sub;

  } catch {
    throw new Error("No se pudo interpretar el token JWT");
  }
}
