package com.logistica.logistica.service;

import com.logistica.logistica.dto.RegistrarUbicacionRequest;
import com.logistica.logistica.dto.UbicacionRepartidorResponse;
import com.logistica.logistica.model.entity.Repartidor;
import com.logistica.logistica.model.entity.UbicacionRepartidor;
import com.logistica.logistica.repository.RepartidorRepository;
import com.logistica.logistica.repository.UbicacionRepartidorRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.logistica.logistica.exception.RecursoNoEncontradoException;
import com.logistica.logistica.dto.BuscarRepartidoresCercanosRequest;
import com.logistica.logistica.repository.RutaRepository;
import com.logistica.logistica.repository.PedidoRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.logistica.logistica.model.enums.EstadoRuta;

import java.time.LocalDateTime;

@Service
public class UbicacionRepartidorService {

    private final UbicacionRepartidorRepository ubicacionRepository;
    private final RepartidorRepository repartidorRepository;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);
    private final RutaRepository rutaRepository;
    private final PedidoRepository pedidoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public UbicacionRepartidorService(
        UbicacionRepartidorRepository ubicacionRepository,
        RepartidorRepository repartidorRepository,
        RutaRepository rutaRepository,
        PedidoRepository pedidoRepository,
        SimpMessagingTemplate messagingTemplate
        ) {
        this.ubicacionRepository = ubicacionRepository;
        this.repartidorRepository = repartidorRepository;
        this.rutaRepository = rutaRepository;
        this.pedidoRepository = pedidoRepository;
        this.messagingTemplate = messagingTemplate;
        }
    @Transactional
    public UbicacionRepartidorResponse registrarUbicacion(
            String email,
            RegistrarUbicacionRequest request
    ) {
        Repartidor repartidor = repartidorRepository
                .findByUsuarioEmail(email)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un repartidor asociado al usuario autenticado"
                        ));

        Point punto = geometryFactory.createPoint(
                new Coordinate(
                        request.getLongitud().doubleValue(),
                        request.getLatitud().doubleValue()
                )
        );

        UbicacionRepartidor ubicacion = new UbicacionRepartidor();
        ubicacion.setRepartidor(repartidor);
        ubicacion.setUbicacion(punto);
        ubicacion.setFechaRegistro(LocalDateTime.now());

        UbicacionRepartidor guardada =
        ubicacionRepository.save(ubicacion);

        UbicacionRepartidorResponse response =
                convertirAResponse(guardada);

        // Buscar rutas que estén actualmente en curso.
        var rutasActivas = rutaRepository.findByRepartidorIdAndEstadoIn(
                repartidor.getId(),
                java.util.List.of(EstadoRuta.EN_CURSO)
        );

        // Identificar los pedidos asociados a esas rutas.
        // LinkedHashSet evita pedidos duplicados y conserva el orden.
        java.util.Set<Long> pedidosIds = new java.util.LinkedHashSet<>();

        for (var ruta : rutasActivas) {

        var pedidos = pedidoRepository.findByRutaId(ruta.getId());

        for (var pedido : pedidos) {
                pedidosIds.add(pedido.getId());
        }
        }

        // Verificar que exista una transacción activa.
        if (!org.springframework.transaction.support.TransactionSynchronizationManager
                .isSynchronizationActive()) {
        throw new IllegalStateException(
                "No hay una transacción activa para publicar la ubicación"
        );
        }

        // Publicar solamente después de confirmar la transacción.
        org.springframework.transaction.support.TransactionSynchronizationManager
                .registerSynchronization(
                        new org.springframework.transaction.support.TransactionSynchronization() {

                        @Override
                        public void afterCommit() {

                                for (Long pedidoId : pedidosIds) {

                                messagingTemplate.convertAndSend(
                                        "/topic/pedidos/" + pedidoId + "/ubicacion",
                                        response
                                );
                                }
                        }
                        }
                );

        return response;
    }
    @Transactional(readOnly = true)
    public java.util.List<UbicacionRepartidorResponse> consultarHistorial(
            Long repartidorId
    ) {
        if (!repartidorRepository.existsById(repartidorId)) {
            throw new RecursoNoEncontradoException("Repartidor no encontrado");
        }

        return ubicacionRepository
                .findByRepartidorIdOrderByFechaRegistroDesc(repartidorId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public UbicacionRepartidorResponse consultarUltimaUbicacion(
            Long repartidorId
    ) {
        if (!repartidorRepository.existsById(repartidorId)) {
            throw new RecursoNoEncontradoException("Repartidor no encontrado");
        }

        UbicacionRepartidor ubicacion = ubicacionRepository
                .findFirstByRepartidorIdOrderByFechaRegistroDesc(repartidorId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El repartidor todavía no tiene ubicaciones registradas"
                        ));

        return convertirAResponse(ubicacion);
    }
    @Transactional(readOnly = true)
    public java.util.List<UbicacionRepartidorResponse> buscarCercanos(
            BuscarRepartidoresCercanosRequest request
    ) {
        return ubicacionRepository.buscarCercanos(
                        request.getLongitud().doubleValue(),
                        request.getLatitud().doubleValue(),
                        request.getRadioMetros()
                )
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }
    @Transactional(readOnly = true)
        public UbicacionRepartidorResponse consultarUltimaUbicacionPedido(
                Long pedidoId
        ) {
        var pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Pedido no encontrado")
                );

        if (pedido.getRuta() == null ||
                pedido.getRuta().getRepartidor() == null ||
                pedido.getRuta().getEstado() != EstadoRuta.EN_CURSO) {
                return null;
        }

        Long repartidorId = pedido.getRuta().getRepartidor().getId();

        return ubicacionRepository
                .findFirstByRepartidorIdOrderByFechaRegistroDesc(repartidorId)
                .map(this::convertirAResponse)
                .orElse(null);
        }
    private UbicacionRepartidorResponse convertirAResponse(
            UbicacionRepartidor ubicacion
    ) {
        UbicacionRepartidorResponse response =
                new UbicacionRepartidorResponse();

        response.setId(ubicacion.getId());
        response.setRepartidorId(ubicacion.getRepartidor().getId());
        response.setLongitud(ubicacion.getUbicacion().getX());
        response.setLatitud(ubicacion.getUbicacion().getY());
        response.setFechaRegistro(ubicacion.getFechaRegistro());

        return response;
    }
}