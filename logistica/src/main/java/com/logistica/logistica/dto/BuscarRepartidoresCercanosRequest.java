package com.logistica.logistica.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BuscarRepartidoresCercanosRequest {

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal longitud;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal latitud;

    @NotNull(message = "El radio es obligatorio")
    @Positive(message = "El radio debe ser mayor que cero")
    @DecimalMax(value = "50000.0", message = "El radio máximo permitido es de 50000 metros")
    private Double radioMetros;
}