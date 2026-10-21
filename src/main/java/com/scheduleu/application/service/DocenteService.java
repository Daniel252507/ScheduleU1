package com.scheduleu.application.service;

import com.scheduleu.domain.model.Docente;
import com.scheduleu.domain.port.in.DocenteUseCase;
import com.scheduleu.domain.port.out.DocentePort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocenteService implements DocenteUseCase {

    private final DocentePort docenteRepository;

    public DocenteService(DocentePort docenteRepository) {
        this.docenteRepository = docenteRepository;
    }

    @Override
    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    @Override
    public Optional<Docente> buscarPorId(Integer id) {
        return docenteRepository.findById(id);
    }

    @Override
    public Docente guardar(Docente docente) {
        return docenteRepository.save(docente);
    }

    @Override
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

    @Override
    public void eliminar(Integer id) {
        if (!docenteRepository.existsById(id)) {
            throw new RuntimeException("Docente no encontrado con id: " + id);
        }
        docenteRepository.deleteById(id);
    }
}
