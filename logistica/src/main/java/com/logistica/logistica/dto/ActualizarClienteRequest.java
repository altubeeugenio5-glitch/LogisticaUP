
package com.logistica.logistica.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarClienteRequest {

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String telefono;
}
