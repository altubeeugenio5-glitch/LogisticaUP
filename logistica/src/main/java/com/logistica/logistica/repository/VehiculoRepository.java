
package com.logistica.logistica.repository;

import com.logistica.logistica.model.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByPatente(String patente);

    boolean existsByPatente(String patente);

    List<Vehiculo> findByActivo(Boolean activo);

    List<Vehiculo> findByDisponible(Boolean disponible);

    List<Vehiculo> findByActivoAndDisponible(Boolean activo, Boolean disponible);
}
