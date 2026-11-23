// permite registrar y consultar la ubicación de los repartidores en tiempo real, facilitando el seguimiento de entregas y la asignación de pedidos cercanos.
package com.logistica.logistica.controller;

import com.logistica.logistica.dto.RegistrarUbicacionRequest;
import com.logistica.logistica.dto.UbicacionRepartidorResponse;
import com.logistica.logistica.service.UbicacionRepartidorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.logistica.logistica.dto.BuscarRepartidoresCercanosRequest;

import java.util.List;

@RestController
@RequestMapping("/api/ubicaciones")
public class UbicacionRepartidorController {

    private final UbicacionRepartidorService ubicacionService;

    public UbicacionRepartidorController(
            UbicacionRepartidorService ubicacionService
    ) {
        this.ubicacionService = ubicacionService;
    }

    @PostMapping("/mi-ubicacion")
    @ResponseStatus(HttpStatus.CREATED)
    public UbicacionRepartidorResponse registrarUbicacion(
            @Valid @RequestBody RegistrarUbicacionRequest request
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return ubicacionService.registrarUbicacion(email, request);
    }

    @GetMapping("/repartidores/{repartidorId}/historial")
    public List<UbicacionRepartidorResponse> consultarHistorial(
            @PathVariable Long repartidorId
    ) {
        return ubicacionService.consultarHistorial(repartidorId);
    }

    @GetMapping("/repartidores/{repartidorId}/actual")
    public UbicacionRepartidorResponse consultarUltimaUbicacion(
            @PathVariable Long repartidorId
    ) {
        return ubicacionService.consultarUltimaUbicacion(repartidorId);
    }
    @PostMapping("/cercanos")
    public List<UbicacionRepartidorResponse> buscarCercanos(
            @Valid @RequestBody BuscarRepartidoresCercanosRequest request
    ) {
        return ubicacionService.buscarCercanos(request);
    }
}