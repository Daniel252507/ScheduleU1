package com.scheduleu.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos para completar el registro de un estudiante que se autentica por
 * primera vez con su cuenta de Google. El ID token se reenvia para volver a
 * verificar el correo antes de crear la cuenta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroGoogleRequest {
    private String idToken;
    /** Nombre del estudiante. Si viene vacio se usa el del perfil de Google. */
    private String nombre;
    private String codigo;
    private Integer semestre;
}
