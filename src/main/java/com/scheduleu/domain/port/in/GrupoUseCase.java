package com.scheduleu.domain.port.in;

import com.scheduleu.domain.model.Grupo;

import java.util.List;
import java.util.Optional;

public interface GrupoUseCase {
    List<Grupo> listarTodos();
    Optional<Grupo> buscarPorId(Integer id);
    List<Grupo> listarPorAsignatura(Integer idAsignatura);
    List<Grupo> listarPorDocente(Integer idDocente);
    List<Grupo> listarGruposConCupoDisponiblePorAsignatura(Integer idAsignatura);
    Grupo guardar(Grupo grupo);
    Grupo actualizar(Integer id, Grupo grupo);
    void eliminar(Integer id);
}
