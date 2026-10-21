package com.scheduleu.infrastructure.adapter.in.web;

import com.scheduleu.domain.model.Asignatura;
import com.scheduleu.domain.port.in.AsignaturaUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaturas")
public class AsignaturaController {

    private final AsignaturaUseCase asignaturaService;

    public AsignaturaController(AsignaturaUseCase asignaturaService) {
        this.asignaturaService = asignaturaService;
    }

    @GetMapping
    public ResponseEntity<List<Asignatura>> listarTodas() {
        return ResponseEntity.ok(asignaturaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asignatura> buscarPorId(@PathVariable Integer id) {
        return asignaturaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Asignatura> guardar(@RequestBody Asignatura asignatura) {
        Asignatura guardada = asignaturaService.guardar(asignatura);
        return new ResponseEntity<>(guardada, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asignatura> actualizar(@PathVariable Integer id, @RequestBody Asignatura asignatura) {
        try {
            Asignatura actualizada = asignaturaService.actualizar(id, asignatura);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            asignaturaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
