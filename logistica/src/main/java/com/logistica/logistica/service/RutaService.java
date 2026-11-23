// contiene la lógica principal para gestionar las rutas de entrega: permite crearlas, asignarles repartidores y pedidos, iniciarlas, completarlas o cancelarlas. También valida las operaciones, conserva historiales y coordina la optimización de recorridos mediante RabbitMQ y un servicio externo de Python.
package com.logistica.logistica.service;

import com.logistica.logistica.dto.CrearParadaRutaRequest;
import com.logistica.logistica.dto.CrearRutaRequest;
import com.logistica.logistica.dto.ParadaOptimizacionDTO;
import com.logistica.logistica.dto.ParadaRutaResponse;
import com.logistica.logistica.dto.ResultadoOptimizacionDTO;
import com.logistica.logistica.dto.RutaResponse;
import com.logistica.logistica.exception.ConflictoException;
import com.logistica.logistica.exception.RecursoNoEncontradoException;
import com.logistica.logistica.model.entity.ParadaRuta;
import com.logistica.logistica.model.entity.Pedido;
import com.logistica.logistica.model.entity.Repartidor;
import com.logistica.logistica.model.entity.Ruta;
import com.logistica.logistica.model.enums.EstadoPedido;
import com.logistica.logistica.model.enums.EstadoRuta;
import com.logistica.logistica.model.enums.TipoParada;
import com.logistica.logistica.repository.RutaRepository;
import com.logistica.logistica.repository.RepartidorRepository;
import com.logistica.logistica.repository.PedidoRepository;
import com.logistica.logistica.dto.SolicitudOptimizacionDTO;
import com.logistica.logistica.dto.RutaHistorialResponse;
import com.logistica.logistica.repository.RutaPedidoHistorialRepository;
import com.logistica.logistica.model.entity.RutaPedidoHistorial;
import com.logistica.logistica.dto.PedidoRutaResponse;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Set;

@Service
public class RutaService {

