package com.logistica.logistica.dto;

import com.logistica.logistica.model.enums.EstadoPedido;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PedidoRutaResponse {

    private Long pedidoId;

    private EstadoPedido estado;

    private LocalDateTime fechaEntrega;
}