package com.logistica.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParadaOptimizacionDTO {

    private Long id;

    private Integer orden;

    private String tipo;

    private String direccion;

    private Double longitud;

    private Double latitud;
}