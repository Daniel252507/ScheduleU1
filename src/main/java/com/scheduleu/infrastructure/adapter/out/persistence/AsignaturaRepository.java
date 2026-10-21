package com.scheduleu.infrastructure.adapter.out.persistence;

import com.scheduleu.domain.model.Asignatura;
import com.scheduleu.domain.port.out.AsignaturaPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignaturaRepository extends JpaRepository<Asignatura, Integer>, AsignaturaPort {
}
