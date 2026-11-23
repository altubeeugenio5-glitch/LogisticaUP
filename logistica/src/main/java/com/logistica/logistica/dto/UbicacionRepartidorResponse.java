package com.logistica.logistica.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UbicacionRepartidorResponse {

    private Long id;

    private Long repartidorId;

    private Double longitud;

    private Double latitud;

    private LocalDateTime fechaRegistro;
}