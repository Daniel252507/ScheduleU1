package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.PreferenciaEstudiante;

import java.util.Optional;

public interface PreferenciaEstudiantePort {
    Optional<PreferenciaEstudiante> findByEstudianteIdEstudiante(Integer idEstudiante);
    <S extends PreferenciaEstudiante> S save(S entity);
    void eliminarPorEstudiante(Integer idEstudiante);
}
