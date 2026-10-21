package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.Grupo;

import java.util.List;
import java.util.Optional;

public interface GrupoPort {
    List<Grupo> findAll();
    Optional<Grupo> findById(Integer id);
    List<Grupo> findByAsignaturaIdAsignatura(Integer idAsignatura);
    List<Grupo> findByDocenteIdDocente(Integer idDocente);
    List<Grupo> findGruposConCupoDisponiblePorAsignatura(Integer idAsignatura);
    <S extends Grupo> S save(S entity);
    boolean existsById(Integer id);
    void deleteById(Integer id);
}
