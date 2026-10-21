package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.Estudiante;

import java.util.List;
import java.util.Optional;

public interface EstudiantePort {
    List<Estudiante> findAll();
    Optional<Estudiante> findById(Integer id);
    Optional<Estudiante> findByCodigo(String codigo);
    <S extends Estudiante> S save(S entity);
    boolean existsById(Integer id);
    void deleteById(Integer id);
}
