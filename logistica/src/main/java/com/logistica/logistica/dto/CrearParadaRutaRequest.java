package com.logistica.logistica.dto;

import com.logistica.logistica.model.enums.TipoParada;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearParadaRutaRequest {

    @NotNull
    @Positive
    private Integer orden;

    @NotNull
    private TipoParada tipo;

    @NotBlank
    private String direccion;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal longitud;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal latitud;
}