package com.scheduleu.application.service;

import com.scheduleu.domain.model.Inscripcion;
import com.scheduleu.domain.port.in.InscripcionUseCase;
import com.scheduleu.domain.port.out.InscripcionPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InscripcionService implements InscripcionUseCase {

    private final InscripcionPort inscripcionRepository;

    public InscripcionService(InscripcionPort inscripcionRepository) {
        this.inscripcionRepository = inscripcionRepository;
    }

    @Override
    public List<Inscripcion> listarTodas() {
        return inscripcionRepository.findAll();
    }

    @Override
    public Optional<Inscripcion> buscarPorId(Integer id) {
        return inscripcionRepository.findById(id);
    }

    @Override
    public List<Inscripcion> listarPorEstudiante(Integer idEstudiante) {
        return inscripcionRepository.findByEstudianteIdEstudiante(idEstudiante);
    }

    @Override
    public List<Inscripcion> listarPorAsignatura(Integer idAsignatura) {
        return inscripcionRepository.findByAsignaturaIdAsignatura(idAsignatura);
    }

    @Override
    public Inscripcion guardar(Inscripcion inscripcion) {
        return inscripcionRepository.save(inscripcion);
    }

    @Override
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

    @Override
    public void eliminar(Integer id) {
        if (!inscripcionRepository.existsById(id)) {
            throw new RuntimeException("Inscripcion no encontrada con id: " + id);
        }
        inscripcionRepository.deleteById(id);
    }
}
