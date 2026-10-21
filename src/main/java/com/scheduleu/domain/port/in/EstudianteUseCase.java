package com.scheduleu.domain.port.in;

import com.scheduleu.domain.model.Estudiante;

import java.util.List;
import java.util.Optional;

public interface EstudianteUseCase {
    List<Estudiante> listarTodos();
    Optional<Estudiante> buscarPorId(Integer id);
    Optional<Estudiante> buscarPorCodigo(String codigo);
    Estudiante guardar(Estudiante estudiante);
    Estudiante actualizar(Integer id, Estudiante estudiante);
    void eliminar(Integer id);
}
