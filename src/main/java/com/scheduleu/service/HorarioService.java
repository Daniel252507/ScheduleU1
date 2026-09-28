package com.scheduleu.service;

import com.scheduleu.model.Horario;
import com.scheduleu.repository.HorarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HorarioService {

    private final HorarioRepository horarioRepository;

    public HorarioService(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    public List<Horario> listarTodos() {
        return horarioRepository.findAll();
    }

    public Optional<Horario> buscarPorId(Integer id) {
        return horarioRepository.findById(id);
    }

    public List<Horario> listarPorGrupo(Integer idGrupo) {
        return horarioRepository.findByGrupoIdGrupo(idGrupo);
    }

    public Horario guardar(Horario horario) {
        return horarioRepository.save(horario);
    }

    public Horario actualizar(Integer id, Horario horarioActualizado) {
        return horarioRepository.findById(id)
                .map(horario -> {
                    horario.setGrupo(horarioActualizado.getGrupo());
                    horario.setDia(horarioActualizado.getDia());
                    horario.setHoraInicio(horarioActualizado.getHoraInicio());
                    horario.setHoraFin(horarioActualizado.getHoraFin());
                    return horarioRepository.save(horario);
                })
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con id: " + id));
    }

    public void eliminar(Integer id) {
        if (!horarioRepository.existsById(id)) {
            throw new RuntimeException("Horario no encontrado con id: " + id);
        }
        horarioRepository.deleteById(id);
    }
}
