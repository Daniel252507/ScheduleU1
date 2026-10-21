package com.scheduleu.domain.port.in;

import com.scheduleu.domain.model.Salon;

import java.util.List;
import java.util.Optional;

public interface SalonUseCase {
    List<Salon> listarTodos();
    Optional<Salon> buscarPorId(Integer id);
    Salon guardar(Salon salon);
    Salon actualizar(Integer id, Salon salon);
    void eliminar(Integer id);
}
