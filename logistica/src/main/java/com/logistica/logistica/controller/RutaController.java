// Permite crear rutas, asignarles pedidos y repartidores, solicitar su optimización, iniciar y completar recorridos y consultar historiales.
package com.logistica.logistica.controller;

import com.logistica.logistica.dto.CrearRutaRequest;
import com.logistica.logistica.dto.RutaResponse;
import com.logistica.logistica.model.enums.EstadoRuta;
import com.logistica.logistica.service.RutaService;
import com.logistica.logistica.dto.AsignarPedidoRutaRequest;
import com.logistica.logistica.dto.AsignarRepartidorRutaRequest;
import com.logistica.logistica.dto.RutaHistorialResponse;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RutaResponse crearRuta(
            @Valid @RequestBody CrearRutaRequest request) {

        return rutaService.crearRuta(request);
    }

    @GetMapping
    public List<RutaResponse> listarRutas() {
        return rutaService.listarRutas();
    }
    @GetMapping("/mis-rutas")
    public List<RutaResponse> listarMisRutas(
            Authentication authentication) {

        return rutaService.listarMisRutas(
                authentication.getName()
        );
    }
    @GetMapping("/{id}")
    public RutaResponse buscarPorId(@PathVariable Long id) {
        return rutaService.buscarPorId(id);
    }

    @GetMapping("/estado/{estado}")
    public List<RutaResponse> listarPorEstado(
            @PathVariable EstadoRuta estado) {

        return rutaService.listarPorEstado(estado);
    }
    @PatchMapping("/{rutaId}/repartidor")
    public RutaResponse asignarRepartidor(
            @PathVariable Long rutaId,
            @Valid @RequestBody AsignarRepartidorRutaRequest request) {

        return rutaService.asignarRepartidor(
                rutaId,
                request.getRepartidorId()
        );
    }

    @PatchMapping("/{rutaId}/pedidos")
    public RutaResponse asignarPedido(
            @PathVariable Long rutaId,
            @Valid @RequestBody AsignarPedidoRutaRequest request) {

        return rutaService.asignarPedido(
                rutaId,
                request.getPedidoId()
        );
    }

    @DeleteMapping("/{rutaId}/pedidos/{pedidoId}")
    public RutaResponse retirarPedido(
            @PathVariable Long rutaId,
            @PathVariable Long pedidoId) {

        return rutaService.retirarPedido(rutaId, pedidoId);
    }
    @DeleteMapping("/{rutaId}/repartidor")
    public RutaResponse desasignarRepartidor(
            @PathVariable Long rutaId) {

        return rutaService.desasignarRepartidor(rutaId);
    }
    @GetMapping("/{rutaId}/pedidos")
    public List<Long> consultarPedidosAsignados(
            @PathVariable Long rutaId) {

        return rutaService.consultarPedidosAsignados(rutaId);
    }
    @PostMapping("/{rutaId}/optimizar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void solicitarOptimizacion(
            @PathVariable Long rutaId) {

        rutaService.solicitarOptimizacion(rutaId);
    }
    @PatchMapping("/{rutaId}/iniciar")
    public RutaResponse iniciarRuta(
            @PathVariable Long rutaId,
            Authentication authentication) {

        return rutaService.iniciarRuta(
                rutaId,
                authentication.getName()
        );
    }
    @PatchMapping("/{rutaId}/pedidos/{pedidoId}/entregar")
    public RutaResponse confirmarEntrega(
            @PathVariable Long rutaId,
            @PathVariable Long pedidoId,
            Authentication authentication) {

        return rutaService.confirmarEntrega(
                rutaId,
                pedidoId,
                authentication.getName()
        );
    }
    @PatchMapping("/{rutaId}/completar")
    public RutaResponse completarRuta(
            @PathVariable Long rutaId,
            Authentication authentication) {

        return rutaService.completarRuta(
                rutaId,
                authentication.getName()
        );
    }
    @GetMapping("/mi-historial")
    public List<RutaHistorialResponse> consultarMiHistorial(
            Authentication authentication) {

        return rutaService.consultarHistorialRepartidor(
                authentication.getName()
        );
    }
    @PutMapping("/{id}/cancelar")
    public RutaResponse cancelarRuta(@PathVariable Long id) {
        return rutaService.cancelarRuta(id);
    }
    @GetMapping("/historial")
    public List<RutaResponse> consultarHistorialRutas(
            @RequestParam List<EstadoRuta> estados,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime desde,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime hasta) {

        return rutaService.consultarHistorialRutas(estados, desde, hasta);
    }
}