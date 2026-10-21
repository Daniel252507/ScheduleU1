package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.Asignatura;

import java.util.List;
import java.util.Optional;

public interface AsignaturaPort {
    List<Asignatura> findAll();
    Optional<Asignatura> findById(Integer id);
    <S extends Asignatura> S save(S entity);
    boolean existsById(Integer id);
    void deleteById(Integer id);
}
