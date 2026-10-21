package com.scheduleu.application.service;

import com.scheduleu.domain.model.Asignacion;
import com.scheduleu.domain.port.in.AsignacionUseCase;
import com.scheduleu.domain.port.out.AsignacionPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AsignacionService implements AsignacionUseCase {

    private final AsignacionPort asignacionRepository;

    public AsignacionService(AsignacionPort asignacionRepository) {
        this.asignacionRepository = asignacionRepository;
    }

    @Override
    public List<Asignacion> listarTodas() {
        return asignacionRepository.findAll();
    }

    @Override
    public Optional<Asignacion> buscarPorId(Integer id) {
        return asignacionRepository.findById(id);
    }

    @Override
    public List<Asignacion> listarPorEstudiante(Integer idEstudiante) {
        return asignacionRepository.findByEstudianteIdEstudiante(idEstudiante);
    }

    @Override
    public List<Asignacion> listarPorGrupo(Integer idGrupo) {
        return asignacionRepository.findByGrupoIdGrupo(idGrupo);
    }

    @Override
    public List<Asignacion> listarPorEstado(Asignacion.EstadoAsignacion estado) {
        return asignacionRepository.findByEstado(estado);
    }

    @Override
    public long contarAsignadosPorGrupo(Integer idGrupo) {
        return asignacionRepository.countByGrupoIdGrupoAndEstado(idGrupo, Asignacion.EstadoAsignacion.ASIGNADO);
    }

    @Override
    public Asignacion guardar(Asignacion asignacion) {
        return asignacionRepository.save(asignacion);
    }

    @Override
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

    @Override
    public void eliminar(Integer id) {
        if (!asignacionRepository.existsById(id)) {
            throw new RuntimeException("Asignacion no encontrada con id: " + id);
        }
        asignacionRepository.deleteById(id);
    }
}
