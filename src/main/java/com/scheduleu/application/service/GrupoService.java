package com.scheduleu.application.service;

import com.scheduleu.domain.model.Grupo;
import com.scheduleu.domain.port.in.GrupoUseCase;
import com.scheduleu.domain.port.out.GrupoPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GrupoService implements GrupoUseCase {

    private final GrupoPort grupoRepository;

    public GrupoService(GrupoPort grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    @Override
    public List<Grupo> listarTodos() {
        return grupoRepository.findAll();
    }

    @Override
    public Optional<Grupo> buscarPorId(Integer id) {
        return grupoRepository.findById(id);
    }

    @Override
    public List<Grupo> listarPorAsignatura(Integer idAsignatura) {
        return grupoRepository.findByAsignaturaIdAsignatura(idAsignatura);
    }

    @Override
    public List<Grupo> listarPorDocente(Integer idDocente) {
        return grupoRepository.findByDocenteIdDocente(idDocente);
    }

    @Override
    public List<Grupo> listarGruposConCupoDisponiblePorAsignatura(Integer idAsignatura) {
        return grupoRepository.findGruposConCupoDisponiblePorAsignatura(idAsignatura);
    }

    @Override
    public Grupo guardar(Grupo grupo) {
        return grupoRepository.save(grupo);
    }

    @Override
    public Grupo actualizar(Integer id, Grupo grupoActualizado) {
        return grupoRepository.findById(id)
                .map(grupo -> {
                    grupo.setAsignatura(grupoActualizado.getAsignatura());
                    grupo.setDocente(grupoActualizado.getDocente());
                    grupo.setSalon(grupoActualizado.getSalon());
                    grupo.setCupo(grupoActualizado.getCupo());
                    grupo.setNombre(grupoActualizado.getNombre());
                    return grupoRepository.save(grupo);
                })
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado con id: " + id));
    }

    @Override
    public void eliminar(Integer id) {
        if (!grupoRepository.existsById(id)) {
            throw new RuntimeException("Grupo no encontrado con id: " + id);
        }
        grupoRepository.deleteById(id);
    }
}
