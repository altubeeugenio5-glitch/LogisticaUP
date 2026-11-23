// es el controlador REST encargado de gestionar el inventario.
// Su función es permitir registrar movimientos de stock, consultar el historial de movimientos y controlar las cantidades disponibles de cada producto.
package com.logistica.logistica.controller;

import com.logistica.logistica.dto.CrearMovimientoStockRequest;
import com.logistica.logistica.model.entity.MovimientoStock;
import com.logistica.logistica.service.MovimientoStockService;
import com.logistica.logistica.model.entity.Producto;
import com.logistica.logistica.service.ProductoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final MovimientoStockService movimientoStockService;
    private final ProductoService productoService;

    public InventarioController(
            MovimientoStockService movimientoStockService,
            ProductoService productoService) {

        this.movimientoStockService = movimientoStockService;
        this.productoService = productoService;
    }

    @PostMapping("/productos/{productoId}/movimientos")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoStock registrarMovimiento(
            @PathVariable Long productoId,
            @Valid @RequestBody CrearMovimientoStockRequest request) {

        return movimientoStockService.registrarMovimiento(
                productoId, request);
    }

    @GetMapping("/movimientos")
    public List<MovimientoStock> listarMovimientos() {
        return movimientoStockService.listarMovimientos();
    }

    @GetMapping("/productos/{productoId}/movimientos")
    public List<MovimientoStock> listarMovimientosPorProducto(
            @PathVariable Long productoId) {

        return movimientoStockService
                .listarMovimientosPorProducto(productoId);
    }
    
    @GetMapping("/stock-disponible")
    public List<Producto> listarStockDisponible() {
        return productoService.listarProductosConStockDisponible();
    }

    @GetMapping("/stock-bajo")
    public List<Producto> listarStockBajo() {
        return productoService.listarProductosConStockBajo();
    }

    @GetMapping("/productos/{productoId}/stock")
    public Producto consultarStock(@PathVariable Long productoId) {
        return productoService.buscarPorId(productoId);
    }
}