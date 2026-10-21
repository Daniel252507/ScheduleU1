package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.Asignacion;

import java.util.List;
import java.util.Optional;

public interface AsignacionPort {
    List<Asignacion> findAll();
    Optional<Asignacion> findById(Integer id);
    List<Asignacion> findByEstudianteIdEstudiante(Integer idEstudiante);
    List<Asignacion> findByGrupoIdGrupo(Integer idGrupo);
    List<Asignacion> findByEstado(Asignacion.EstadoAsignacion estado);
    long countByGrupoIdGrupoAndEstado(Integer idGrupo, Asignacion.EstadoAsignacion estado);
    <S extends Asignacion> S save(S entity);
    boolean existsById(Integer id);
    void deleteById(Integer id);
    void deleteAllById(Iterable<? extends Integer> ids);
}
