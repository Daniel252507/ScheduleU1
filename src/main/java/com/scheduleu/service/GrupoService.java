package com.scheduleu.service;

import com.scheduleu.model.Grupo;
import com.scheduleu.repository.GrupoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;

    public GrupoService(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public List<Grupo> listarTodos() {
        return grupoRepository.findAll();
    }

    public Optional<Grupo> buscarPorId(Integer id) {
        return grupoRepository.findById(id);
    }

    public List<Grupo> listarPorAsignatura(Integer idAsignatura) {
        return grupoRepository.findByAsignaturaIdAsignatura(idAsignatura);
    }

    public List<Grupo> listarPorDocente(Integer idDocente) {
        return grupoRepository.findByDocenteIdDocente(idDocente);
    }

    public List<Grupo> listarGruposConCupoDisponiblePorAsignatura(Integer idAsignatura) {
        return grupoRepository.findGruposConCupoDisponiblePorAsignatura(idAsignatura);
    }

    public Grupo guardar(Grupo grupo) {
        return grupoRepository.save(grupo);
    }

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

    public void eliminar(Integer id) {
        if (!grupoRepository.existsById(id)) {
            throw new RuntimeException("Grupo no encontrado con id: " + id);
        }
        grupoRepository.deleteById(id);
    }
}
