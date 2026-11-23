package com.logistica.logistica.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignarPedidoRutaRequest {

    @NotNull
    @Positive
    private Long pedidoId;
}
