package com.scheduleu.repository;

import com.scheduleu.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Integer> {
    List<Horario> findByGrupoIdGrupo(Integer idGrupo);
}
