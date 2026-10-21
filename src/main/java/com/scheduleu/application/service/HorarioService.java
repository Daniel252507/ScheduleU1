package com.scheduleu.application.service;

import com.scheduleu.domain.model.Horario;
import com.scheduleu.domain.port.in.HorarioUseCase;
import com.scheduleu.domain.port.out.HorarioPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HorarioService implements HorarioUseCase {

    private final HorarioPort horarioRepository;

    public HorarioService(HorarioPort horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    @Override
    public List<Horario> listarTodos() {
        return horarioRepository.findAll();
    }

    @Override
    public Optional<Horario> buscarPorId(Integer id) {
        return horarioRepository.findById(id);
    }

    @Override
    public List<Horario> listarPorGrupo(Integer idGrupo) {
        return horarioRepository.findByGrupoIdGrupo(idGrupo);
    }

    @Override
    public Horario guardar(Horario horario) {
        return horarioRepository.save(horario);
    }

    @Override
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

    @Override
    public void eliminar(Integer id) {
        if (!horarioRepository.existsById(id)) {
            throw new RuntimeException("Horario no encontrado con id: " + id);
        }
        horarioRepository.deleteById(id);
    }
}
