
package com.logistica.logistica.repository;

import com.logistica.logistica.model.entity.UbicacionRepartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UbicacionRepartidorRepository
        extends JpaRepository<UbicacionRepartidor, Long> {

    List<UbicacionRepartidor> findByRepartidorIdOrderByFechaRegistroDesc(
            Long repartidorId
    );

    Optional<UbicacionRepartidor> findFirstByRepartidorIdOrderByFechaRegistroDesc(
            Long repartidorId
    );

    
    @Query(value = """
        SELECT u.*
        FROM ubicaciones_repartidores u
        INNER JOIN (
            SELECT DISTINCT ON (repartidor_id)
                   repartidor_id, id
            FROM ubicaciones_repartidores
            ORDER BY repartidor_id, fecha_registro DESC, id DESC
        ) ultima
        ON u.id = ultima.id
        INNER JOIN repartidores r
        ON r.id = u.repartidor_id
        WHERE r.disponible = TRUE
        AND ST_DWithin(
            u.ubicacion::geography,
            ST_SetSRID(
                ST_MakePoint(:longitud, :latitud),
                4326
            )::geography,
            :radioMetros
        )
        ORDER BY ST_Distance(
            u.ubicacion::geography,
            ST_SetSRID(
                ST_MakePoint(:longitud, :latitud),
                4326
            )::geography
        )
        """, nativeQuery = true)
    List<UbicacionRepartidor> buscarCercanos(
            @Param("longitud") double longitud,
            @Param("latitud") double latitud,
            @Param("radioMetros") double radioMetros
    );
    @Query("""
        SELECT u
        FROM UbicacionRepartidor u
        WHERE u.repartidor.id = :repartidorId
        ORDER BY u.fechaRegistro DESC, u.id DESC
        """)
    List<UbicacionRepartidor> buscarUltimasUbicaciones(
            @Param("repartidorId") Long repartidorId,
            org.springframework.data.domain.Pageable pageable
    );
}
