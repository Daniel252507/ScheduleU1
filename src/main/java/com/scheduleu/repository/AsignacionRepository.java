package com.scheduleu.repository;

import com.scheduleu.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Integer> {
    List<Asignacion> findByEstudianteIdEstudiante(Integer idEstudiante);
    List<Asignacion> findByGrupoIdGrupo(Integer idGrupo);
    List<Asignacion> findByEstado(Asignacion.EstadoAsignacion estado);
    long countByGrupoIdGrupoAndEstado(Integer idGrupo, Asignacion.EstadoAsignacion estado);
}