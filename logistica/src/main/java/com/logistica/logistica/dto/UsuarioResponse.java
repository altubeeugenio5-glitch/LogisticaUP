package com.logistica.logistica.dto;

import com.logistica.logistica.model.enums.Rol;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Rol rol;
    private Boolean activo;
}