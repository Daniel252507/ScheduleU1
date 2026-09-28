package com.scheduleu.controller;

import com.scheduleu.model.Inscripcion;
import com.scheduleu.service.InscripcionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    public ResponseEntity<List<Inscripcion>> listarTodas() {
        return ResponseEntity.ok(inscripcionService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inscripcion> buscarPorId(@PathVariable Integer id) {
        return inscripcionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estudiante/{idEstudiante}")
    public ResponseEntity<List<Inscripcion>> listarPorEstudiante(@PathVariable Integer idEstudiante) {
        return ResponseEntity.ok(inscripcionService.listarPorEstudiante(idEstudiante));
    }

    @GetMapping("/asignatura/{idAsignatura}")
    public ResponseEntity<List<Inscripcion>> listarPorAsignatura(@PathVariable Integer idAsignatura) {
        return ResponseEntity.ok(inscripcionService.listarPorAsignatura(idAsignatura));
    }

    @PostMapping
    public ResponseEntity<Inscripcion> guardar(@RequestBody Inscripcion inscripcion) {
        Inscripcion guardada = inscripcionService.guardar(inscripcion);
        return new ResponseEntity<>(guardada, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inscripcion> actualizar(@PathVariable Integer id, @RequestBody Inscripcion inscripcion) {
        try {
            Inscripcion actualizada = inscripcionService.actualizar(id, inscripcion);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            inscripcionService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
