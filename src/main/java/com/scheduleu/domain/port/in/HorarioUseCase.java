package com.scheduleu.domain.port.in;

import com.scheduleu.domain.model.Horario;

import java.util.List;
import java.util.Optional;

public interface HorarioUseCase {
    List<Horario> listarTodos();
    Optional<Horario> buscarPorId(Integer id);
    List<Horario> listarPorGrupo(Integer idGrupo);
    Horario guardar(Horario horario);
    Horario actualizar(Integer id, Horario horario);
    void eliminar(Integer id);
}
