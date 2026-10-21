package com.scheduleu.infrastructure.adapter.in.web.dto;

import com.scheduleu.domain.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tipo = "Bearer";
    private Integer idUsuario;
    private String nombre;
    private String correo;
    private Usuario.RolUsuario rol;
}
