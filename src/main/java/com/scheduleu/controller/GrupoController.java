package com.scheduleu.controller;

import com.scheduleu.model.Grupo;
import com.scheduleu.service.GrupoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @GetMapping
    public ResponseEntity<List<Grupo>> listarTodos() {
        return ResponseEntity.ok(grupoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grupo> buscarPorId(@PathVariable Integer id) {
        return grupoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/asignatura/{idAsignatura}")
    public ResponseEntity<List<Grupo>> listarPorAsignatura(@PathVariable Integer idAsignatura) {
        return ResponseEntity.ok(grupoService.listarPorAsignatura(idAsignatura));
    }

    @GetMapping("/docente/{idDocente}")
    public ResponseEntity<List<Grupo>> listarPorDocente(@PathVariable Integer idDocente) {
        return ResponseEntity.ok(grupoService.listarPorDocente(idDocente));
    }

    @GetMapping("/asignatura/{idAsignatura}/disponibles")
    public ResponseEntity<List<Grupo>> listarGruposConCupoDisponible(@PathVariable Integer idAsignatura) {
        return ResponseEntity.ok(grupoService.listarGruposConCupoDisponiblePorAsignatura(idAsignatura));
    }

    @PostMapping
    public ResponseEntity<Grupo> guardar(@RequestBody Grupo grupo) {
        Grupo guardado = grupoService.guardar(grupo);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Grupo> actualizar(@PathVariable Integer id, @RequestBody Grupo grupo) {
        try {
            Grupo actualizado = grupoService.actualizar(id, grupo);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            grupoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
