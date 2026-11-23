// Este controlador gestiona las cuentas de usuario de la plataforma.
package com.logistica.logistica.controller;

import com.logistica.logistica.dto.CrearUsuarioRequest;
import com.logistica.logistica.dto.UsuarioResponse;
import com.logistica.logistica.model.entity.Usuario;
import com.logistica.logistica.service.UsuarioService;
import com.logistica.logistica.dto.ActualizarUsuarioRequest;
import org.springframework.security.core.Authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // CREAR USUARIO
    @PostMapping
        public ResponseEntity<UsuarioResponse> crearUsuario(
                @Valid @RequestBody CrearUsuarioRequest request) {

        if (request.getRol() != com.logistica.logistica.model.enums.Rol.CLIENTE) {
                throw new IllegalArgumentException(
                        "El registro público solamente permite crear clientes");
        }

        Usuario nuevoUsuario = usuarioService.crearUsuario(request);

        UsuarioResponse response =
                usuarioService.convertirAResponse(nuevoUsuario);

        return ResponseEntity.ok(response);
        }

    // LISTAR TODOS LOS USUARIOS
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {

        List<UsuarioResponse> usuarios =
                usuarioService.listarUsuarios()
                        .stream()
                        .map(usuarioService::convertirAResponse)
                        .toList();

        return ResponseEntity.ok(usuarios);
    }

    // BUSCAR USUARIO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable Long id) {

        return usuarioService.buscarPorId(id)
                .map(usuarioService::convertirAResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ACTUALIZAR USUARIO
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {

        Usuario actualizado =
                usuarioService.actualizarUsuario(id, request);

        return ResponseEntity.ok(
                usuarioService.convertirAResponse(actualizado)
        );
    }

    // DESACTIVAR USUARIO
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioResponse> desactivarUsuario(
            @PathVariable Long id) {

        Usuario usuario =
                usuarioService.desactivarUsuario(id);

        return ResponseEntity.ok(
                usuarioService.convertirAResponse(usuario)
        );
    }

    // REACTIVAR USUARIO
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<UsuarioResponse> reactivarUsuario(
            @PathVariable Long id) {

        Usuario usuario =
                usuarioService.reactivarUsuario(id);

        return ResponseEntity.ok(
                usuarioService.convertirAResponse(usuario)
        );
    }

    // OBTENER LOS DATOS DEL USUARIO AUTENTICADO
        @GetMapping("/me")
        public ResponseEntity<UsuarioResponse> obtenerUsuarioAutenticado(
                Authentication authentication) {

        String email = authentication.getName();

        return usuarioService.buscarPorEmail(email)
                .map(usuarioService::convertirAResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        }
}