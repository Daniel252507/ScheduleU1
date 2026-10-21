package com.scheduleu.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarUsuarioRequest {
    private String nombre;
    private String correo;
    private String contrasena;
    private String rol;
}
