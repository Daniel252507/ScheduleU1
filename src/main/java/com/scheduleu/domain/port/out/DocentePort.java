package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.Docente;

import java.util.List;
import java.util.Optional;

public interface DocentePort {
    List<Docente> findAll();
    Optional<Docente> findById(Integer id);
    <S extends Docente> S save(S entity);
    boolean existsById(Integer id);
    void deleteById(Integer id);
}
