package com.logistica.logistica.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.logistica.logistica.model.enums.Rol;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean activo = true;

    // Relación con el perfil de cliente
    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Cliente cliente;

    // Relación con el perfil de repartidor
    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Repartidor repartidor;
}
