
package com.logistica.logistica.dto;

import com.logistica.logistica.model.enums.EstadoPedido;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EstadoPedidoResponse {

    private Long pedidoId;

    private EstadoPedido estado;

    private LocalDateTime fechaCreacion;

    private BigDecimal total;
}
