package com.scheduleu.service;

import com.scheduleu.model.Docente;
import com.scheduleu.repository.DocenteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocenteService {

    private final DocenteRepository docenteRepository;

    public DocenteService(DocenteRepository docenteRepository) {
        this.docenteRepository = docenteRepository;
    }

    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    public Optional<Docente> buscarPorId(Integer id) {
        return docenteRepository.findById(id);
    }

    public Docente guardar(Docente docente) {
        return docenteRepository.save(docente);
    }

    public Docente actualizar(Integer id, Docente docenteActualizado) {
        return docenteRepository.findById(id)
                .map(docente -> {
                    docente.setNombre(docenteActualizado.getNombre());
                    docente.setCorreo(docenteActualizado.getCorreo());
                    docente.setIdentificacion(docenteActualizado.getIdentificacion());
                    return docenteRepository.save(docente);
                })
                .orElseThrow(() -> new RuntimeException("Docente no encontrado con id: " + id));
    }

    public void eliminar(Integer id) {
        if (!docenteRepository.existsById(id)) {
            throw new RuntimeException("Docente no encontrado con id: " + id);
        }
        docenteRepository.deleteById(id);
    }
}
