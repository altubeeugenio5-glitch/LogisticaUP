package com.logistica.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudOptimizacionDTO {

    private Long rutaId;

    private List<ParadaOptimizacionDTO> paradas;
}