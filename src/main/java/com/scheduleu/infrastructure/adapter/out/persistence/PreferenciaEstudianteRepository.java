package com.scheduleu.infrastructure.adapter.out.persistence;

import com.scheduleu.domain.model.PreferenciaEstudiante;
import com.scheduleu.domain.port.out.PreferenciaEstudiantePort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferenciaEstudianteRepository extends JpaRepository<PreferenciaEstudiante, Long>, PreferenciaEstudiantePort {
    Optional<PreferenciaEstudiante> findByEstudianteIdEstudiante(Integer idEstudiante);
}
