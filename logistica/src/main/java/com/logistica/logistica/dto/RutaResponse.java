package com.logistica.logistica.dto;

import com.logistica.logistica.model.enums.EstadoRuta;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class RutaResponse {

    private Long id;

    private String nombre;

    private EstadoRuta estado;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFinalizacion;

    private LocalDateTime fechaCancelacion;

    private Long repartidorId;

    private List<Long> pedidosIds;

    private List<PedidoRutaResponse> pedidos;
    
    private List<ParadaRutaResponse> paradas;
}