package com.scheduleu.service;

import com.scheduleu.model.Estudiante;
import com.scheduleu.repository.EstudianteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    public List<Estudiante> listarTodos() {
        return estudianteRepository.findAll();
    }

    public Optional<Estudiante> buscarPorId(Integer id) {
        return estudianteRepository.findById(id);
    }

    public Optional<Estudiante> buscarPorCodigo(String codigo) {
        return estudianteRepository.findByCodigo(codigo);
    }

    public Estudiante guardar(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }

    public Estudiante actualizar(Integer id, Estudiante estudianteActualizado) {
        return estudianteRepository.findById(id)
                .map(estudiante -> {
                    estudiante.setNombre(estudianteActualizado.getNombre());
                    estudiante.setCodigo(estudianteActualizado.getCodigo());
                    estudiante.setSemestre(estudianteActualizado.getSemestre());
                    estudiante.setCorreo(estudianteActualizado.getCorreo());
                    return estudianteRepository.save(estudiante);
                })
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con id: " + id));
    }

    public void eliminar(Integer id) {
        if (!estudianteRepository.existsById(id)) {
            throw new RuntimeException("Estudiante no encontrado con id: " + id);
        }
        estudianteRepository.deleteById(id);
    }
}
