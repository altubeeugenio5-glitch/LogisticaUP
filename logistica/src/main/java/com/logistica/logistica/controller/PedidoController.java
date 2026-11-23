// Se encarga de gestionar los pedidos: creación, consulta, actualización de estados, cancelación y seguimiento.
package com.logistica.logistica.controller;

import com.logistica.logistica.dto.CrearPedidoRequest;
import com.logistica.logistica.model.entity.Pedido;
import com.logistica.logistica.model.enums.EstadoPedido;
import com.logistica.logistica.service.PedidoService;
import com.logistica.logistica.dto.EstadoPedidoResponse;
import com.logistica.logistica.dto.PedidoClienteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.logistica.logistica.dto.PedidoCreadoResponse;
import com.logistica.logistica.dto.PedidoAdminResponse;
import com.logistica.logistica.dto.PedidoDetalleClienteResponse;
import com.logistica.logistica.dto.RutaResponse;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoCreadoResponse crearPedido(
            @Valid @RequestBody CrearPedidoRequest request,
            Authentication authentication) {

        Pedido pedido = pedidoService.crearPedido(
                request,
                authentication.getName()
        );

        return new PedidoCreadoResponse(
                pedido.getId(),
                pedido.getEstado(),
                pedido.getFechaCreacion(),
                pedido.getTotal()
        );
    }

    @GetMapping
    public List<PedidoAdminResponse> listarPedidos() {
        return pedidoService.listarPedidosAdmin();
    }

    @GetMapping("/{id}")
    public Pedido buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Pedido> listarPorCliente(@PathVariable Long clienteId) {
        return pedidoService.listarPorCliente(clienteId);
    }

    @GetMapping("/estado/{estado}")
    public List<Pedido> listarPorEstado(@PathVariable EstadoPedido estado) {
        return pedidoService.listarPorEstado(estado);
    }

    @PatchMapping("/{id}/estado")
    public Pedido actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido estado) {
        return pedidoService.actualizarEstado(id, estado);
    }

    @PatchMapping("/{id}/cancelar")
    public Pedido cancelarPedido(@PathVariable Long id) {
        return pedidoService.cancelarPedido(id);
    }
    
    @GetMapping("/{id}/estado")
    public EstadoPedidoResponse consultarEstado(
            @PathVariable Long id,
            Authentication authentication) {

        return pedidoService.consultarEstadoCliente(
                id,
                authentication.getName()
        );
    }

    @GetMapping("/cliente/{clienteId}/estado")
    public List<PedidoClienteResponse> consultarPedidosCliente(
            @PathVariable Long clienteId) {
        return pedidoService.consultarPedidosCliente(clienteId);
    }
    @GetMapping("/mis-pedidos/estado")
    public List<PedidoClienteResponse> consultarMisPedidos(
            Authentication authentication) {

        return pedidoService.consultarMisPedidos(
                authentication.getName()
        );
    }
    @GetMapping("/mi-historial")
    public List<PedidoClienteResponse> consultarMiHistorial(
            Authentication authentication) {

        return pedidoService.consultarHistorialEntregasCliente(
                authentication.getName()
        );
    }

    @GetMapping("/{id}/detalles")
    public List<PedidoDetalleClienteResponse> consultarDetallesPedidoCliente(
            @PathVariable Long id,
            Authentication authentication) {

        return pedidoService.consultarDetallesPedidoCliente(
                id,
                authentication.getName()
        );
    }
    @GetMapping("/{id}/ruta")
    public RutaResponse consultarRutaPedido(
            @PathVariable Long id,
            Authentication authentication) {

        return pedidoService.consultarRutaPedido(
                id,
                authentication.getName()
        );
    }
}
