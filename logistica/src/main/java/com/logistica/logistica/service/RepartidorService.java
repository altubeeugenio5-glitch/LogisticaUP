
package com.logistica.logistica.service;

import com.logistica.logistica.dto.ActualizarRepartidorRequest;
import com.logistica.logistica.dto.RepartidorAdminResponse;
import com.logistica.logistica.exception.ConflictoException;
import com.logistica.logistica.exception.RecursoNoEncontradoException;
import com.logistica.logistica.model.entity.Repartidor;
import com.logistica.logistica.model.enums.EstadoRuta;
import com.logistica.logistica.repository.RepartidorRepository;
import com.logistica.logistica.repository.RutaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RepartidorService {

    private final RepartidorRepository repartidorRepository;
    private final RutaRepository rutaRepository;

    private static final List<EstadoRuta> ESTADOS_RUTA_ACTIVA = List.of(
            EstadoRuta.PLANIFICADA,
            EstadoRuta.PENDIENTE_OPTIMIZACION,
            EstadoRuta.OPTIMIZADA,
            EstadoRuta.EN_CURSO
    );

    public RepartidorService(
            RepartidorRepository repartidorRepository,
            RutaRepository rutaRepository) {

        this.repartidorRepository = repartidorRepository;
        this.rutaRepository = rutaRepository;
    }

    @Transactional(readOnly = true)
    public List<RepartidorAdminResponse> listarRepartidoresAdmin() {
        return repartidorRepository.findAll()
                .stream()
                .map(this::convertirAAdminResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RepartidorAdminResponse> listarPorDisponibilidadAdmin(Boolean disponible) {
        return repartidorRepository.findByDisponible(disponible)
                .stream()
                .map(this::convertirAAdminResponse)
                .toList();
    }

    private RepartidorAdminResponse convertirAAdminResponse(Repartidor repartidor) {
        return new RepartidorAdminResponse(
                repartidor.getId(),
                repartidor.getUsuario().getId(),
                repartidor.getUsuario().getNombre(),
                repartidor.getUsuario().getApellido(),
                repartidor.getUsuario().getEmail(),
                repartidor.getTelefono(),
                repartidor.getDisponible(),
                repartidor.getUsuario().getActivo()
        );
    }

    @Transactional(readOnly = true)
    public Repartidor buscarPorId(Long id) {
        return repartidorRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un repartidor con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Repartidor buscarPorUsuarioId(Long usuarioId) {
        return repartidorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un repartidor asociado al usuario: " + usuarioId));
    }

    @Transactional(readOnly = true)
    public List<Repartidor> listarRepartidores() {
        return repartidorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Repartidor> listarPorDisponibilidad(Boolean disponible) {
        return repartidorRepository.findByDisponible(disponible);
    }

    @Transactional
    public Repartidor actualizarRepartidor(
            Long id,
            ActualizarRepartidorRequest request) {

        Repartidor repartidor = buscarPorId(id);
        repartidor.setTelefono(request.getTelefono());

        return repartidorRepository.save(repartidor);
    }

    @Transactional
    public Repartidor activarDisponibilidad(Long id) {
        Repartidor repartidor = buscarPorId(id);

        if (!Boolean.TRUE.equals(repartidor.getUsuario().getActivo())) {
            throw new ConflictoException(
                    "No se puede activar la disponibilidad de un repartidor desactivado"
            );
        }

        boolean tieneRutaActiva = !rutaRepository
                .findByRepartidorIdAndEstadoIn(
                        id,
                        ESTADOS_RUTA_ACTIVA
                )
                .isEmpty();

        if (tieneRutaActiva) {
            throw new ConflictoException(
                    "No se puede activar la disponibilidad: el repartidor ya tiene una ruta activa"
            );
        }

        repartidor.setDisponible(true);
        return repartidorRepository.save(repartidor);
    }

    @Transactional
    public Repartidor desactivarDisponibilidad(Long id) {
        Repartidor repartidor = buscarPorId(id);
        repartidor.setDisponible(false);
        return repartidorRepository.save(repartidor);
    }

    @Transactional
    public Repartidor desactivarRepartidor(Long id) {
        Repartidor repartidor = buscarPorId(id);

        repartidor.getUsuario().setActivo(false);
        repartidor.setDisponible(false);

        return repartidorRepository.save(repartidor);
    }

    @Transactional
    public Repartidor reactivarRepartidor(Long id) {
        Repartidor repartidor = buscarPorId(id);
        repartidor.getUsuario().setActivo(true);

        return repartidorRepository.save(repartidor);
    }
}
