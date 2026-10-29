package com.scheduleu.infrastructure.adapter.in.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cuerpo del inicio de sesion federado con Google.
 * El frontend envia el ID token que entrega Google Identity Services.
 */
@Data
@NoArgsConstructor
public class GoogleLoginRequest {
    private String idToken;
}
