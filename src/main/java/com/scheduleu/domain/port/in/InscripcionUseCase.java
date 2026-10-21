package com.scheduleu.domain.port.in;

import com.scheduleu.domain.model.Inscripcion;

import java.util.List;
import java.util.Optional;

public interface InscripcionUseCase {
    List<Inscripcion> listarTodas();
    Optional<Inscripcion> buscarPorId(Integer id);
    List<Inscripcion> listarPorEstudiante(Integer idEstudiante);
    List<Inscripcion> listarPorAsignatura(Integer idAsignatura);
    Inscripcion guardar(Inscripcion inscripcion);
    Inscripcion actualizar(Integer id, Inscripcion inscripcion);
    void eliminar(Integer id);
}
