package com.scheduleu.domain.port.in;

import com.scheduleu.infrastructure.adapter.in.web.dto.AuthResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.RegistroGoogleRequest;

/**
 * Caso de uso de autenticacion federada (OpenID Connect).
 */
public interface GoogleAuthUseCase {

    /**
     * Autentica a un usuario a partir del ID token emitido por Google.
     *
     * Si el correo ya pertenece a una cuenta de ScheduleU devuelve la sesion
     * propia de la aplicacion (JWT + datos del usuario). Si el correo no esta
     * registrado, devuelve una respuesta con {@code requiereRegistro = true}
     * para que el usuario complete su alta como estudiante.
     *
     * @param idToken Token entregado por Google Identity Services.
     * @throws RuntimeException Si el token no es valido.
     */
    AuthResponse loginConGoogle(String idToken);

    /**
     * Completa el registro de un estudiante que ingresa por primera vez con
     * Google y devuelve la sesion ya iniciada.
     *
     * @param request ID token de Google, codigo academico y semestre.
     * @throws RuntimeException Si el token no es valido, el codigo ya existe
     *                          o faltan datos obligatorios.
     */
    AuthResponse registrarEstudianteConGoogle(RegistroGoogleRequest request);
}
