package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.Inscripcion;

import java.util.List;
import java.util.Optional;

public interface InscripcionPort {
    List<Inscripcion> findAll();
    Optional<Inscripcion> findById(Integer id);
    List<Inscripcion> findByEstudianteIdEstudiante(Integer idEstudiante);
    List<Inscripcion> findByAsignaturaIdAsignatura(Integer idAsignatura);
    <S extends Inscripcion> S save(S entity);
    boolean existsById(Integer id);
    void deleteById(Integer id);
    void eliminarPorEstudiante(Integer idEstudiante);
}
