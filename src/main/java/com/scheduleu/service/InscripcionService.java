package com.scheduleu.service;

import com.scheduleu.model.Inscripcion;
import com.scheduleu.repository.InscripcionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;

    public InscripcionService(InscripcionRepository inscripcionRepository) {
        this.inscripcionRepository = inscripcionRepository;
    }

    public List<Inscripcion> listarTodas() {
        return inscripcionRepository.findAll();
    }

    public Optional<Inscripcion> buscarPorId(Integer id) {
        return inscripcionRepository.findById(id);
    }

    public List<Inscripcion> listarPorEstudiante(Integer idEstudiante) {
        return inscripcionRepository.findByEstudianteIdEstudiante(idEstudiante);
    }

    public List<Inscripcion> listarPorAsignatura(Integer idAsignatura) {
        return inscripcionRepository.findByAsignaturaIdAsignatura(idAsignatura);
    }

    public Inscripcion guardar(Inscripcion inscripcion) {
        return inscripcionRepository.save(inscripcion);
    }

    public Inscripcion actualizar(Integer id, Inscripcion inscripcionActualizada) {
        return inscripcionRepository.findById(id)
                .map(inscripcion -> {
                    inscripcion.setEstudiante(inscripcionActualizada.getEstudiante());
                    inscripcion.setAsignatura(inscripcionActualizada.getAsignatura());
                    inscripcion.setEstado(inscripcionActualizada.getEstado());
                    return inscripcionRepository.save(inscripcion);
                })
                .orElseThrow(() -> new RuntimeException("Inscripcion no encontrada con id: " + id));
    }

    public void eliminar(Integer id) {
        if (!inscripcionRepository.existsById(id)) {
            throw new RuntimeException("Inscripcion no encontrada con id: " + id);
        }
        inscripcionRepository.deleteById(id);
    }
}
