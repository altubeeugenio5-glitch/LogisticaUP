// contiene la lógica de negocio para administrar productos: permite crearlos, consultarlos, modificarlos, desactivarlos y reactivarlos. También verifica códigos duplicados y permite identificar productos con stock bajo o con existencias disponibles.
package com.logistica.logistica.service;

import com.logistica.logistica.model.entity.Producto;
import com.logistica.logistica.repository.ProductoRepository;
import com.logistica.logistica.exception.ConflictoException;
import com.logistica.logistica.exception.RecursoNoEncontradoException;
import com.logistica.logistica.dto.ActualizarProductoRequest;
import com.logistica.logistica.dto.CrearProductoRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un producto con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Producto buscarPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un producto con código: " + codigo));
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorEstado(Boolean activo) {
        return productoRepository.findByActivo(activo);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    @Transactional
    public Producto crearProducto(CrearProductoRequest request) {

        if (productoRepository.existsByCodigo(request.getCodigo())) {
            throw new ConflictoException(
                    "Ya existe un producto con el código: " + request.getCodigo());
        }

        Producto producto = new Producto();

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCodigo(request.getCodigo());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setActivo(true);

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizarProducto(
            Long id,
            ActualizarProductoRequest request) {

        Producto producto = buscarPorId(id);

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto desactivarProducto(Long id) {

        Producto producto = buscarPorId(id);
        producto.setActivo(false);

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto reactivarProducto(Long id) {

        Producto producto = buscarPorId(id);
        producto.setActivo(true);

        return productoRepository.save(producto);
    }
    @Transactional(readOnly = true)
    public List<Producto> listarProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }
    @Transactional(readOnly = true)
    public List<Producto> listarProductosConStockDisponible() {
        return productoRepository.findByActivoTrueAndStockGreaterThan(0);
    }
}