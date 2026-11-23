
package com.logistica.logistica.model.entity;

import com.logistica.logistica.model.enums.EstadoRuta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutas")
@Getter
@Setter
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoRuta estado = EstadoRuta.PLANIFICADA;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;
    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Repartidor repartidor;
    @OneToMany(
        mappedBy = "ruta",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("orden ASC")
    private List<ParadaRuta> paradas = new ArrayList<>();
}
