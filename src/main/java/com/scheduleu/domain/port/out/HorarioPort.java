package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.Horario;

import java.util.List;
import java.util.Optional;

public interface HorarioPort {
    List<Horario> findAll();
    Optional<Horario> findById(Integer id);
    List<Horario> findByGrupoIdGrupo(Integer idGrupo);
    <S extends Horario> S save(S entity);
    boolean existsById(Integer id);
    void deleteById(Integer id);
}
