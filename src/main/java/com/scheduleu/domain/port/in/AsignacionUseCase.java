package com.scheduleu.domain.port.in;

import com.scheduleu.domain.model.Asignacion;

import java.util.List;
import java.util.Optional;

public interface AsignacionUseCase {
    List<Asignacion> listarTodas();
    Optional<Asignacion> buscarPorId(Integer id);
    List<Asignacion> listarPorEstudiante(Integer idEstudiante);
    List<Asignacion> listarPorGrupo(Integer idGrupo);
    List<Asignacion> listarPorEstado(Asignacion.EstadoAsignacion estado);
    long contarAsignadosPorGrupo(Integer idGrupo);
    Asignacion guardar(Asignacion asignacion);
    Asignacion actualizar(Integer id, Asignacion asignacion);
    void eliminar(Integer id);
}
