
package com.logistica.logistica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrearPedidoRequest {

    @NotEmpty(message = "El pedido debe contener al menos un producto")
    @Valid
    private List<CrearDetallePedidoRequest> detalles;
}
