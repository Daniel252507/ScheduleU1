package com.scheduleu.domain.port.in;

import com.scheduleu.infrastructure.adapter.in.web.dto.AuthResponse;

/**
 * Caso de uso de autenticacion federada (OpenID Connect).
 */
public interface GoogleAuthUseCase {

    /**
     * Autentica a un usuario a partir del ID token emitido por Google y
     * devuelve la sesion propia de ScheduleU (JWT + datos del usuario).
     *
     * @param idToken Token entregado por Google Identity Services.
     * @throws RuntimeException Si el token no es valido o el correo no
     *                          corresponde a una cuenta registrada.
     */
    AuthResponse loginConGoogle(String idToken);
}
