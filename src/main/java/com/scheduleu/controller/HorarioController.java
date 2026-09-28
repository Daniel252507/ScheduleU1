package com.scheduleu.controller;

import com.scheduleu.model.Horario;
import com.scheduleu.service.HorarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping
    public ResponseEntity<List<Horario>> listarTodos() {
        return ResponseEntity.ok(horarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Horario> buscarPorId(@PathVariable Integer id) {
        return horarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<List<Horario>> listarPorGrupo(@PathVariable Integer idGrupo) {
        return ResponseEntity.ok(horarioService.listarPorGrupo(idGrupo));
    }

    @PostMapping
    public ResponseEntity<Horario> guardar(@RequestBody Horario horario) {
        Horario guardado = horarioService.guardar(horario);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Horario> actualizar(@PathVariable Integer id, @RequestBody Horario horario) {
        try {
            Horario actualizado = horarioService.actualizar(id, horario);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            horarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
