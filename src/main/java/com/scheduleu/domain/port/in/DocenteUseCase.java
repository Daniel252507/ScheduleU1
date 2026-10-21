package com.scheduleu.domain.port.in;

import com.scheduleu.domain.model.Docente;

import java.util.List;
import java.util.Optional;

public interface DocenteUseCase {
    List<Docente> listarTodos();
    Optional<Docente> buscarPorId(Integer id);
    Docente guardar(Docente docente);
    Docente actualizar(Integer id, Docente docente);
    void eliminar(Integer id);
}
