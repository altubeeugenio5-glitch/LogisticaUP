// contiene la lógica de negocio para gestionar los pedidos: permite crearlos, consultar su información, actualizar estados y cancelarlos. También controla la disponibilidad de productos y los movimientos de stock asociados, calcula los totales y gestiona los historiales de pedidos y entregas.
package com.logistica.logistica.service;

import com.logistica.logistica.dto.CrearDetallePedidoRequest;
import com.logistica.logistica.dto.CrearMovimientoStockRequest;
import com.logistica.logistica.dto.CrearPedidoRequest;
import com.logistica.logistica.exception.ConflictoException;
import com.logistica.logistica.exception.RecursoNoEncontradoException;
import com.logistica.logistica.model.entity.Cliente;
import com.logistica.logistica.model.entity.DetallePedido;
import com.logistica.logistica.model.entity.Pedido;
import com.logistica.logistica.model.entity.Producto;
import com.logistica.logistica.model.enums.EstadoPedido;
import com.logistica.logistica.model.enums.TipoMovimientoStock;
import com.logistica.logistica.repository.ClienteRepository;
import com.logistica.logistica.repository.PedidoRepository;
import com.logistica.logistica.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.logistica.logistica.dto.EstadoPedidoResponse;
import com.logistica.logistica.dto.PedidoClienteResponse;
import com.logistica.logistica.dto.PedidoAdminResponse;
import com.logistica.logistica.dto.PedidoDetalleAdminResponse;
import com.logistica.logistica.dto.PedidoDetalleClienteResponse;
import com.logistica.logistica.dto.RutaResponse;
import com.logistica.logistica.service.RutaService;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockService movimientoStockService;
    private final RutaService rutaService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            MovimientoStockService movimientoStockService,
                RutaService rutaService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.movimientoStockService = movimientoStockService;
        this.rutaService = rutaService;
    }

    @Transactional
    public Pedido crearPedido(CrearPedidoRequest request, String email) {

        Cliente cliente = clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un cliente asociado al usuario autenticado"));

        if (!Boolean.TRUE.equals(cliente.getUsuario().getActivo())) {
            throw new ConflictoException(
                    "No se puede crear un pedido para un cliente inactivo");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;

        for (CrearDetallePedidoRequest item : request.getDetalles()) {

            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No se encontró el producto con ID: " + item.getProductoId()));

            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new ConflictoException(
                        "El producto " + producto.getNombre() + " está inactivo");
            }

            CrearMovimientoStockRequest movimiento = new CrearMovimientoStockRequest();
            movimiento.setTipo(TipoMovimientoStock.EGRESO);
            movimiento.setCantidad(item.getCantidad());
            movimiento.setObservacion("Egreso por creación de pedido");

            movimientoStockService.registrarMovimiento(producto.getId(), movimiento);

            BigDecimal precioUnitario = producto.getPrecio();
            BigDecimal subtotal = precioUnitario.multiply(
                    BigDecimal.valueOf(item.getCantidad()));

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);

            pedido.getDetalles().add(detalle);

            total = total.add(subtotal);
        }

        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el pedido con ID: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<Pedido> listarPorCliente(Long clienteId) {

        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el cliente con ID: " + clienteId);
        }

        return pedidoRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }

    @Transactional
    public Pedido actualizarEstado(Long id, EstadoPedido nuevoEstado) {

        Pedido pedido = buscarPorId(id);

        if (pedido.getEstado() == EstadoPedido.CANCELADO ||
                pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new ConflictoException(
                    "No se puede modificar el estado de un pedido cancelado o entregado"
            );
        }

        boolean transicionValida =
                (pedido.getEstado() == EstadoPedido.PENDIENTE
                        && nuevoEstado == EstadoPedido.CONFIRMADO)
                ||
                (pedido.getEstado() == EstadoPedido.CONFIRMADO
                        && nuevoEstado == EstadoPedido.EN_PREPARACION);

        if (!transicionValida) {
            throw new ConflictoException(
                    "Transición de estado no permitida. Las transiciones de ruta "
                            + "se gestionan desde el módulo de rutas"
            );
        }

        pedido.setEstado(nuevoEstado);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cancelarPedido(Long id) {

        Pedido pedido = buscarPorId(id);
        if (pedido.getRuta() != null) {
            throw new ConflictoException(
                    "No se puede cancelar un pedido asignado a una ruta. "
                            + "Primero debe retirarse de la ruta"
            );
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new ConflictoException("El pedido ya está cancelado");
        }

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new ConflictoException(
                    "No se puede cancelar un pedido que ya fue entregado");
        }

        for (DetallePedido detalle : pedido.getDetalles()) {

            CrearMovimientoStockRequest movimiento =
                    new CrearMovimientoStockRequest();

            movimiento.setTipo(TipoMovimientoStock.INGRESO);
            movimiento.setCantidad(detalle.getCantidad());
            movimiento.setObservacion(
                    "Devolución de stock por cancelación del pedido " + pedido.getId());

            movimientoStockService.registrarMovimiento(
                    detalle.getProducto().getId(), movimiento);
        }

        pedido.setEstado(EstadoPedido.CANCELADO);

        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public EstadoPedidoResponse consultarEstado(Long pedidoId) {

        Pedido pedido = buscarPorId(pedidoId);

        EstadoPedidoResponse response = new EstadoPedidoResponse();

        response.setPedidoId(pedido.getId());
        response.setEstado(pedido.getEstado());
        response.setFechaCreacion(pedido.getFechaCreacion());
        response.setTotal(pedido.getTotal());

        return response;
    }
    @Transactional(readOnly = true)
    public EstadoPedidoResponse consultarEstadoCliente(Long pedidoId, String email) {

        Cliente cliente = clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un cliente asociado al usuario autenticado"));

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el pedido con ID: " + pedidoId));

        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new RecursoNoEncontradoException(
                    "No tenés permiso para consultar este pedido");
        }

        EstadoPedidoResponse response = new EstadoPedidoResponse();

        response.setPedidoId(pedido.getId());
        response.setEstado(pedido.getEstado());
        response.setFechaCreacion(pedido.getFechaCreacion());
        response.setTotal(pedido.getTotal());

        return response;
    }
    @Transactional(readOnly = true)
    public List<PedidoClienteResponse> consultarPedidosCliente(Long clienteId) {

        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el cliente con ID: " + clienteId);
        }

        return pedidoRepository.findByClienteId(clienteId)
                .stream()
                .map(pedido -> {
                    PedidoClienteResponse response = new PedidoClienteResponse();

                    response.setPedidoId(pedido.getId());
                    response.setEstado(pedido.getEstado());
                    response.setFechaCreacion(pedido.getFechaCreacion());
                    response.setTotal(pedido.getTotal());

                    return response;
                })
                .toList();
    }
    public List<PedidoClienteResponse> consultarMisPedidos(String email) {

        Cliente cliente = clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un cliente asociado al usuario autenticado"));

        return pedidoRepository.findByClienteIdOrderByFechaCreacionDesc(cliente.getId())
                .stream()
                .map(pedido -> {
                    PedidoClienteResponse response = new PedidoClienteResponse();
                    response.setPedidoId(pedido.getId());
                    response.setEstado(pedido.getEstado());
                    response.setFechaCreacion(pedido.getFechaCreacion());
                    response.setTotal(pedido.getTotal());
                    return response;
                })
                .toList();
    }
    @Transactional(readOnly = true)
    public List<PedidoClienteResponse> consultarHistorialEntregasCliente(String email) {

        Cliente cliente = clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un cliente asociado al usuario autenticado"));

        return pedidoRepository.findByClienteIdAndEstadoOrderByFechaCreacionDesc(
                        cliente.getId(),
                        EstadoPedido.ENTREGADO
                )
                .stream()
                .map(pedido -> {
                    PedidoClienteResponse response = new PedidoClienteResponse();

                    response.setPedidoId(pedido.getId());
                    response.setEstado(pedido.getEstado());
                    response.setFechaCreacion(pedido.getFechaCreacion());
                    response.setTotal(pedido.getTotal());

                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoAdminResponse> listarPedidosAdmin() {

        return pedidoRepository.findAll()
                .stream()
                .map(this::convertirPedidoAdmin)
                .toList();
    }

    private PedidoAdminResponse convertirPedidoAdmin(Pedido pedido) {

        List<PedidoDetalleAdminResponse> detalles = pedido.getDetalles()
                .stream()
                .map(detalle -> new PedidoDetalleAdminResponse(
                        detalle.getProducto().getId(),
                        detalle.getProducto().getNombre(),
                        detalle.getProducto().getCodigo(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ))
                .toList();

        return new PedidoAdminResponse(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getCliente().getUsuario().getNombre(),
                pedido.getCliente().getUsuario().getApellido(),
                pedido.getCliente().getUsuario().getEmail(),
                pedido.getCliente().getTelefono(),
                pedido.getEstado(),
                pedido.getFechaCreacion(),
                pedido.getTotal(),
                pedido.getRuta() != null ? pedido.getRuta().getId() : null,
                detalles
        );
    }

    @Transactional(readOnly = true)
    public List<PedidoDetalleClienteResponse> consultarDetallesPedidoCliente(
            Long pedidoId,
            String email) {

        Cliente cliente = clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un cliente asociado al usuario autenticado"));

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el pedido con ID: " + pedidoId));

        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new RecursoNoEncontradoException(
                    "No tenés permiso para consultar este pedido");
        }

        return pedido.getDetalles()
                .stream()
                .map(detalle -> new PedidoDetalleClienteResponse(
                        detalle.getProducto().getId(),
                        detalle.getProducto().getNombre(),
                        detalle.getProducto().getCodigo(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ))
                .toList();
    }
    @Transactional(readOnly = true)
        public RutaResponse consultarRutaPedido(Long pedidoId, String email) {

        Cliente cliente = clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un cliente asociado al usuario autenticado"));

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el pedido con ID: " + pedidoId));

        if (!pedido.getCliente().getId().equals(cliente.getId())) {
                throw new RecursoNoEncontradoException(
                        "No tenés permiso para consultar este pedido");
        }

        if (pedido.getRuta() == null) {
                throw new RecursoNoEncontradoException(
                        "El pedido todavía no tiene una ruta asignada");
        }

        return rutaService.convertirARutaResponse(pedido.getRuta());
        }
}