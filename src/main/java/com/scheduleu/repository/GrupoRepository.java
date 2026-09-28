package com.scheduleu.repository;

import com.scheduleu.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Integer> {
    List<Grupo> findByAsignaturaIdAsignatura(Integer idAsignatura);
    List<Grupo> findByDocenteIdDocente(Integer idDocente);

    @Query("SELECT g FROM Grupo g WHERE g.asignatura.idAsignatura = ?1 AND (SELECT COUNT(a) FROM Asignacion a WHERE a.grupo.idGrupo = g.idGrupo AND a.estado = 'ASIGNADO') < g.cupo")
    List<Grupo> findGruposConCupoDisponiblePorAsignatura(Integer idAsignatura);
}
