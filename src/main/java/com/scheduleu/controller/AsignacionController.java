package com.scheduleu.controller;

import com.scheduleu.model.Asignacion;
import com.scheduleu.service.AsignacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {

    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @GetMapping
    public ResponseEntity<List<Asignacion>> listarTodas() {
        return ResponseEntity.ok(asignacionService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asignacion> buscarPorId(@PathVariable Integer id) {
        return asignacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estudiante/{idEstudiante}")
    public ResponseEntity<List<Asignacion>> listarPorEstudiante(@PathVariable Integer idEstudiante) {
        return ResponseEntity.ok(asignacionService.listarPorEstudiante(idEstudiante));
    }

    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<List<Asignacion>> listarPorGrupo(@PathVariable Integer idGrupo) {
        return ResponseEntity.ok(asignacionService.listarPorGrupo(idGrupo));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Asignacion>> listarPorEstado(@PathVariable String estado) {
        try {
            Asignacion.EstadoAsignacion estadoEnum = Asignacion.EstadoAsignacion.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(asignacionService.listarPorEstado(estadoEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Asignacion> guardar(@RequestBody Asignacion asignacion) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asignacion> actualizar(@PathVariable Integer id, @RequestBody Asignacion asignacion) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
