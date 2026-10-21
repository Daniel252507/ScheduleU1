package com.scheduleu.infrastructure.adapter.in.web;

import com.scheduleu.domain.model.Docente;
import com.scheduleu.domain.port.in.DocenteUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final DocenteUseCase docenteService;

    public DocenteController(DocenteUseCase docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping
    public ResponseEntity<List<Docente>> listarTodos() {
        return ResponseEntity.ok(docenteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Docente> buscarPorId(@PathVariable Integer id) {
        return docenteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Docente> guardar(@RequestBody Docente docente) {
        Docente guardado = docenteService.guardar(docente);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Docente> actualizar(@PathVariable Integer id, @RequestBody Docente docente) {
        try {
            Docente actualizado = docenteService.actualizar(id, docente);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            docenteService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
