package com.scheduleu.infrastructure.adapter.out.persistence;

import com.scheduleu.domain.model.Asignacion;
import com.scheduleu.domain.port.out.AsignacionPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Integer>, AsignacionPort {
    List<Asignacion> findByEstudianteIdEstudiante(Integer idEstudiante);
    List<Asignacion> findByGrupoIdGrupo(Integer idGrupo);
    List<Asignacion> findByEstado(Asignacion.EstadoAsignacion estado);
    long countByGrupoIdGrupoAndEstado(Integer idGrupo, Asignacion.EstadoAsignacion estado);

    @Modifying
    @Transactional
    @Query("DELETE FROM Asignacion a WHERE a.estudiante.idEstudiante = :idEst")
    void deleteByEstudianteIdEstudiante(@Param("idEst") Integer idEstudiante);

    @Override
    default void eliminarPorEstudiante(Integer idEstudiante) {
        deleteByEstudianteIdEstudiante(idEstudiante);
    }
}
