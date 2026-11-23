// Este controlador administra los vehículos utilizados para realizar las entregas.
package com.logistica.logistica.controller;

import com.logistica.logistica.dto.ActualizarVehiculoRequest;
import com.logistica.logistica.dto.CrearVehiculoRequest;
import com.logistica.logistica.model.entity.Vehiculo;
import com.logistica.logistica.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehiculo crearVehiculo(@Valid @RequestBody CrearVehiculoRequest request) {
        return vehiculoService.crearVehiculo(request);
    }

    @GetMapping
    public List<Vehiculo> listarVehiculos() {
        return vehiculoService.listarVehiculos();
    }

    @GetMapping("/{id}")
    public Vehiculo buscarPorId(@PathVariable Long id) {
        return vehiculoService.buscarPorId(id);
    }

    @GetMapping("/patente/{patente}")
    public Vehiculo buscarPorPatente(@PathVariable String patente) {
        return vehiculoService.buscarPorPatente(patente);
    }

    @GetMapping("/estado/{activo}")
    public List<Vehiculo> listarPorEstado(@PathVariable Boolean activo) {
        return vehiculoService.listarPorEstado(activo);
    }

    @GetMapping("/disponibilidad/{disponible}")
    public List<Vehiculo> listarPorDisponibilidad(@PathVariable Boolean disponible) {
        return vehiculoService.listarPorDisponibilidad(disponible);
    }

    @PutMapping("/{id}")
    public Vehiculo actualizarVehiculo(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarVehiculoRequest request) {
        return vehiculoService.actualizarVehiculo(id, request);
    }

    @PatchMapping("/{id}/disponibilidad/activar")
    public Vehiculo activarDisponibilidad(@PathVariable Long id) {
        return vehiculoService.activarDisponibilidad(id);
    }

    @PatchMapping("/{id}/disponibilidad/desactivar")
    public Vehiculo desactivarDisponibilidad(@PathVariable Long id) {
        return vehiculoService.desactivarDisponibilidad(id);
    }

    @PatchMapping("/{id}/desactivar")
    public Vehiculo desactivarVehiculo(@PathVariable Long id) {
        return vehiculoService.desactivarVehiculo(id);
    }

    @PatchMapping("/{id}/reactivar")
    public Vehiculo reactivarVehiculo(@PathVariable Long id) {
        return vehiculoService.reactivarVehiculo(id);
    }
}