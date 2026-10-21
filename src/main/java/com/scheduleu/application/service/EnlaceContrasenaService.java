package com.scheduleu.application.service;

import com.scheduleu.domain.model.TokenRecuperacion;
import com.scheduleu.domain.model.Usuario;
import com.scheduleu.domain.port.in.EnlaceContrasenaUseCase;
import com.scheduleu.domain.port.out.NotificacionPort;
import com.scheduleu.domain.port.out.TokenRecuperacionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EnlaceContrasenaService implements EnlaceContrasenaUseCase {

    private final TokenRecuperacionPort tokenRepository;
    private final NotificacionPort notificacion;
    private final String frontendUrl;
    private final long vigenciaHoras;

    public EnlaceContrasenaService(TokenRecuperacionPort tokenRepository,
                                   NotificacionPort notificacion,
                                   @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl,
                                   @Value("${app.token.vigencia-horas:48}") long vigenciaHoras) {
        this.tokenRepository = tokenRepository;
        this.notificacion = notificacion;
        this.frontendUrl = frontendUrl;
        this.vigenciaHoras = vigenciaHoras;
    }

    @Override
    public void enviarEnlaceEstablecimiento(Usuario usuario) {
        if (usuario == null || usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            return;
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        TokenRecuperacion registro = new TokenRecuperacion();
        registro.setToken(token);
        registro.setUsuario(usuario);
        registro.setExpiraEn(LocalDateTime.now().plusHours(vigenciaHoras));
        tokenRepository.save(registro);

        String enlace = frontendUrl + "/establecer-contrasena?token=" + token;
        notificacion.enviarEnlaceEstablecimientoContrasena(usuario.getCorreo(), usuario.getNombre(), enlace);
    }
}