    private final RutaRepository rutaRepository;
    private final RepartidorRepository repartidorRepository;
    private final PedidoRepository pedidoRepository;  
    private final RutaPedidoHistorialRepository rutaPedidoHistorialRepository;  
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);
    private final OptimizacionPublisher optimizacionPublisher;
    public RutaService(
                RutaRepository rutaRepository,
                RepartidorRepository repartidorRepository,
                PedidoRepository pedidoRepository,
                RutaPedidoHistorialRepository rutaPedidoHistorialRepository,
                OptimizacionPublisher optimizacionPublisher) {

        this.rutaRepository = rutaRepository;
        this.repartidorRepository = repartidorRepository;
        this.pedidoRepository = pedidoRepository;
        this.rutaPedidoHistorialRepository = rutaPedidoHistorialRepository;
        this.optimizacionPublisher = optimizacionPublisher;
        }

    @Transactional
    public RutaResponse crearRuta(CrearRutaRequest request) {

        validarParadas(request.getParadas());

        Ruta ruta = new Ruta();
        ruta.setNombre(request.getNombre());
        ruta.setEstado(EstadoRuta.PLANIFICADA);

        for (CrearParadaRutaRequest paradaRequest : request.getParadas()) {

            Point ubicacion = geometryFactory.createPoint(
                    new Coordinate(
                            paradaRequest.getLongitud().doubleValue(),
                            paradaRequest.getLatitud().doubleValue()
                    )
            );

            ParadaRuta parada = new ParadaRuta();
            parada.setRuta(ruta);
            parada.setOrden(paradaRequest.getOrden());
            parada.setTipo(paradaRequest.getTipo());
            parada.setDireccion(paradaRequest.getDireccion());
            parada.setUbicacion(ubicacion);

            ruta.getParadas().add(parada);
        }

        Ruta rutaGuardada = rutaRepository.save(ruta);

        return convertirARutaResponse(rutaGuardada);
    }

    @Transactional(readOnly = true)
    public List<RutaResponse> listarRutas() {

        return rutaRepository.findAll()
                .stream()
                .map(this::convertirARutaResponse)
                .toList();
    }
        @Transactional(readOnly = true)
        public List<RutaResponse> listarMisRutas(String email) {

        Repartidor repartidor = repartidorRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un repartidor asociado al usuario autenticado"
                ));

        return rutaRepository.findByRepartidorIdOrderByFechaCreacionDesc(
                        repartidor.getId()
                )
                .stream()
                .map(this::convertirARutaResponse)
                .toList();
        }
    @Transactional(readOnly = true)
    public RutaResponse buscarPorId(Long id) {

        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una ruta con el ID: " + id
                ));

        return convertirARutaResponse(ruta);
    }

    @Transactional(readOnly = true)
    public List<RutaResponse> listarPorEstado(EstadoRuta estado) {

        return rutaRepository.findByEstado(estado)
                .stream()
                .map(this::convertirARutaResponse)
                .toList();
    }

    private void validarParadas(List<CrearParadaRutaRequest> paradas) {

        long cantidadOrigenes = paradas.stream()
                .filter(p -> p.getTipo() == TipoParada.ORIGEN)
                .count();

        long cantidadDestinos = paradas.stream()
                .filter(p -> p.getTipo() == TipoParada.DESTINO)
                .count();

        if (cantidadOrigenes != 1 || cantidadDestinos != 1) {
            throw new ConflictoException(
                    "La ruta debe tener exactamente un origen y un destino"
            );
        }

        long cantidadOrdenes = paradas.stream()
                .map(CrearParadaRutaRequest::getOrden)
                .distinct()
                .count();

        if (cantidadOrdenes != paradas.size()) {
            throw new ConflictoException(
                    "No puede haber dos paradas con el mismo orden"
            );
        }

        List<CrearParadaRutaRequest> ordenadas = paradas.stream()
                .sorted(Comparator.comparing(
                        CrearParadaRutaRequest::getOrden
                ))
                .toList();

        if (ordenadas.get(0).getTipo() != TipoParada.ORIGEN) {
            throw new ConflictoException(
                    "La primera parada debe ser el origen"
            );
        }

        if (ordenadas.get(ordenadas.size() - 1).getTipo()
                != TipoParada.DESTINO) {
            throw new ConflictoException(
                    "La última parada debe ser el destino"
            );
        }
    }

    public RutaResponse convertirARutaResponse(Ruta ruta) {

        RutaResponse response = new RutaResponse();

        response.setId(ruta.getId());
        response.setNombre(ruta.getNombre());
        response.setEstado(ruta.getEstado());
        response.setFechaCreacion(ruta.getFechaCreacion());
        response.setFechaInicio(ruta.getFechaInicio());
        response.setFechaFinalizacion(ruta.getFechaFinalizacion());
        response.setFechaCancelacion(ruta.getFechaCancelacion());
        // Repartidor asignado
        if (ruta.getRepartidor() != null) {
            response.setRepartidorId(ruta.getRepartidor().getId());
        }

        // Pedidos asignados
        List<Long> pedidosIds = java.util.stream.Stream.concat(
                pedidoRepository.findByRutaId(ruta.getId())
                        .stream()
                        .map(Pedido::getId),
                rutaPedidoHistorialRepository.findByRutaId(ruta.getId())
                        .stream()
                        .map(RutaPedidoHistorial::getPedidoId)
        )
        .distinct()
        .toList();

        response.setPedidosIds(pedidosIds);
        List<PedidoRutaResponse> pedidos = pedidoRepository.findByRutaId(ruta.getId())
                .stream()
                .map(pedido -> {
                PedidoRutaResponse pedidoResponse = new PedidoRutaResponse();

                pedidoResponse.setPedidoId(pedido.getId());
                pedidoResponse.setEstado(pedido.getEstado());
                pedidoResponse.setFechaEntrega(pedido.getFechaEntrega());

                return pedidoResponse;
                })
                .toList();

        response.setPedidos(pedidos);
        List<ParadaRutaResponse> paradas = ruta.getParadas()
                .stream()
                .sorted(Comparator.comparing(ParadaRuta::getOrden))
                .map(this::convertirAParadaResponse)
                .toList();

        response.setParadas(paradas);

        return response;
    }

    private ParadaRutaResponse convertirAParadaResponse(
            ParadaRuta parada) {

        ParadaRutaResponse response = new ParadaRutaResponse();

        response.setId(parada.getId());
        response.setOrden(parada.getOrden());
        response.setTipo(parada.getTipo());
        response.setDireccion(parada.getDireccion());
        response.setLongitud(parada.getUbicacion().getX());
        response.setLatitud(parada.getUbicacion().getY());

        return response;
    }
    
        @Transactional
        public RutaResponse asignarRepartidor(
                Long rutaId,
                Long repartidorId) {

        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una ruta con el ID: " + rutaId
                ));

        if (ruta.getEstado() != EstadoRuta.PLANIFICADA
                && ruta.getEstado() != EstadoRuta.PENDIENTE_OPTIMIZACION
                && ruta.getEstado() != EstadoRuta.OPTIMIZADA) {
                throw new ConflictoException(
                        "No se puede asignar un repartidor a una ruta en este estado"
                );
        }

        if (ruta.getRepartidor() != null) {
                throw new ConflictoException(
                        "La ruta ya tiene un repartidor asignado"
                );
        }

        // Verificar que el repartidor no tenga otra ruta activa.
        List<EstadoRuta> estadosActivos = List.of(
                EstadoRuta.PLANIFICADA,
                EstadoRuta.PENDIENTE_OPTIMIZACION,
                EstadoRuta.OPTIMIZADA,
                EstadoRuta.EN_CURSO
        );

        boolean yaTieneRutaActiva = !rutaRepository
                .findByRepartidorIdAndEstadoIn(repartidorId, estadosActivos)
                .isEmpty();

        if (yaTieneRutaActiva) {
                throw new ConflictoException(
                        "El repartidor ya está asignado a otra ruta activa"
                );
        }

        var repartidor = repartidorRepository
                .findByIdAndDisponibleTrue(repartidorId)
                .orElseThrow(() -> new ConflictoException(
                        "El repartidor no existe o no está disponible"
                ));

        if (!Boolean.TRUE.equals(repartidor.getUsuario().getActivo())) {
                throw new ConflictoException(
                        "El repartidor está desactivado"
                );
        }

        ruta.setRepartidor(repartidor);

        repartidor.setDisponible(false);
        repartidorRepository.save(repartidor);

        Ruta rutaGuardada = rutaRepository.save(ruta);

        return convertirARutaResponse(rutaGuardada);
        }

    
    @Transactional
    public RutaResponse asignarPedido(Long rutaId, Long pedidoId) {

        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una ruta con el ID: " + rutaId
                ));

        if (ruta.getEstado() != EstadoRuta.PLANIFICADA
                && ruta.getEstado() != EstadoRuta.PENDIENTE_OPTIMIZACION
                && ruta.getEstado() != EstadoRuta.OPTIMIZADA) {
            throw new ConflictoException(
                    "No se pueden asignar pedidos a una ruta en este estado"
            );
        }

        if (ruta.getRepartidor() == null) {
            throw new ConflictoException(
                    "Debe asignar un repartidor a la ruta antes de incorporar pedidos"
            );
        }

        var pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un pedido con el ID: " + pedidoId
                ));

        if (pedido.getRuta() != null) {
            throw new ConflictoException(
                    "El pedido ya está asignado a una ruta"
            );
        }

        if (pedido.getEstado() != EstadoPedido.CONFIRMADO
                && pedido.getEstado() != EstadoPedido.EN_PREPARACION) {
            throw new ConflictoException(
                    "Solo se pueden asignar pedidos confirmados o en preparación"
            );
        }

        pedido.setEstadoAnteriorRuta(pedido.getEstado());
        pedido.setRuta(ruta);
        pedido.setEstado(EstadoPedido.ASIGNADO_A_RUTA);

        pedidoRepository.save(pedido);

        return convertirARutaResponse(ruta);
    }
    
    @Transactional
    public RutaResponse retirarPedido(Long rutaId, Long pedidoId) {

        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una ruta con el ID: " + rutaId
                ));

        if (ruta.getEstado() != EstadoRuta.PLANIFICADA
                && ruta.getEstado() != EstadoRuta.PENDIENTE_OPTIMIZACION
                && ruta.getEstado() != EstadoRuta.OPTIMIZADA) {
            throw new ConflictoException(
                    "No se pueden retirar pedidos de una ruta en este estado"
            );
        }

        var pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un pedido con el ID: " + pedidoId
                ));

        if (pedido.getRuta() == null
                || !pedido.getRuta().getId().equals(rutaId)) {
            throw new ConflictoException(
                    "El pedido no está asignado a esta ruta"
            );
        }

        EstadoPedido estadoAnterior = pedido.getEstadoAnteriorRuta();

        if (estadoAnterior == null) {
        estadoAnterior = EstadoPedido.CONFIRMADO;
        }
        RutaPedidoHistorial historial = new RutaPedidoHistorial();
        historial.setRuta(ruta);
        historial.setPedidoId(pedido.getId());

        rutaPedidoHistorialRepository.save(historial);
        pedido.setRuta(null);
        pedido.setEstado(estadoAnterior);
        pedido.setEstadoAnteriorRuta(null);

        pedidoRepository.save(pedido);

        return convertirARutaResponse(ruta);
    }
    @Transactional
    public RutaResponse desasignarRepartidor(Long rutaId) {

        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una ruta con el ID: " + rutaId
                ));

        if (ruta.getEstado() != EstadoRuta.PLANIFICADA
                && ruta.getEstado() != EstadoRuta.PENDIENTE_OPTIMIZACION
                && ruta.getEstado() != EstadoRuta.OPTIMIZADA) {
            throw new ConflictoException(
                    "No se puede desasignar el repartidor de una ruta en este estado"
            );
        }

        if (ruta.getRepartidor() == null) {
            throw new ConflictoException(
                    "La ruta no tiene un repartidor asignado"
            );
        }

        Repartidor repartidor = ruta.getRepartidor();

        ruta.setRepartidor(null);
        rutaRepository.save(ruta);

        List<EstadoRuta> estadosActivos = List.of(
                EstadoRuta.PLANIFICADA,
                EstadoRuta.PENDIENTE_OPTIMIZACION,
                EstadoRuta.OPTIMIZADA,
                EstadoRuta.EN_CURSO
        );

        boolean tieneOtrasRutas = !rutaRepository
                .findByRepartidorIdAndEstadoIn(
                        repartidor.getId(),
                        estadosActivos
                )
                .isEmpty();

        repartidor.setDisponible(
                Boolean.TRUE.equals(repartidor.getUsuario().getActivo())
                        && !tieneOtrasRutas
        );

        repartidorRepository.save(repartidor);

        Ruta rutaGuardada = rutaRepository.save(ruta);

        return convertirARutaResponse(rutaGuardada);
    }

        @Transactional
        public RutaResponse cancelarRuta(Long rutaId) {

        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una ruta con el ID: " + rutaId
                ));

        if (ruta.getEstado() != EstadoRuta.PLANIFICADA
                && ruta.getEstado() != EstadoRuta.PENDIENTE_OPTIMIZACION
                && ruta.getEstado() != EstadoRuta.OPTIMIZADA) {
                throw new ConflictoException(
                        "Solo se pueden cancelar rutas que todavía no se iniciaron"
                );
        }

        List<Pedido> pedidos = pedidoRepository
                .findByRutaIdOrderByFechaCreacionDesc(rutaId);

        for (Pedido pedido : pedidos) {

                // Conservar el vínculo histórico entre ruta y pedido
                RutaPedidoHistorial historial = new RutaPedidoHistorial();
                historial.setRuta(ruta);
                historial.setPedidoId(pedido.getId());

                rutaPedidoHistorialRepository.save(historial);

                // Restaurar el estado original del pedido
                EstadoPedido estadoAnterior = pedido.getEstadoAnteriorRuta();

                if (estadoAnterior == null) {
                estadoAnterior = EstadoPedido.CONFIRMADO;
                }

                pedido.setRuta(null);
                pedido.setEstado(estadoAnterior);
                pedido.setEstadoAnteriorRuta(null);
        }

        pedidoRepository.saveAll(pedidos);

        // Registrar la cancelación
        ruta.setFechaCancelacion(LocalDateTime.now());
        ruta.setEstado(EstadoRuta.CANCELADA);

        // Liberar al repartidor si no tiene otras rutas activas
        Repartidor repartidor = ruta.getRepartidor();

        if (repartidor != null) {

                List<EstadoRuta> estadosActivos = List.of(
                        EstadoRuta.PLANIFICADA,
                        EstadoRuta.PENDIENTE_OPTIMIZACION,
                        EstadoRuta.OPTIMIZADA,
                        EstadoRuta.EN_CURSO
                );

                boolean tieneOtrasRutas = !rutaRepository
                        .findByRepartidorIdAndEstadoIn(
                                repartidor.getId(),
                                estadosActivos
                        )
                        .isEmpty();

                repartidor.setDisponible(
                        Boolean.TRUE.equals(repartidor.getUsuario().getActivo())
                                && !tieneOtrasRutas
                );

                repartidorRepository.save(repartidor);
        }

        Ruta rutaGuardada = rutaRepository.save(ruta);

        return convertirARutaResponse(rutaGuardada);
        }

    @Transactional(readOnly = true)
    public void validarRutaLista(Long rutaId) {

        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una ruta con el ID: " + rutaId
                ));

        if (ruta.getRepartidor() == null) {
            throw new ConflictoException(
                    "La ruta no tiene un repartidor asignado"
            );
        }

        if (!pedidoRepository.existsByRutaId(rutaId)) {
            throw new ConflictoException(
                    "La ruta no tiene pedidos asignados"
            );
        }
    }
        @Transactional
        public RutaResponse iniciarRuta(Long rutaId, String email) {

        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una ruta con el ID: " + rutaId
                ));

        if (ruta.getEstado() != EstadoRuta.OPTIMIZADA) {
                throw new ConflictoException(
                        "Solo se pueden iniciar rutas optimizadas"
                );
        }

        if (ruta.getRepartidor() == null) {
                throw new ConflictoException(
                        "La ruta no tiene un repartidor asignado"
                );
        }
        if (!ruta.getRepartidor().getUsuario().getEmail().equals(email)) {
                throw new ConflictoException(
                        "No tenés autorización para iniciar esta ruta"
                );
        }
        if (!pedidoRepository.existsByRutaId(rutaId)) {
                throw new ConflictoException(
                        "La ruta no tiene pedidos asignados"
                );
        }
        List<com.logistica.logistica.model.entity.Pedido> pedidos =
                pedidoRepository.findByRutaId(rutaId);

        for (var pedido : pedidos) {
        if (pedido.getEstado() != EstadoPedido.ASIGNADO_A_RUTA) {
                throw new ConflictoException(
                        "Todos los pedidos deben estar asignados a la ruta"
                );
        }

        pedido.setEstado(EstadoPedido.EN_CAMINO);
        }

        pedidoRepository.saveAll(pedidos);
        ruta.setFechaInicio(LocalDateTime.now());
        ruta.setEstado(EstadoRuta.EN_CURSO);

        Ruta rutaGuardada = rutaRepository.save(ruta);

        return convertirARutaResponse(rutaGuardada);
    }
    @Transactional
        public RutaResponse confirmarEntrega(
                Long rutaId,
                Long pedidoId,
                String email) {

        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una ruta con el ID: " + rutaId
                ));

        if (ruta.getEstado() != EstadoRuta.EN_CURSO) {
                throw new ConflictoException(
                        "Solo se pueden confirmar entregas de rutas en curso"
                );
        }

        if (ruta.getRepartidor() == null ||
                !ruta.getRepartidor().getUsuario().getEmail().equals(email)) {
                throw new ConflictoException(
                        "No tenés autorización para confirmar entregas de esta ruta"
                );
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un pedido con el ID: " + pedidoId
                ));

        if (pedido.getRuta() == null ||
                !pedido.getRuta().getId().equals(rutaId)) {
                throw new ConflictoException(
                        "El pedido no pertenece a esta ruta"
                );
        }

        if (pedido.getEstado() != EstadoPedido.EN_CAMINO) {
                throw new ConflictoException(
                        "El pedido no está en camino o ya fue entregado"
                );
        }

        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setFechaEntrega(LocalDateTime.now());
        pedido.setEstadoAnteriorRuta(null);

        pedidoRepository.save(pedido);

        return convertirARutaResponse(ruta);
        }
    @Transactional
        public RutaResponse completarRuta(Long rutaId, String email) {

                Ruta ruta = rutaRepository.findById(rutaId)
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "No existe una ruta con el ID: " + rutaId
                        ));

                if (ruta.getEstado() != EstadoRuta.EN_CURSO) {
                        throw new ConflictoException(
                                "Solo se pueden completar rutas que estén en curso"
                        );
                }
                if (ruta.getRepartidor() == null ||
                        !ruta.getRepartidor().getUsuario().getEmail().equals(email)) {
                        throw new ConflictoException(
                                "No tenés autorización para completar esta ruta"
                        );
                }
                List<com.logistica.logistica.model.entity.Pedido> pedidos =
                        pedidoRepository.findByRutaId(rutaId);

                for (var pedido : pedidos) {
                        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
                                throw new ConflictoException(
                                        "Todos los pedidos deben estar entregados para completar la ruta"
                                );
                        }
                        }
                ruta.setFechaFinalizacion(LocalDateTime.now());
                ruta.setEstado(EstadoRuta.COMPLETADA);

                Repartidor repartidor = ruta.getRepartidor();
                if (repartidor == null) {
                        throw new ConflictoException(
                                "La ruta no tiene un repartidor asignado"
                        );
                }

                List<EstadoRuta> estadosActivos = List.of(
                        EstadoRuta.PLANIFICADA,
                        EstadoRuta.PENDIENTE_OPTIMIZACION,
                        EstadoRuta.OPTIMIZADA,
                        EstadoRuta.EN_CURSO
                );

                boolean tieneOtrasRutas = !rutaRepository
                        .findByRepartidorIdAndEstadoIn(
                                repartidor.getId(),
                                estadosActivos
                        )
                        .isEmpty();

                repartidor.setDisponible(
                        Boolean.TRUE.equals(repartidor.getUsuario().getActivo())
                                && !tieneOtrasRutas
                );

                repartidorRepository.save(repartidor);

                Ruta rutaGuardada = rutaRepository.save(ruta);

                return convertirARutaResponse(rutaGuardada);
        }
    @Transactional(readOnly = true)
    public List<Long> consultarPedidosAsignados(Long rutaId) {

        if (!rutaRepository.existsById(rutaId)) {
            throw new RecursoNoEncontradoException(
                    "No existe una ruta con el ID: " + rutaId
            );
        }

        return pedidoRepository.findByRutaId(rutaId)
                .stream()
                .map(pedido -> pedido.getId())
                .toList();
    }
        @Transactional
        public void solicitarOptimizacion(Long rutaId) {

        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la ruta con ID: " + rutaId
                ));

        if (ruta.getEstado() != EstadoRuta.PLANIFICADA) {
                throw new ConflictoException(
                        "Solo se pueden optimizar rutas planificadas."
                );
        }

        validarRutaLista(rutaId);

        // Convertir las paradas de la ruta en DTOs
        List<ParadaOptimizacionDTO> paradasDTO = ruta.getParadas()
                .stream()
                .map(parada -> new ParadaOptimizacionDTO(
                        parada.getId(),
                        parada.getOrden(),
                        parada.getTipo().name(),
                        parada.getDireccion(),
                        parada.getUbicacion().getX(),
                        parada.getUbicacion().getY()
                ))
                .toList();

        // Construir la solicitud completa
        SolicitudOptimizacionDTO solicitud =
                new SolicitudOptimizacionDTO(rutaId, paradasDTO);

        // Cambiar el estado de la ruta
        ruta.setEstado(EstadoRuta.PENDIENTE_OPTIMIZACION);

        rutaRepository.save(ruta);

        // Enviar la solicitud a RabbitMQ
        optimizacionPublisher.enviarSolicitud(solicitud);
        }
        @Transactional
        public void aplicarResultadoOptimizacion(
                ResultadoOptimizacionDTO resultado) {

        Ruta ruta = rutaRepository.findById(resultado.getRutaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la ruta con ID: " + resultado.getRutaId()
                ));

        if (ruta.getEstado() != EstadoRuta.PENDIENTE_OPTIMIZACION) {
                throw new ConflictoException(
                        "La ruta no se encuentra pendiente de optimización."
                );
        }

        List<ParadaRuta> paradas = ruta.getParadas();
        List<Long> idsOrdenados = resultado.getParadasOrdenadas();

        // Verificar que no falten paradas ni existan identificadores repetidos
        Set<Long> idsOriginales = paradas.stream()
                .map(ParadaRuta::getId)
                .collect(Collectors.toSet());

        Set<Long> idsRecibidos = new HashSet<>(idsOrdenados);

        if (idsOrdenados.size() != paradas.size()
                || idsRecibidos.size() != idsOrdenados.size()
                || !idsOriginales.equals(idsRecibidos)) {

                throw new ConflictoException(
                        "El resultado contiene paradas faltantes, duplicadas o desconocidas."
                );
        }

        // Aplicar el nuevo orden
        Map<Long, ParadaRuta> paradasPorId = paradas.stream()
                .collect(Collectors.toMap(
                        ParadaRuta::getId,
                        parada -> parada
                ));

        for (int i = 0; i < idsOrdenados.size(); i++) {
                ParadaRuta parada = paradasPorId.get(idsOrdenados.get(i));
                parada.setOrden(i + 1);
        }

        // Marcar la ruta como optimizada
        ruta.setEstado(EstadoRuta.OPTIMIZADA);

        rutaRepository.save(ruta);
        }
        @Transactional(readOnly = true)
        public List<RutaHistorialResponse> consultarHistorialRepartidor(String email) {

        Repartidor repartidor = repartidorRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un repartidor asociado al usuario autenticado"));

        return rutaRepository.findByRepartidorIdOrderByFechaCreacionDesc(
                        repartidor.getId()
                )
                .stream()
                .map(ruta -> {
                        RutaHistorialResponse response = new RutaHistorialResponse();

                        response.setRutaId(ruta.getId());
                        response.setRepartidorId(
                                ruta.getRepartidor() != null
                                        ? ruta.getRepartidor().getId()
                                        : null
                        );
                        response.setNombre(ruta.getNombre());
                        response.setEstado(ruta.getEstado());
                        response.setFechaCreacion(ruta.getFechaCreacion());
                        response.setFechaInicio(ruta.getFechaInicio());
                        response.setFechaFinalizacion(ruta.getFechaFinalizacion());
                        response.setFechaCancelacion(ruta.getFechaCancelacion());

                        response.setPedidosIds(
                                java.util.stream.Stream.concat(
                                        pedidoRepository.findByRutaId(ruta.getId())
                                                .stream()
                                                .map(Pedido::getId),
                                        rutaPedidoHistorialRepository.findByRutaId(ruta.getId())
                                                .stream()
                                                .map(RutaPedidoHistorial::getPedidoId)
                                )
                                .distinct()
                                .toList()
                        );

                        return response;
                })
                .toList();
        }
        @Transactional(readOnly = true)
        public List<RutaResponse> consultarHistorialRutas(
                List<EstadoRuta> estados,
                LocalDateTime desde,
                LocalDateTime hasta) {

        if (estados == null || estados.isEmpty()) {
                throw new ConflictoException(
                        "Debe indicar al menos un estado de ruta");
        }

        if (desde == null || hasta == null || desde.isAfter(hasta)) {
                throw new ConflictoException(
                        "El período de consulta no es válido");
        }

        if (desde.isAfter(LocalDateTime.now())
                || hasta.isAfter(LocalDateTime.now())) {
                throw new ConflictoException(
                        "El período del historial no puede incluir fechas futuras");
        }

        return rutaRepository
                .findByEstadoInAndFechaCreacionBetweenOrderByFechaCreacionDesc(
                        estados, desde, hasta)
                .stream()
                .map(this::convertirARutaResponse)
                .toList();
        }
}