// Es el controlador REST encargado de gestionar las operaciones relacionadas con los clientes.
package com.logistica.logistica.controller;

import com.logistica.logistica.model.entity.Cliente;
import com.logistica.logistica.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.logistica.logistica.dto.ActualizarClienteRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // Listar todos los clientes
    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    // Buscar cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    // Buscar cliente por ID de usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Cliente> buscarPorUsuarioId(
            @PathVariable Long usuarioId) {

        return ResponseEntity.ok(
                clienteService.buscarPorUsuarioId(usuarioId));
    }
    // Actualizar datos del cliente
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarClienteRequest request) {

        return ResponseEntity.ok(
                clienteService.actualizarCliente(id, request));
    }
    // Desactivar cliente
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Cliente> desactivarCliente(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                clienteService.desactivarCliente(id));
    }

    // Reactivar cliente
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Cliente> reactivarCliente(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                clienteService.reactivarCliente(id));
    }
}