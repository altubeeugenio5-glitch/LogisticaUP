
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
public class CrearVehiculoRequest {

    @NotBlank(message = "La patente es obligatoria")
    @Size(max = 20)
    private String patente;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 80)
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 80)
    private String modelo;

    @NotNull(message = "El año de fabricación es obligatorio")
    @Min(value = 1950, message = "El año de fabricación no es válido")
    @Max(value = 2026, message = "El año de fabricación no puede ser futuro")
    private Integer anioFabricacion;

    @NotNull(message = "La capacidad de carga es obligatoria")
    @DecimalMin(value = "0.1", message = "La capacidad debe ser mayor que cero")
    private Double capacidadCarga;
}
