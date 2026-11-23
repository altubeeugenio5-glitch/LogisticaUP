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
public class ResultadoOptimizacionDTO {

    private Long rutaId;

    private List<Long> paradasOrdenadas;

    private Double distanciaTotalKm;
}