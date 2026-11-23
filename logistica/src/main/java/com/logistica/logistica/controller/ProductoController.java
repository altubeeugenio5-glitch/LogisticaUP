// es el controlador REST encargado de administrar los productos
// Permite crear productos, consultarlos, buscarlos por diferentes criterios, actualizar sus datos y cambiar su estado de actividad.
package com.logistica.logistica.controller;

import com.logistica.logistica.dto.ActualizarProductoRequest;
import com.logistica.logistica.dto.CrearProductoRequest;
import com.logistica.logistica.model.entity.Producto;
import com.logistica.logistica.service.ProductoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crearProducto(
            @Valid @RequestBody CrearProductoRequest request) {
        return productoService.crearProducto(request);
    }

    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.listarProductos();
    }

    @GetMapping("/{id}")
    public Producto buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    @GetMapping("/codigo/{codigo}")
    public Producto buscarPorCodigo(@PathVariable String codigo) {
        return productoService.buscarPorCodigo(codigo);
    }

    @GetMapping("/estado/{activo}")
    public List<Producto> listarPorEstado(@PathVariable Boolean activo) {
        return productoService.listarPorEstado(activo);
    }

    @GetMapping("/buscar")
    public List<Producto> buscarPorNombre(@RequestParam String nombre) {
        return productoService.buscarPorNombre(nombre);
    }

    @PutMapping("/{id}")
    public Producto actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarProductoRequest request) {
        return productoService.actualizarProducto(id, request);
    }
        
    @PatchMapping("/{id}/desactivar")
    public Producto desactivarProducto(@PathVariable Long id) {
        return productoService.desactivarProducto(id);
    }

    @PatchMapping("/{id}/reactivar")
    public Producto reactivarProducto(@PathVariable Long id) {
        return productoService.reactivarProducto(id);
    }

}