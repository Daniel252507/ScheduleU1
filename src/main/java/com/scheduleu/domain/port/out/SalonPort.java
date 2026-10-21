package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.Salon;

import java.util.List;
import java.util.Optional;

public interface SalonPort {
    List<Salon> findAll();
    Optional<Salon> findById(Integer id);
    <S extends Salon> S save(S entity);
    boolean existsById(Integer id);
    void deleteById(Integer id);
}
