
package com.logistica.logistica.repository;

import com.logistica.logistica.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Producto> findByActivo(Boolean activo);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    List<Producto> findByActivoTrueAndStockGreaterThan(Integer stock);

    List<Producto> findByActivoTrueAndStockLessThanEqual(Integer stock);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();

}
