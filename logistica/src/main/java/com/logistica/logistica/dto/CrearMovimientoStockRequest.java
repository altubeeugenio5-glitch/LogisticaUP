
package com.logistica.logistica.dto;

import com.logistica.logistica.model.enums.TipoMovimientoStock;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearMovimientoStockRequest {

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimientoStock tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @Size(max = 500)
    private String observacion;
}
