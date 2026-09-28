package com.scheduleu.service;

import com.scheduleu.model.Asignatura;
import com.scheduleu.repository.AsignaturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;

    public AsignaturaService(AsignaturaRepository asignaturaRepository) {
        this.asignaturaRepository = asignaturaRepository;
    }

    public List<Asignatura> listarTodas() {
        return asignaturaRepository.findAll();
    }

    public Optional<Asignatura> buscarPorId(Integer id) {
        return asignaturaRepository.findById(id);
    }

    public Asignatura guardar(Asignatura asignatura) {
        return asignaturaRepository.save(asignatura);
    }

    public Asignatura actualizar(Integer id, Asignatura asignaturaActualizada) {
        return asignaturaRepository.findById(id)
                .map(asignatura -> {
                    asignatura.setNombre(asignaturaActualizada.getNombre());
                    asignatura.setCreditos(asignaturaActualizada.getCreditos());
                    asignatura.setCodigo(asignaturaActualizada.getCodigo());
                    asignatura.setSemestreRecomendado(asignaturaActualizada.getSemestreRecomendado());
                    return asignaturaRepository.save(asignatura);
                })
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con id: " + id));
    }

    public void eliminar(Integer id) {
        if (!asignaturaRepository.existsById(id)) {
            throw new RuntimeException("Asignatura no encontrada con id: " + id);
        }
        asignaturaRepository.deleteById(id);
    }
}
