package com.scheduleu.infrastructure.adapter.out.persistence;

import com.scheduleu.domain.model.PreferenciaEstudiante;
import com.scheduleu.domain.port.out.PreferenciaEstudiantePort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PreferenciaEstudianteRepository extends JpaRepository<PreferenciaEstudiante, Long>, PreferenciaEstudiantePort {
    Optional<PreferenciaEstudiante> findByEstudianteIdEstudiante(Integer idEstudiante);

    @Modifying
    @Transactional
    @Query("DELETE FROM PreferenciaEstudiante p WHERE p.estudiante.idEstudiante = :idEst")
    void deleteByEstudianteIdEstudiante(@Param("idEst") Integer idEstudiante);

    @Override
    default void eliminarPorEstudiante(Integer idEstudiante) {
        deleteByEstudianteIdEstudiante(idEstudiante);
    }
}
