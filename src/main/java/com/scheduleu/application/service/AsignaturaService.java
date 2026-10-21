package com.scheduleu.application.service;

import com.scheduleu.domain.model.Asignatura;
import com.scheduleu.domain.port.in.AsignaturaUseCase;
import com.scheduleu.domain.port.out.AsignaturaPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AsignaturaService implements AsignaturaUseCase {

    private final AsignaturaPort asignaturaRepository;

    public AsignaturaService(AsignaturaPort asignaturaRepository) {
        this.asignaturaRepository = asignaturaRepository;
    }

    @Override
    public List<Asignatura> listarTodas() {
        return asignaturaRepository.findAll();
    }

    @Override
    public Optional<Asignatura> buscarPorId(Integer id) {
        return asignaturaRepository.findById(id);
    }

    @Override
    public Asignatura guardar(Asignatura asignatura) {
        return asignaturaRepository.save(asignatura);
    }

    @Override
    public Asignatura actualizar(Integer id, Asignatura asignaturaActualizada) {
        return asignaturaRepository.findById(id)
                .map(asignatura -> {
                    asignatura.setNombre(asignaturaActualizada.getNombre());
                    asignatura.setCreditos(asignaturaActualizada.getCreditos());
                    asignatura.setCodigo(asignaturaActualizada.getCodigo());
                    asignatura.setSemestreRecomendado(asignaturaActualizada.getSemestreRecomendado());
                    return asignaturaRepository.save(asignatura);
                })
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con id: " + id));
    }

    @Override
    public void eliminar(Integer id) {
        if (!asignaturaRepository.existsById(id)) {
            throw new RuntimeException("Asignatura no encontrada con id: " + id);
        }
        asignaturaRepository.deleteById(id);
    }
}
