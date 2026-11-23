// contiene la lógica de negocio para gestionar los clientes: permite listarlos, buscarlos por ID o usuario, actualizar su teléfono y activar o desactivar sus cuentas.
package com.logistica.logistica.service;

import com.logistica.logistica.dto.ActualizarClienteRequest;
import com.logistica.logistica.exception.RecursoNoEncontradoException;
import com.logistica.logistica.model.entity.Cliente;
import com.logistica.logistica.repository.ClienteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un cliente con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorUsuarioId(Long usuarioId) {
        return clienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un cliente asociado al usuario: " + usuarioId));
    }

    @Transactional
    public Cliente actualizarCliente(Long id, ActualizarClienteRequest request) {

        Cliente cliente = buscarPorId(id);

        cliente.setTelefono(request.getTelefono());

        return clienteRepository.save(cliente);
    }
    @Transactional
    public Cliente desactivarCliente(Long id) {

        Cliente cliente = buscarPorId(id);

        cliente.getUsuario().setActivo(false);

        return clienteRepository.save(cliente);
    }
    @Transactional
    public Cliente reactivarCliente(Long id) {

        Cliente cliente = buscarPorId(id);

        cliente.getUsuario().setActivo(true);

        return clienteRepository.save(cliente);
    }
}