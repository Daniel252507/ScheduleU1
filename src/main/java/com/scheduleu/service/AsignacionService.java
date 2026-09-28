package com.scheduleu.service;

import com.scheduleu.model.Asignacion;
import com.scheduleu.repository.AsignacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AsignacionService {

    private final AsignacionRepository asignacionRepository;

    public AsignacionService(AsignacionRepository asignacionRepository) {
        this.asignacionRepository = asignacionRepository;
    }

    public List<Asignacion> listarTodas() {
        return asignacionRepository.findAll();
    }

    public Optional<Asignacion> buscarPorId(Integer id) {
        return asignacionRepository.findById(id);
    }

    public List<Asignacion> listarPorEstudiante(Integer idEstudiante) {
        return asignacionRepository.findByEstudianteIdEstudiante(idEstudiante);
    }

    public List<Asignacion> listarPorGrupo(Integer idGrupo) {
        return asignacionRepository.findByGrupoIdGrupo(idGrupo);
    }

    public List<Asignacion> listarPorEstado(Asignacion.EstadoAsignacion estado) {
        return asignacionRepository.findByEstado(estado);
    }

    public long contarAsignadosPorGrupo(Integer idGrupo) {
        return asignacionRepository.countByGrupoIdGrupoAndEstado(idGrupo, Asignacion.EstadoAsignacion.ASIGNADO);
    }

    public Asignacion guardar(Asignacion asignacion) {
        return asignacionRepository.save(asignacion);
    }

    public Asignacion actualizar(Integer id, Asignacion asignacionActualizada) {
        return asignacionRepository.findById(id)
                .map(asignacion -> {
                    asignacion.setEstudiante(asignacionActualizada.getEstudiante());
                    asignacion.setGrupo(asignacionActualizada.getGrupo());
                    asignacion.setEstado(asignacionActualizada.getEstado());
                    return asignacionRepository.save(asignacion);
                })
                .orElseThrow(() -> new RuntimeException("Asignacion no encontrada con id: " + id));
    }

    public void eliminar(Integer id) {
        if (!asignacionRepository.existsById(id)) {
            throw new RuntimeException("Asignacion no encontrada con id: " + id);
        }
        asignacionRepository.deleteById(id);
    }
}
