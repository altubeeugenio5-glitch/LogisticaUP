
package com.logistica.logistica.model.entity;

import com.logistica.logistica.model.enums.TipoParada;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

@Entity
@Table(
    name = "paradas_ruta",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_ruta_orden",
            columnNames = {"ruta_id", "orden"}
        )
    }
)
@Getter
@Setter
public class ParadaRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(nullable = false)
    private Integer orden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoParada tipo;

    @Column(nullable = false, length = 250)
    private String direccion;

    @Column(
        name = "ubicacion",
        nullable = false,
        columnDefinition = "geometry(Point,4326)"
    )
    private Point ubicacion;
}
