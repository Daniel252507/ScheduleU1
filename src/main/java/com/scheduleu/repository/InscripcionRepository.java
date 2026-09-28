package com.scheduleu.repository;

import com.scheduleu.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    List<Inscripcion> findByEstudianteIdEstudiante(Integer idEstudiante);
    List<Inscripcion> findByAsignaturaIdAsignatura(Integer idAsignatura);

    @Modifying
    @Transactional
    @Query("DELETE FROM Inscripcion i WHERE i.estudiante.idEstudiante = :idEst")
    void deleteByEstudianteIdEstudiante(@Param("idEst") Integer idEstudiante);
}
