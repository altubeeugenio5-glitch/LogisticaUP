// contiene pruebas unitarias para verificar el funcionamiento de UsuarioService utilizando JUnit y Mockito. Comprueba la creación de clientes y repartidores, el rechazo de emails duplicados, las validaciones al actualizar usuarios y las operaciones de desactivación y reactivación.
package com.logistica.logistica.service;

import com.logistica.logistica.dto.CrearUsuarioRequest;
import com.logistica.logistica.dto.ActualizarUsuarioRequest;
import com.logistica.logistica.exception.ConflictoException;
import com.logistica.logistica.exception.RecursoNoEncontradoException;
import com.logistica.logistica.model.entity.Cliente;
import com.logistica.logistica.model.entity.Repartidor;
import com.logistica.logistica.model.entity.Usuario;
import com.logistica.logistica.model.enums.Rol;
import com.logistica.logistica.repository.ClienteRepository;
import com.logistica.logistica.repository.RepartidorRepository;
import com.logistica.logistica.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RepartidorRepository repartidorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void debeCrearClienteCorrectamente() {

        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setNombre("Juan");
        request.setApellido("Perez");
        request.setEmail("juan@example.com");
        request.setRol(Rol.CLIENTE);
        request.setPassword("MiClave123");

        when(usuarioRepository.existsByEmail("juan@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("MiClave123"))
                .thenReturn("hash-falso");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.crearUsuario(request);

        assertEquals("Juan", resultado.getNombre());
        assertEquals("juan@example.com", resultado.getEmail());
        assertEquals(Rol.CLIENTE, resultado.getRol());
        assertEquals("hash-falso", resultado.getPasswordHash());
        assertTrue(resultado.getActivo());

        verify(clienteRepository).save(any(Cliente.class));
        verify(repartidorRepository, never()).save(any(Repartidor.class));
    }

    @Test
    void debeCrearRepartidorCorrectamente() {

        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setNombre("Carlos");
        request.setApellido("Lopez");
        request.setEmail("carlos@example.com");
        request.setRol(Rol.REPARTIDOR);
        request.setPassword("MiClave123");

        when(usuarioRepository.existsByEmail("carlos@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("MiClave123"))
                .thenReturn("hash-falso");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.crearUsuario(request);

        assertEquals(Rol.REPARTIDOR, resultado.getRol());

        ArgumentCaptor<Repartidor> captor =
                ArgumentCaptor.forClass(Repartidor.class);

        verify(repartidorRepository).save(captor.capture());

        assertFalse(captor.getValue().getDisponible());

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void noDebeCrearUsuarioConEmailDuplicado() {

        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setEmail("existente@example.com");

        when(usuarioRepository.existsByEmail("existente@example.com"))
                .thenReturn(true);

        assertThrows(
                ConflictoException.class,
                () -> usuarioService.crearUsuario(request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void debeLanzarErrorSiElUsuarioNoExisteAlActualizar() {

        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest();

        when(usuarioRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> usuarioService.actualizarUsuario(999L, request)
        );
    }

    @Test
    void noDebePermitirCambiarElRolDelUsuario() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("juan@example.com");
        usuario.setRol(Rol.CLIENTE);

        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest();
        request.setNombre("Juan");
        request.setApellido("Perez");
        request.setEmail("juan@example.com");
        request.setRol(Rol.REPARTIDOR);

        when(usuarioRepository.findById(1L))
                .thenReturn(java.util.Optional.of(usuario));

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.actualizarUsuario(1L, request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void debeDesactivarUsuario() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setActivo(true);

        when(usuarioRepository.findById(1L))
                .thenReturn(java.util.Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.desactivarUsuario(1L);

        assertFalse(resultado.getActivo());
    }

    @Test
    void debeReactivarUsuario() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setActivo(false);

        when(usuarioRepository.findById(1L))
                .thenReturn(java.util.Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.reactivarUsuario(1L);

        assertTrue(resultado.getActivo());
    }
}