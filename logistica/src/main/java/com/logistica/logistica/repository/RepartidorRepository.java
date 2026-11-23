
package com.logistica.logistica.repository;

import com.logistica.logistica.model.entity.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {

    Optional<Repartidor> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    List<Repartidor> findByDisponible(Boolean disponible);

    Optional<Repartidor> findByIdAndDisponibleTrue(Long id);
    Optional<Repartidor> findByUsuarioEmail(String email);
}
