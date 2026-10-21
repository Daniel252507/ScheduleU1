package com.scheduleu.infrastructure.adapter.in.web;

import com.scheduleu.domain.port.in.EstudianteHorarioUseCase;
import com.scheduleu.infrastructure.adapter.in.web.dto.HorarioGeneradoResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.PreferenciasRequest;
import com.scheduleu.infrastructure.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mis-horarios")
@RequiredArgsConstructor
public class MisHorariosController {

    private final EstudianteHorarioUseCase service;
    private final JwtTokenProvider jwtTokenProvider;

    private String extraerCorreo(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new RuntimeException("No autorizado");
        return jwtTokenProvider.getCorreoFromToken(header.substring(7));
    }

    @GetMapping("/perfil")
    public ResponseEntity<Map<String, Object>> perfil(HttpServletRequest req) {
        return ResponseEntity.ok(service.obtenerPerfil(extraerCorreo(req)));
    }

    @GetMapping("/asignaturas-disponibles")
    public ResponseEntity<List<Map<String, Object>>> asignaturasDisponibles(HttpServletRequest req) {
        return ResponseEntity.ok(service.listarAsignaturasDisponibles(extraerCorreo(req)));
    }

    @GetMapping("/preferencias")
    public ResponseEntity<Map<String, Object>> verPreferencias(HttpServletRequest req) {
        return ResponseEntity.ok(service.obtenerPreferencias(extraerCorreo(req)));
    }

    @PostMapping("/guardar-preferencias")
    public ResponseEntity<?> guardarPreferencias(@RequestBody PreferenciasRequest body, HttpServletRequest req) {
        return ResponseEntity.ok(service.guardarPreferencias(extraerCorreo(req), body));
    }

    @PostMapping("/generar")
    public ResponseEntity<HorarioGeneradoResponse> generarMiHorario(HttpServletRequest req) {
        return ResponseEntity.ok(service.generarHorario(extraerCorreo(req)));
    }

    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarOpcion(@RequestBody HorarioGeneradoResponse.OpcionHorario opcion,
                                             HttpServletRequest req) {
        return ResponseEntity.ok(service.confirmarHorario(extraerCorreo(req), opcion));
    }

    @GetMapping("/asignaciones")
    public ResponseEntity<List<HorarioGeneradoResponse.FilaAsignacion>> misAsignaciones(HttpServletRequest req) {
        return ResponseEntity.ok(service.listarMisAsignaciones(extraerCorreo(req)));
    }
}
