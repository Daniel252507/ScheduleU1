package com.scheduleu.infrastructure.adapter.in.web;

import com.scheduleu.domain.port.in.ExportarHorarioCalendarioUseCase;
import com.scheduleu.infrastructure.adapter.in.web.dto.ExportarCalendarioRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.ExportarCalendarioResponse;
import com.scheduleu.infrastructure.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada (REST) para exportar el horario del estudiante a su
 * Google Calendar. El horario se obtiene en el servidor a partir del JWT; el
 * cliente solo aporta el token de acceso de Google.
 */
@RestController
@RequestMapping("/api/calendario")
@RequiredArgsConstructor
public class CalendarioController {

    private final ExportarHorarioCalendarioUseCase service;
    private final JwtTokenProvider jwtTokenProvider;

    private String extraerCorreo(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new RuntimeException("No autorizado");
        return jwtTokenProvider.getCorreoFromToken(header.substring(7));
    }

    @PostMapping("/exportar")
    public ResponseEntity<ExportarCalendarioResponse> exportar(@RequestBody ExportarCalendarioRequest body,
                                                               HttpServletRequest req) {
        return ResponseEntity.ok(service.exportar(extraerCorreo(req), body.getAccessToken()));
    }
}
