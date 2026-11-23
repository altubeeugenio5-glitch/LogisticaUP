
package com.logistica.logistica.dto;

import com.logistica.logistica.model.enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PedidoCreadoResponse {

    private Long id;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private BigDecimal total;
}
