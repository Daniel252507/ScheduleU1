package com.scheduleu.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Identidad de una persona verificada por un proveedor externo (OpenID Connect).
 *
 * Es dominio puro: no conoce Google, ni HTTP, ni ninguna libreria concreta.
 * Cada proveedor de identidad que se integre debe producir una instancia de
 * esta clase a partir de su propio token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentidadExterna {

    /** Correo electronico ya verificado por el proveedor. */
    private String correo;

    /** Nombre completo tal como lo reporta el proveedor. */
    private String nombre;

    /** Indica si el proveedor confirmo que el correo pertenece a la persona. */
    private boolean correoVerificado;
}
