package com.scheduleu.controller;

import com.scheduleu.model.Salon;
import com.scheduleu.service.SalonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salones")
public class SalonController {

    private final SalonService salonService;

    public SalonController(SalonService salonService) {
        this.salonService = salonService;
    }

    @GetMapping
    public ResponseEntity<List<Salon>> listarTodos() {
        return ResponseEntity.ok(salonService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Salon> buscarPorId(@PathVariable Integer id) {
        return salonService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Salon> guardar(@RequestBody Salon salon) {
        Salon guardado = salonService.guardar(salon);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Salon> actualizar(@PathVariable Integer id, @RequestBody Salon salon) {
        try {
            Salon actualizado = salonService.actualizar(id, salon);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            salonService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
