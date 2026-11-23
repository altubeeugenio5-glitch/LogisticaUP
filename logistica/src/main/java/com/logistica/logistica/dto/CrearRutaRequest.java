package com.logistica.logistica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrearRutaRequest {

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotEmpty
    @Valid
    private List<CrearParadaRutaRequest> paradas;
}