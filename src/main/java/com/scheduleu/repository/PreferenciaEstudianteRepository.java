package com.scheduleu.repository;

import com.scheduleu.model.PreferenciaEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferenciaEstudianteRepository extends JpaRepository<PreferenciaEstudiante, Long> {
    Optional<PreferenciaEstudiante> findByEstudianteIdEstudiante(Integer idEstudiante);
}
