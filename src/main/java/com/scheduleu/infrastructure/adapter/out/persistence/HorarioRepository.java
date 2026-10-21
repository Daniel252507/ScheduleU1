package com.scheduleu.infrastructure.adapter.out.persistence;

import com.scheduleu.domain.model.Horario;
import com.scheduleu.domain.port.out.HorarioPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Integer>, HorarioPort {
    List<Horario> findByGrupoIdGrupo(Integer idGrupo);
}
