package com.scheduleu.domain.port.in;

import com.scheduleu.domain.model.Asignatura;

import java.util.List;
import java.util.Optional;

public interface AsignaturaUseCase {
    List<Asignatura> listarTodas();
    Optional<Asignatura> buscarPorId(Integer id);
    Asignatura guardar(Asignatura asignatura);
    Asignatura actualizar(Integer id, Asignatura asignatura);
    void eliminar(Integer id);
}
