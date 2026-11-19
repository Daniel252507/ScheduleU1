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
    /**
     * Marca que la cuenta de Google no existe todavia en ScheduleU y que el
     * usuario debe completar su registro como estudiante (codigo y semestre).
     * Cuando es true, el resto de campos de sesion vienen vacios.
     */
    private boolean requiereRegistro;
}
