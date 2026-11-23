package com.logistica.logistica.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "ubicaciones_repartidores")
@Getter
@Setter
public class UbicacionRepartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Repartidor al que pertenece la ubicación
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Repartidor repartidor;

    // Coordenadas geográficas
    @Column(
        columnDefinition = "geometry(Point,4326)",
        nullable = false
    )
    private Point ubicacion;

    // Fecha y hora del registro
    @Column(nullable = false)
    private LocalDateTime fechaRegistro;
}