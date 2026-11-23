package com.logistica.logistica.service;

import com.logistica.logistica.dto.ActualizarUsuarioRequest;
import com.logistica.logistica.dto.CrearUsuarioRequest;
import com.logistica.logistica.dto.UsuarioResponse;
import com.logistica.logistica.model.entity.Cliente;
import com.logistica.logistica.model.entity.Repartidor;
import com.logistica.logistica.model.entity.Usuario;
import com.logistica.logistica.model.enums.Rol;
import com.logistica.logistica.repository.ClienteRepository;
import com.logistica.logistica.repository.RepartidorRepository;
import com.logistica.logistica.repository.UsuarioRepository;
import com.logistica.logistica.exception.ConflictoException;
import com.logistica.logistica.exception.RecursoNoEncontradoException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RepartidorRepository repartidorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            RepartidorRepository repartidorRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.repartidorRepository = repartidorRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public Usuario crearUsuario(CrearUsuarioRequest request) {

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictoException(
                    "El email ya está registrado");
        }

        if (request.getRol() == null) {
            throw new IllegalArgumentException(
                    "El rol es obligatorio");
        }

        // Impedir que un usuario se registre como administrador
        if (request.getRol() == Rol.ADMIN) {
            throw new IllegalArgumentException(
                    "No está permitido registrarse como administrador");
        }

        // 1. Crear la cuenta del usuario
        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setRol(request.getRol());

        usuario.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        usuario.setActivo(true);

        // 2. Guardar el usuario
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 3. Crear el perfil correspondiente
        if (usuarioGuardado.getRol() == Rol.CLIENTE) {

            Cliente cliente = new Cliente();
            cliente.setUsuario(usuarioGuardado);

            clienteRepository.save(cliente);

        } else if (usuarioGuardado.getRol() == Rol.REPARTIDOR) {

            Repartidor repartidor = new Repartidor();
            repartidor.setUsuario(usuarioGuardado);
            repartidor.setDisponible(false);

            repartidorRepository.save(repartidor);
        }

        return usuarioGuardado;
    }


    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public Usuario actualizarUsuario(
            Long id,
            ActualizarUsuarioRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
        .orElseThrow(() ->
                new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!usuario.getEmail().equals(request.getEmail())
                && usuarioRepository.existsByEmail(request.getEmail())) {

            throw new IllegalArgumentException(
                    "El email ya está registrado");
        }

        // No cambiar el rol en esta operación:
        // podría dejar el perfil asociado inconsistente.
        if (usuario.getRol() != request.getRol()) {
            throw new IllegalArgumentException(
                    "No se puede cambiar el rol mediante esta operación");
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario desactivarUsuario(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"));

        usuario.setActivo(false);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario reactivarUsuario(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Usuario no encontrado"));

        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    public UsuarioResponse convertirAResponse(Usuario usuario) {

        UsuarioResponse response = new UsuarioResponse();

        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        response.setActivo(usuario.getActivo());

        return response;
    }
}