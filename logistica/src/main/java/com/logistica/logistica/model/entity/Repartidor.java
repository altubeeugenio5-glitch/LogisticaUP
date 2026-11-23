
package com.logistica.logistica.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "repartidores")
@Getter
@Setter
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cuenta del sistema asociada al repartidor
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // Datos específicos del repartidor
    @Column(length = 20)
    private String telefono;

    @Column(nullable = false)
    private Boolean disponible = false;
}
