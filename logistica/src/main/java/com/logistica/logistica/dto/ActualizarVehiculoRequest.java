
package com.logistica.logistica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarVehiculoRequest {

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 80)
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 80)
    private String modelo;

    @NotNull(message = "El año de fabricación es obligatorio")
    @Min(value = 1950)
    @Max(value = 2026)
    private Integer anioFabricacion;

    @NotNull(message = "La capacidad de carga es obligatoria")
    @DecimalMin(value = "0.1")
    private Double capacidadCarga;
}
