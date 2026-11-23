
package com.logistica.logistica.dto;

import com.logistica.logistica.model.enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PedidoAdminResponse {

    private Long pedidoId;

    private Long clienteId;
    private String nombreCliente;
    private String apellidoCliente;
    private String emailCliente;
    private String telefonoCliente;

    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private BigDecimal total;

    private Long rutaId;

    private List<PedidoDetalleAdminResponse> detalles;
}
