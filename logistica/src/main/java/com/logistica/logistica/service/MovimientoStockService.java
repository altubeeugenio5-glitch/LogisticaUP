// gestiona los movimientos de inventario: registra ingresos y egresos, actualiza el stock de los productos y evita egresos cuando no hay existencias suficientes.
package com.logistica.logistica.service;

import com.logistica.logistica.dto.CrearMovimientoStockRequest;
import com.logistica.logistica.exception.ConflictoException;
import com.logistica.logistica.exception.RecursoNoEncontradoException;
import com.logistica.logistica.model.entity.MovimientoStock;
import com.logistica.logistica.model.entity.Producto;
import com.logistica.logistica.model.enums.TipoMovimientoStock;
import com.logistica.logistica.repository.MovimientoStockRepository;
import com.logistica.logistica.repository.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final ProductoRepository productoRepository;

    public MovimientoStockService(
            MovimientoStockRepository movimientoStockRepository,
            ProductoRepository productoRepository) {
        this.movimientoStockRepository = movimientoStockRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public MovimientoStock registrarMovimiento(
            Long productoId,
            CrearMovimientoStockRequest request) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un producto con ID: " + productoId));

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new ConflictoException(
                    "No se puede modificar el stock de un producto inactivo");
        }

        int stockAnterior = producto.getStock();
        int cantidad = request.getCantidad();
        int stockPosterior;

        if (request.getTipo() == TipoMovimientoStock.INGRESO) {
            stockPosterior = stockAnterior + cantidad;

        } else if (request.getTipo() == TipoMovimientoStock.EGRESO) {

            if (stockAnterior < cantidad) {
                throw new ConflictoException(
                        "No hay stock suficiente para realizar el egreso");
            }

            stockPosterior = stockAnterior - cantidad;

        } else {
            throw new IllegalArgumentException(
                    "Tipo de movimiento no válido");
        }

        producto.setStock(stockPosterior);
        productoRepository.save(producto);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProducto(producto);
        movimiento.setTipo(request.getTipo());
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockPosterior(stockPosterior);
        movimiento.setObservacion(request.getObservacion());

        return movimientoStockRepository.save(movimiento);
    }

    @Transactional(readOnly = true)
    public List<MovimientoStock> listarMovimientos() {
        return movimientoStockRepository.findAllByOrderByFechaMovimientoDesc();
    }

    @Transactional(readOnly = true)
    public List<MovimientoStock> listarMovimientosPorProducto(
            Long productoId) {

        if (!productoRepository.existsById(productoId)) {
            throw new RecursoNoEncontradoException(
                    "No existe un producto con ID: " + productoId);
        }

        return movimientoStockRepository
                .findByProductoIdOrderByFechaMovimientoDesc(productoId);
    }
}