package com.scheduleu.infrastructure.adapter.in.web;

import com.scheduleu.domain.port.in.AsistenteUseCase;
import com.scheduleu.infrastructure.adapter.in.web.dto.AsistenteConsultaRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.AsistenteRespuesta;
import com.scheduleu.infrastructure.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada (REST) del asistente academico con IA. El horario que
 * se usa como contexto se obtiene en el servidor a partir del JWT.
 */
@RestController
@RequestMapping("/api/asistente")
@RequiredArgsConstructor
public class AsistenteController {

    private final AsistenteUseCase service;
    private final JwtTokenProvider jwtTokenProvider;

    private String extraerCorreo(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new RuntimeException("No autorizado");
        return jwtTokenProvider.getCorreoFromToken(header.substring(7));
    }

    @PostMapping("/consultar")
    public ResponseEntity<AsistenteRespuesta> consultar(@RequestBody AsistenteConsultaRequest body,
                                                        HttpServletRequest req) {
        return ResponseEntity.ok(service.consultar(extraerCorreo(req), body.getPregunta()));
    }
}
