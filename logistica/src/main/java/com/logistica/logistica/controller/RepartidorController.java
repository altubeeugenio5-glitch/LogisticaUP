
package com.logistica.logistica.controller;

import com.logistica.logistica.dto.ActualizarRepartidorRequest;
import com.logistica.logistica.dto.CrearUsuarioRequest;
import com.logistica.logistica.dto.RepartidorAdminResponse;
import com.logistica.logistica.dto.UsuarioResponse;
import com.logistica.logistica.model.entity.Repartidor;
import com.logistica.logistica.model.entity.Usuario;
import com.logistica.logistica.model.enums.Rol;
import com.logistica.logistica.service.RepartidorService;
import com.logistica.logistica.service.UsuarioService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/repartidores")
public class RepartidorController {

    private final RepartidorService repartidorService;
    private final UsuarioService usuarioService;

    public RepartidorController(
            RepartidorService repartidorService,
            UsuarioService usuarioService) {
        this.repartidorService = repartidorService;
        this.usuarioService = usuarioService;
    }

    // Crear cuenta y perfil de repartidor
    @PostMapping
    public ResponseEntity<UsuarioResponse> crearRepartidor(
            @Valid @RequestBody CrearUsuarioRequest request) {

        if (request.getRol() != Rol.REPARTIDOR) {
            throw new IllegalArgumentException(
                    "El rol debe ser REPARTIDOR");
        }

        Usuario nuevoUsuario = usuarioService.crearUsuario(request);

        return ResponseEntity.ok(
                usuarioService.convertirAResponse(nuevoUsuario));
    }

    @GetMapping
    public ResponseEntity<List<RepartidorAdminResponse>> listarRepartidores() {
        return ResponseEntity.ok(
                repartidorService.listarRepartidoresAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Repartidor> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                repartidorService.buscarPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Repartidor> buscarPorUsuarioId(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(
                repartidorService.buscarPorUsuarioId(usuarioId));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<RepartidorAdminResponse>> listarPorDisponibilidad(
            @RequestParam Boolean disponible) {
        return ResponseEntity.ok(
                repartidorService.listarPorDisponibilidadAdmin(disponible));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Repartidor> actualizarRepartidor(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarRepartidorRequest request) {
        return ResponseEntity.ok(
                repartidorService.actualizarRepartidor(id, request));
    }

    @PatchMapping("/{id}/disponibilidad/activar")
    public ResponseEntity<Repartidor> activarDisponibilidad(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                repartidorService.activarDisponibilidad(id));
    }

    @PatchMapping("/{id}/disponibilidad/desactivar")
    public ResponseEntity<Repartidor> desactivarDisponibilidad(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                repartidorService.desactivarDisponibilidad(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Repartidor> desactivarRepartidor(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                repartidorService.desactivarRepartidor(id));
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Repartidor> reactivarRepartidor(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                repartidorService.reactivarRepartidor(id));
    }
}
