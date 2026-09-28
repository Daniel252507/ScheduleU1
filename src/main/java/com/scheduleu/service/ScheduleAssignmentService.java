package com.scheduleu.service;

import com.scheduleu.model.*;
import com.scheduleu.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleAssignmentService {

    private final EstudianteRepository estudianteRepository;
    private final InscripcionRepository inscripcionRepository;
    private final GrupoRepository grupoRepository;
    private final HorarioRepository horarioRepository;
    private final AsignacionRepository asignacionRepository;

    public ScheduleAssignmentService(EstudianteRepository estudianteRepository,
                                     InscripcionRepository inscripcionRepository,
                                     GrupoRepository grupoRepository,
                                     HorarioRepository horarioRepository,
                                     AsignacionRepository asignacionRepository) {
        this.estudianteRepository = estudianteRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.grupoRepository = grupoRepository;
        this.horarioRepository = horarioRepository;
        this.asignacionRepository = asignacionRepository;
    }

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
