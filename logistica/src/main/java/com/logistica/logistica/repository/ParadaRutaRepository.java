package com.logistica.logistica.repository;

import com.logistica.logistica.model.entity.ParadaRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParadaRutaRepository extends JpaRepository<ParadaRuta, Long> {

    List<ParadaRuta> findByRutaIdOrderByOrdenAsc(Long rutaId);

    boolean existsByRutaIdAndOrden(Long rutaId, Integer orden);
}