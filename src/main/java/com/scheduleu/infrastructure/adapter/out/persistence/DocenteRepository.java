package com.scheduleu.infrastructure.adapter.out.persistence;

import com.scheduleu.domain.model.Docente;
import com.scheduleu.domain.port.out.DocentePort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Integer>, DocentePort {
}
