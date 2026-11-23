package com.logistica.logistica.dto;

import com.logistica.logistica.model.enums.TipoParada;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParadaRutaResponse {

    private Long id;

    private Integer orden;

    private TipoParada tipo;

    private String direccion;

    private Double longitud;

    private Double latitud;
}