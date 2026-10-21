package com.scheduleu.application.service;

import com.scheduleu.domain.model.*;
import com.scheduleu.domain.port.in.ScheduleAssignmentUseCase;
import com.scheduleu.domain.port.out.AsignacionPort;
import com.scheduleu.domain.port.out.EstudiantePort;
import com.scheduleu.domain.port.out.GrupoPort;
import com.scheduleu.domain.port.out.HorarioPort;
import com.scheduleu.domain.port.out.InscripcionPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleAssignmentService implements ScheduleAssignmentUseCase {

    private final EstudiantePort estudianteRepository;
    private final InscripcionPort inscripcionRepository;
    private final GrupoPort grupoRepository;
    private final HorarioPort horarioRepository;
    private final AsignacionPort asignacionRepository;

    public ScheduleAssignmentService(EstudiantePort estudianteRepository,
                                     InscripcionPort inscripcionRepository,
                                     GrupoPort grupoRepository,
                                     HorarioPort horarioRepository,
                                     AsignacionPort asignacionRepository) {
        this.estudianteRepository = estudianteRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.grupoRepository = grupoRepository;
        this.horarioRepository = horarioRepository;
        this.asignacionRepository = asignacionRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> ejecutarPrimeraAsignacion() {
        List<Estudiante> estudiantes = estudianteRepository.findAll();
        int asignados = 0;
        int pendientes = 0;

        for (Estudiante estudiante : estudiantes) {
            List<Inscripcion> inscripciones = inscripcionRepository.findByEstudianteIdEstudiante(estudiante.getIdEstudiante());
            List<Horario> horariosOcupados = new ArrayList<>();

            for (Inscripcion inscripcion : inscripciones) {
                Optional<Grupo> grupoOpt = buscarGrupoDisponible(inscripcion.getAsignatura().getIdAsignatura(), horariosOcupados);

                Asignacion asignacion = new Asignacion();
                asignacion.setEstudiante(estudiante);

                if (grupoOpt.isPresent()) {
                    Grupo grupo = grupoOpt.get();
                    long asignadosEnGrupo = asignacionRepository.countByGrupoIdGrupoAndEstado(
                            grupo.getIdGrupo(), Asignacion.EstadoAsignacion.ASIGNADO);

                    if (asignadosEnGrupo < grupo.getCupo()) {
                        asignacion.setGrupo(grupo);
                        asignacion.setEstado(Asignacion.EstadoAsignacion.ASIGNADO);
                        horariosOcupados.addAll(horarioRepository.findByGrupoIdGrupo(grupo.getIdGrupo()));
                        asignados++;
                    } else {
                        asignacion.setGrupo(grupo);
                        asignacion.setEstado(Asignacion.EstadoAsignacion.PENDIENTE);
                        pendientes++;
                    }
                } else {
                    asignacion.setGrupo(null);
                    asignacion.setEstado(Asignacion.EstadoAsignacion.PENDIENTE);
                    pendientes++;
                }

                asignacionRepository.save(asignacion);
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalEstudiantes", estudiantes.size());
        resultado.put("asignacionesExitosas", asignados);
        resultado.put("asignacionesPendientes", pendientes);
        return resultado;
    }

    @Override
    @Transactional
    public Map<String, Object> ejecutarSegundaAsignacion() {
        List<Asignacion> pendientes = asignacionRepository.findByEstado(Asignacion.EstadoAsignacion.PENDIENTE);
        int reasignados = 0;
        int siguenPendientes = 0;

        Map<Estudiante, List<Horario>> horariosPorEstudiante = new HashMap<>();

        List<Asignacion> asignadas = asignacionRepository.findByEstado(Asignacion.EstadoAsignacion.ASIGNADO);
        for (Asignacion a : asignadas) {
            horariosPorEstudiante.computeIfAbsent(a.getEstudiante(), k -> new ArrayList<>())
                    .addAll(horarioRepository.findByGrupoIdGrupo(a.getGrupo().getIdGrupo()));
        }

        Map<Estudiante, List<Asignacion>> pendientesPorEstudiante = pendientes.stream()
                .collect(Collectors.groupingBy(Asignacion::getEstudiante));

        for (Map.Entry<Estudiante, List<Asignacion>> entry : pendientesPorEstudiante.entrySet()) {
            Estudiante estudiante = entry.getKey();
            List<Horario> horariosOcupados = horariosPorEstudiante.getOrDefault(estudiante, new ArrayList<>());

            for (Asignacion pendiente : entry.getValue()) {
                Integer idAsignatura = pendiente.getGrupo() != null
                        ? pendiente.getGrupo().getAsignatura().getIdAsignatura()
                        : obtenerAsignaturaDesdeInscripcion(estudiante);

                if (idAsignatura != null) {
                    Optional<Grupo> grupoOpt = buscarGrupoDisponible(idAsignatura, horariosOcupados);

                    if (grupoOpt.isPresent()) {
                        Grupo grupo = grupoOpt.get();
                        long asignadosEnGrupo = asignacionRepository.countByGrupoIdGrupoAndEstado(
                                grupo.getIdGrupo(), Asignacion.EstadoAsignacion.ASIGNADO);

                        if (asignadosEnGrupo < grupo.getCupo()) {
                            pendiente.setGrupo(grupo);
                            pendiente.setEstado(Asignacion.EstadoAsignacion.ASIGNADO);
                            horariosOcupados.addAll(horarioRepository.findByGrupoIdGrupo(grupo.getIdGrupo()));
                            reasignados++;
                        } else {
                            siguenPendientes++;
                        }
                    } else {
                        siguenPendientes++;
                    }
                } else {
                    siguenPendientes++;
                }

                asignacionRepository.save(pendiente);
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("pendientesIniciales", pendientes.size());
        resultado.put("reasignados", reasignados);
        resultado.put("siguenPendientes", siguenPendientes);
        return resultado;
    }

    private Optional<Grupo> buscarGrupoDisponible(Integer idAsignatura, List<Horario> horariosOcupados) {
        List<Grupo> grupos = grupoRepository.findByAsignaturaIdAsignatura(idAsignatura);

        for (Grupo grupo : grupos) {
            List<Horario> horariosGrupo = horarioRepository.findByGrupoIdGrupo(grupo.getIdGrupo());
            boolean hayConflicto = false;

            for (Horario hg : horariosGrupo) {
                for (Horario ho : horariosOcupados) {
                    if (hayConflictoHorario(hg, ho)) {
                        hayConflicto = true;
                        break;
                    }
                }
                if (hayConflicto) break;
            }

            if (!hayConflicto) {
                return Optional.of(grupo);
            }
        }
        return grupos.stream().findFirst();
    }

    private boolean hayConflictoHorario(Horario h1, Horario h2) {
        if (!h1.getDia().equalsIgnoreCase(h2.getDia())) {
            return false;
        }
        LocalTime inicio1 = h1.getHoraInicio();
        LocalTime fin1 = h1.getHoraFin();
        LocalTime inicio2 = h2.getHoraInicio();
        LocalTime fin2 = h2.getHoraFin();

        return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
    }

    private Integer obtenerAsignaturaDesdeInscripcion(Estudiante estudiante) {
        List<Inscripcion> inscripciones = inscripcionRepository.findByEstudianteIdEstudiante(estudiante.getIdEstudiante());
        if (!inscripciones.isEmpty()) {
            return inscripciones.get(0).getAsignatura().getIdAsignatura();
        }
        return null;
    }
}
