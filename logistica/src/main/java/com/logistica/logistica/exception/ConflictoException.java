// permite identificar y comunicar errores de negocio cuando una operación entra en conflicto con el estado actual del sistema.
// ejemplo, intentar asignar un pedido que ya esta asignado a otra ruta.
package com.logistica.logistica.exception;

public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}