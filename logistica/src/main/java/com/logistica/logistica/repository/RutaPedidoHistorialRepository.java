
package com.logistica.logistica.repository;

import com.logistica.logistica.model.entity.RutaPedidoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutaPedidoHistorialRepository
        extends JpaRepository<RutaPedidoHistorial, Long> {

    List<RutaPedidoHistorial> findByRutaId(Long rutaId);
}
