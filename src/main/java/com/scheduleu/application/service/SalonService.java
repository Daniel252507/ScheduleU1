package com.scheduleu.application.service;

import com.scheduleu.domain.model.Salon;
import com.scheduleu.domain.port.in.SalonUseCase;
import com.scheduleu.domain.port.out.SalonPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalonService implements SalonUseCase {

    private final SalonPort salonRepository;

    public SalonService(SalonPort salonRepository) {
        this.salonRepository = salonRepository;
    }

    @Override
    public List<Salon> listarTodos() {
        return salonRepository.findAll();
    }

    @Override
    public Optional<Salon> buscarPorId(Integer id) {
        return salonRepository.findById(id);
    }

    @Override
    public Salon guardar(Salon salon) {
        return salonRepository.save(salon);
    }

    @Override
    public Salon actualizar(Integer id, Salon salonActualizado) {
        return salonRepository.findById(id)
                .map(salon -> {
                    salon.setNombre(salonActualizado.getNombre());
                    salon.setCapacidad(salonActualizado.getCapacidad());
                    salon.setUbicacion(salonActualizado.getUbicacion());
                    return salonRepository.save(salon);
                })
                .orElseThrow(() -> new RuntimeException("Salon no encontrado con id: " + id));
    }

    @Override
    public void eliminar(Integer id) {
        if (!salonRepository.existsById(id)) {
            throw new RuntimeException("Salon no encontrado con id: " + id);
        }
        salonRepository.deleteById(id);
    }
}
