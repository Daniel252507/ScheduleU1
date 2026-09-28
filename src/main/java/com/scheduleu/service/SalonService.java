package com.scheduleu.service;

import com.scheduleu.model.Salon;
import com.scheduleu.repository.SalonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalonService {

    private final SalonRepository salonRepository;

    public SalonService(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    public List<Salon> listarTodos() {
        return salonRepository.findAll();
    }

    public Optional<Salon> buscarPorId(Integer id) {
        return salonRepository.findById(id);
    }

    public Salon guardar(Salon salon) {
        return salonRepository.save(salon);
    }

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

    public void eliminar(Integer id) {
        if (!salonRepository.existsById(id)) {
            throw new RuntimeException("Salon no encontrado con id: " + id);
        }
        salonRepository.deleteById(id);
    }
}
