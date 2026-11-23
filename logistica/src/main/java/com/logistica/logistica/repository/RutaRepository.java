package com.logistica.logistica.repository;

import com.logistica.logistica.model.entity.Ruta;
import com.logistica.logistica.model.enums.EstadoRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {

    List<Ruta> findByEstado(EstadoRuta estado);

    List<Ruta> findByNombreContainingIgnoreCase(String nombre);
    List<Ruta> findByRepartidorIdAndEstadoIn(
        Long repartidorId,
        List<EstadoRuta> estados
    );
    List<Ruta> findByRepartidorIdOrderByFechaCreacionDesc(Long repartidorId);

    List<Ruta> findByRepartidorIdAndFechaCreacionBetweenOrderByFechaCreacionDesc(
        Long repartidorId,
        java.time.LocalDateTime desde,
        java.time.LocalDateTime hasta
    );

    List<Ruta> findByEstadoInOrderByFechaCreacionDesc(
        List<EstadoRuta> estados
    );
    List<Ruta> findByEstadoInAndFechaCreacionBetweenOrderByFechaCreacionDesc(
            List<EstadoRuta> estados,
            java.time.LocalDateTime desde,
            java.time.LocalDateTime hasta
    );
}