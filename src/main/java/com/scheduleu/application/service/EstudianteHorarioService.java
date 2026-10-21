package com.scheduleu.application.service;

import com.scheduleu.domain.model.*;
import com.scheduleu.domain.port.in.EstudianteHorarioUseCase;
import com.scheduleu.domain.port.out.AsignacionPort;
import com.scheduleu.domain.port.out.AsignaturaPort;
import com.scheduleu.domain.port.out.EstudiantePort;
import com.scheduleu.domain.port.out.GrupoPort;
import com.scheduleu.domain.port.out.GrupoPreferidoPort;
import com.scheduleu.domain.port.out.HorarioPort;
import com.scheduleu.domain.port.out.InscripcionPort;
import com.scheduleu.domain.port.out.PreferenciaEstudiantePort;
import com.scheduleu.infrastructure.adapter.in.web.dto.HorarioGeneradoResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.PreferenciasRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstudianteHorarioService implements EstudianteHorarioUseCase {

    private final EstudiantePort estudianteRepo;
    private final AsignaturaPort asignaturaRepo;
    private final GrupoPort grupoRepo;
    private final HorarioPort horarioRepo;
    private final InscripcionPort inscripcionRepo;
    private final AsignacionPort asignacionRepo;
    private final PreferenciaEstudiantePort preferenciaRepo;
    private final GrupoPreferidoPort grupoPrefRepo;

    private static final int MAX_OPCIONES = 8;

    // ====================== casos de uso ======================

    private Integer idEstudiantePorCorreo(String correo) {
        Estudiante e = estudianteRepo.findAll().stream()
                .filter(x -> x.getUsuario() != null && x.getUsuario().getCorreo().equalsIgnoreCase(correo))
                .findFirst().orElse(null);
        if (e == null) throw new RuntimeException("No existe registro de estudiante para tu usuario. Contacta al administrador.");
        return e.getIdEstudiante();
    }

    @Override
    public Map<String, Object> obtenerPerfil(String correo) {
        Integer id = idEstudiantePorCorreo(correo);
        Estudiante e = estudianteRepo.findById(id).orElseThrow();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("idEstudiante", e.getIdEstudiante());
        out.put("nombre", e.getNombre());
        out.put("codigo", e.getCodigo());
        out.put("semestre", e.getSemestre());
        out.put("correo", e.getCorreo());
        return out;
    }

    @Override
    public List<Map<String, Object>> listarAsignaturasDisponibles(String correo) {
        Integer id = idEstudiantePorCorreo(correo);
        Estudiante e = estudianteRepo.findById(id).orElseThrow();
        List<Asignatura> todas = asignaturaRepo.findAll();
        List<Asignatura> delSemestre = e.getSemestre() == null || e.getSemestre() == 0
                ? todas
                : todas.stream().filter(a -> Objects.equals(a.getSemestreRecomendado(), e.getSemestre())).toList();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Asignatura a : (delSemestre.isEmpty() ? todas : delSemestre)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("idAsignatura", a.getIdAsignatura());
            m.put("nombre", a.getNombre());
            m.put("creditos", a.getCreditos());
            m.put("codigo", a.getCodigo());
            m.put("semestreRecomendado", a.getSemestreRecomendado());
            List<Grupo> grupos = grupoRepo.findByAsignaturaIdAsignatura(a.getIdAsignatura());
            List<Map<String, Object>> gruposList = new ArrayList<>();
            for (Grupo g : grupos) {
                Map<String, Object> mg = new LinkedHashMap<>();
                mg.put("idGrupo", g.getIdGrupo());
                mg.put("nombre", g.getNombre());
                mg.put("cupo", g.getCupo());
                mg.put("docente", g.getDocente() == null ? null : g.getDocente().getNombre());
                mg.put("salon", g.getSalon() == null ? null : g.getSalon().getNombre());
                mg.put("jornadas", horarioRepo.findByGrupoIdGrupo(g.getIdGrupo()).stream()
                        .map(h -> h.getDia() + " " + h.getHoraInicio() + "-" + h.getHoraFin())
                        .toList());
                gruposList.add(mg);
            }
            m.put("grupos", gruposList);
            out.add(m);
        }
        return out;
    }

    @Override
    public Map<String, Object> obtenerPreferencias(String correo) {
        Integer id = idEstudiantePorCorreo(correo);
        Map<String, Object> dto = construirPreferenciasDTO(id);
        dto.put("idEstudiante", Long.valueOf(id));
        return dto;
    }

    @Override
    @Transactional
    public Map<String, Object> guardarPreferencias(String correo, PreferenciasRequest req) {
        Integer id = idEstudiantePorCorreo(correo);
        req.setIdEstudiante(Long.valueOf(id));
        PreferenciaEstudiante p = guardarPreferenciasEstudiante(req);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mensaje", "Preferencias guardadas correctamente");
        out.put("jornadaPreferida", p.getJornadaPreferida().name());
        out.put("materiasRegistradas", req.getMaterias() == null ? 0 : req.getMaterias().size());
        return out;
    }

    @Override
    public HorarioGeneradoResponse generarHorario(String correo) {
        Integer id = idEstudiantePorCorreo(correo);
        return generarHorarioParaEstudiante(id);
    }

    @Override
    @Transactional
    public Map<String, Object> confirmarHorario(String correo, HorarioGeneradoResponse.OpcionHorario opcion) {
        Integer id = idEstudiantePorCorreo(correo);
        HorarioGeneradoResponse.OpcionHorario confirmada = confirmarOpcion(id, opcion);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mensaje", "Horario confirmado correctamente. Tus asignaciones han sido guardadas.");
        out.put("totalAsignadas", confirmada.getTotalAsignadas());
        out.put("totalPendientes", confirmada.getTotalPendientes());
        return out;
    }

    @Override
    public List<HorarioGeneradoResponse.FilaAsignacion> listarMisAsignaciones(String correo) {
        Integer id = idEstudiantePorCorreo(correo);
        List<Asignacion> lista = asignacionRepo.findByEstudianteIdEstudiante(id);
        List<HorarioGeneradoResponse.FilaAsignacion> out = new ArrayList<>();
        for (Asignacion ent : lista) {
            HorarioGeneradoResponse.FilaAsignacion f = new HorarioGeneradoResponse.FilaAsignacion();
            f.setIdAsignacion(ent.getIdAsignacion());
            if (ent.getGrupo() != null) {
                Grupo g = ent.getGrupo();
                f.setIdGrupo(g.getIdGrupo());
                f.setNombreGrupo(g.getNombre());
                f.setDocente(g.getDocente() == null ? "" : g.getDocente().getNombre());
                f.setSalon(g.getSalon() == null ? "" : g.getSalon().getNombre());
                if (g.getAsignatura() != null) {
                    f.setIdAsignatura(g.getAsignatura().getIdAsignatura());
                    f.setNombreAsignatura(g.getAsignatura().getNombre());
                }
                f.setHorarios(horarioRepo.findByGrupoIdGrupo(g.getIdGrupo()).stream()
                        .map(h -> h.getDia() + " " + h.getHoraInicio() + "-" + h.getHoraFin()).toList());
            }
            if (f.getNombreAsignatura() == null && ent.getInscripcion() != null) {
                f.setIdAsignatura(ent.getInscripcion().getAsignatura().getIdAsignatura());
                f.setNombreAsignatura(ent.getInscripcion().getAsignatura().getNombre());
            }
            f.setEstado(ent.getEstado().name());
            out.add(f);
        }
        return out;
    }

    // ====================== logica interna ======================

    @Transactional
    protected PreferenciaEstudiante guardarPreferenciasEstudiante(PreferenciasRequest req) {
        Estudiante estudiante = estudianteRepo.findById(req.getIdEstudiante().intValue())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        PreferenciaEstudiante pref = preferenciaRepo.findByEstudianteIdEstudiante(estudiante.getIdEstudiante())
                .orElseGet(PreferenciaEstudiante::new);
        pref.setEstudiante(estudiante);
        pref.setJornadaPreferida(PreferenciaEstudiante.Jornada.valueOf(
                req.getJornadaPreferida() == null || req.getJornadaPreferida().isBlank()
                        ? "SIN_PREFERENCIA" : req.getJornadaPreferida()));
        pref.setPriorizaDocente(req.getPriorizaDocente() == null ? false : req.getPriorizaDocente());
        pref.setObservaciones(req.getObservaciones());
        pref = preferenciaRepo.save(pref);

        if (pref.getIdPreferencia() != null) {
            grupoPrefRepo.eliminarPorPreferencia(pref.getIdPreferencia());
        }

        inscripcionRepo.eliminarPorEstudiante(estudiante.getIdEstudiante());

        List<Integer> idsAEliminar = asignacionRepo.findByEstudianteIdEstudiante(estudiante.getIdEstudiante())
                .stream().map(Asignacion::getIdAsignacion).collect(Collectors.toList());
        if (!idsAEliminar.isEmpty()) asignacionRepo.deleteAllById(idsAEliminar);

        if (req.getMaterias() != null) {
            for (PreferenciasRequest.MateriaSeleccionada ms : req.getMaterias()) {
                Asignatura a = asignaturaRepo.findById(ms.getIdAsignatura().intValue())
                        .orElseThrow(() -> new RuntimeException("Asignatura no existe: " + ms.getIdAsignatura()));
                Inscripcion ins = new Inscripcion();
                ins.setEstudiante(estudiante);
                ins.setAsignatura(a);
                ins.setEstado(Inscripcion.EstadoInscripcion.INSCRITO);
                inscripcionRepo.save(ins);

                if (ms.getIdGrupoPreferido() != null) {
                    GrupoPreferido gp = new GrupoPreferido();
                    gp.setPreferencia(pref);
                    gp.setAsignatura(a);
                    gp.setGrupo(grupoRepo.findById(ms.getIdGrupoPreferido().intValue()).orElse(null));
                    gp.setPrioridad(ms.getPrioridad() == null ? 1 : ms.getPrioridad());
                    grupoPrefRepo.save(gp);
                }
            }
        }
        return pref;
    }

    public Map<String, Object> construirPreferenciasDTO(Integer idEstudiante) {
        Map<String, Object> out = new LinkedHashMap<>();
        PreferenciaEstudiante pref = preferenciaRepo.findByEstudianteIdEstudiante(idEstudiante).orElse(null);
        if (pref == null) {
            out.put("jornadaPreferida", "SIN_PREFERENCIA");
            out.put("priorizaDocente", false);
            out.put("observaciones", "");
            out.put("materiasSeleccionadas", Collections.emptyList());
            return out;
        }
        out.put("jornadaPreferida", pref.getJornadaPreferida().name());
        out.put("priorizaDocente", pref.getPriorizaDocente());
        out.put("observaciones", pref.getObservaciones());
        List<Map<String, Object>> mats = new ArrayList<>();
        for (Inscripcion ins : inscripcionRepo.findByEstudianteIdEstudiante(idEstudiante)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("idAsignatura", ins.getAsignatura().getIdAsignatura());
            m.put("nombreAsignatura", ins.getAsignatura().getNombre());
            m.put("creditos", ins.getAsignatura().getCreditos());
            m.put("codigo", ins.getAsignatura().getCodigo());
            Optional<GrupoPreferido> gp = grupoPrefRepo.findByPreferenciaIdPreferencia(pref.getIdPreferencia())
                    .stream().filter(x -> x.getAsignatura().getIdAsignatura().equals(ins.getAsignatura().getIdAsignatura()))
                    .findFirst();
            if (gp.isPresent() && gp.get().getGrupo() != null) {
                m.put("idGrupoPreferido", gp.get().getGrupo().getIdGrupo());
                m.put("nombreGrupoPreferido", gp.get().getGrupo().getNombre());
                m.put("prioridad", gp.get().getPrioridad());
            } else {
                m.put("idGrupoPreferido", null);
                m.put("nombreGrupoPreferido", null);
                m.put("prioridad", 1);
            }
            mats.add(m);
        }
        out.put("materiasSeleccionadas", mats);
        return out;
    }

    public HorarioGeneradoResponse generarHorarioParaEstudiante(Integer idEstudiante) {
        Estudiante estudiante = estudianteRepo.findById(idEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        List<Inscripcion> inscripciones = inscripcionRepo.findByEstudianteIdEstudiante(idEstudiante);
        PreferenciaEstudiante pref = preferenciaRepo.findByEstudianteIdEstudiante(idEstudiante).orElse(null);
        Map<Integer, Grupo> grupoPreferidoPorAsignatura = new HashMap<>();
        if (pref != null) {
            grupoPrefRepo.findByPreferenciaIdPreferencia(pref.getIdPreferencia()).forEach(gp -> {
                if (gp.getAsignatura() != null && gp.getGrupo() != null) {
                    grupoPreferidoPorAsignatura.put(gp.getAsignatura().getIdAsignatura(), gp.getGrupo());
                }
            });
        }
        PreferenciaEstudiante.Jornada jornada = pref == null ? PreferenciaEstudiante.Jornada.SIN_PREFERENCIA : pref.getJornadaPreferida();
        boolean priorizaDocente = pref != null && Boolean.TRUE.equals(pref.getPriorizaDocente());

        List<OpcionParcial> topOpciones = generarCombinaciones(inscripciones, jornada, grupoPreferidoPorAsignatura, priorizaDocente);

        if (topOpciones.isEmpty()) {
            List<HorarioGeneradoResponse.FilaAsignacion> filasFallback = new ArrayList<>();
            for (Inscripcion ins : inscripciones) {
                Asignatura a = ins.getAsignatura();
                HorarioGeneradoResponse.FilaAsignacion f = new HorarioGeneradoResponse.FilaAsignacion();
                f.setIdAsignatura(a.getIdAsignatura());
                f.setNombreAsignatura(a.getNombre());
                f.setEstado(Asignacion.EstadoAsignacion.PENDIENTE.name());
                f.setHorarios(Collections.emptyList());
                f.setRazonPendiente("No se encontraron combinaciones válidas");
                filasFallback.add(f);
            }
            HorarioGeneradoResponse.OpcionHorario op = new HorarioGeneradoResponse.OpcionHorario();
            op.setIndice(1);
            op.setPuntajeTotal(0);
            op.setTotalAsignadas(0);
            op.setTotalPendientes(filasFallback.size());
            op.setDescripcion("Sin combinaciones disponibles");
            op.setResultados(filasFallback);
            HorarioGeneradoResponse res = new HorarioGeneradoResponse();
            res.setIdEstudiante(estudiante.getIdEstudiante());
            res.setNombreEstudiante(estudiante.getNombre());
            res.setJornadaSeleccionada(jornada.name());
            res.setTotalSolicitadas(inscripciones.size());
            res.setOpciones(Collections.singletonList(op));
            return res;
        }

        List<HorarioGeneradoResponse.OpcionHorario> opciones = new ArrayList<>();
        int idx = 1;
        for (OpcionParcial opParcial : topOpciones) {
            HorarioGeneradoResponse.OpcionHorario op = new HorarioGeneradoResponse.OpcionHorario();
            op.setIndice(idx);
            op.setPuntajeTotal(opParcial.puntaje);
            List<HorarioGeneradoResponse.FilaAsignacion> filas = new ArrayList<>();
            int asignadas = 0, pendientes = 0;
            for (Map.Entry<Integer, Grupo> e : opParcial.asignados.entrySet()) {
                Asignatura a = asignaturaRepo.findById(e.getKey()).orElseThrow();
                Grupo g = e.getValue();
                HorarioGeneradoResponse.FilaAsignacion f = new HorarioGeneradoResponse.FilaAsignacion();
                f.setIdAsignatura(a.getIdAsignatura());
                f.setNombreAsignatura(a.getNombre());
                f.setIdGrupo(g.getIdGrupo());
                f.setNombreGrupo(g.getNombre());
                f.setDocente(g.getDocente() == null ? "" : g.getDocente().getNombre());
                f.setSalon(g.getSalon() == null ? "" : g.getSalon().getNombre());
                f.setEstado(Asignacion.EstadoAsignacion.ASIGNADO.name());
                f.setHorarios(horarioRepo.findByGrupoIdGrupo(g.getIdGrupo()).stream()
                        .map(h -> h.getDia() + " " + h.getHoraInicio() + "-" + h.getHoraFin())
                        .collect(Collectors.toList()));
                filas.add(f);
                asignadas++;
            }
            for (Integer idAsigPend : opParcial.pendientes) {
                Asignatura a = asignaturaRepo.findById(idAsigPend).orElseThrow();
                HorarioGeneradoResponse.FilaAsignacion f = new HorarioGeneradoResponse.FilaAsignacion();
                f.setIdAsignatura(a.getIdAsignatura());
                f.setNombreAsignatura(a.getNombre());
                f.setEstado(Asignacion.EstadoAsignacion.PENDIENTE.name());
                f.setHorarios(Collections.emptyList());
                f.setRazonPendiente("Todos los grupos cruzan horario con las otras materias seleccionadas");
                filas.add(f);
                pendientes++;
            }
            op.setTotalAsignadas(asignadas);
            op.setTotalPendientes(pendientes);
            op.setResultados(filas);
            op.setDescripcion(describir(opParcial, jornada, grupoPreferidoPorAsignatura));
            opciones.add(op);
            idx++;
        }

        HorarioGeneradoResponse res = new HorarioGeneradoResponse();
        res.setIdEstudiante(estudiante.getIdEstudiante());
        res.setNombreEstudiante(estudiante.getNombre());
        res.setJornadaSeleccionada(jornada.name());
        res.setTotalSolicitadas(inscripciones.size());
        res.setOpciones(opciones);
        return res;
    }

    @Transactional
    protected HorarioGeneradoResponse.OpcionHorario confirmarOpcion(Integer idEstudiante, HorarioGeneradoResponse.OpcionHorario opcion) {
        Estudiante estudiante = estudianteRepo.findById(idEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        List<Integer> idsAEliminar = asignacionRepo.findByEstudianteIdEstudiante(idEstudiante)
                .stream().map(Asignacion::getIdAsignacion).collect(Collectors.toList());
        if (!idsAEliminar.isEmpty()) asignacionRepo.deleteAllById(idsAEliminar);

        for (HorarioGeneradoResponse.FilaAsignacion f : opcion.getResultados()) {
            Asignacion ent = new Asignacion();
            ent.setEstudiante(estudiante);
            if (f.getIdGrupo() != null && Asignacion.EstadoAsignacion.ASIGNADO.name().equals(f.getEstado())) {
                Grupo g = grupoRepo.findById(f.getIdGrupo()).orElse(null);
                ent.setGrupo(g);
                ent.setEstado(Asignacion.EstadoAsignacion.ASIGNADO);
            } else {
                ent.setEstado(Asignacion.EstadoAsignacion.PENDIENTE);
            }
            if (f.getIdAsignatura() != null) {
                Inscripcion ins = inscripcionRepo.findByEstudianteIdEstudiante(idEstudiante).stream()
                        .filter(i -> i.getAsignatura().getIdAsignatura().equals(f.getIdAsignatura()))
                        .findFirst().orElse(null);
                ent.setInscripcion(ins);
            }
            asignacionRepo.save(ent);
        }
        return opcion;
    }

    // ====================== backtracking combinacional ======================

    private static class OpcionParcial implements Comparable<OpcionParcial> {
        Map<Integer, Grupo> asignados = new LinkedHashMap<>();
        List<Integer> pendientes = new ArrayList<>();
        int puntaje;
        @Override public int compareTo(OpcionParcial o) { return Integer.compare(o.puntaje, this.puntaje); }
    }

    private List<OpcionParcial> generarCombinaciones(List<Inscripcion> inscripciones,
                                                     PreferenciaEstudiante.Jornada jornada,
                                                     Map<Integer, Grupo> grupoPrefPorAsig,
                                                     boolean priorizaDocente) {
        List<Inscripcion> ordenadas = new ArrayList<>(inscripciones);
        ordenadas.sort((a, b) -> {
            boolean aPref = grupoPrefPorAsig.containsKey(a.getAsignatura().getIdAsignatura());
            boolean bPref = grupoPrefPorAsig.containsKey(b.getAsignatura().getIdAsignatura());
            if (aPref != bPref) return aPref ? -1 : 1;
            int na = grupoRepo.findByAsignaturaIdAsignatura(a.getAsignatura().getIdAsignatura()).size();
            int nb = grupoRepo.findByAsignaturaIdAsignatura(b.getAsignatura().getIdAsignatura()).size();
            return Integer.compare(na, nb);
        });

        PriorityQueue<OpcionParcial> top = new PriorityQueue<>();
        backtrack(ordenadas, 0, new ArrayList<>(), new OpcionParcial(),
                jornada, grupoPrefPorAsig, priorizaDocente, top);

        List<OpcionParcial> lista = new ArrayList<>(top);
        Collections.sort(lista);
        return lista;
    }

    private void backtrack(List<Inscripcion> ordenadas,
                           int idx,
                           List<Horario> ocupados,
                           OpcionParcial actual,
                           PreferenciaEstudiante.Jornada jornada,
                           Map<Integer, Grupo> grupoPrefPorAsig,
                           boolean priorizaDocente,
                           PriorityQueue<OpcionParcial> top) {
        if (idx == ordenadas.size()) {
            OpcionParcial copia = new OpcionParcial();
            copia.asignados = new LinkedHashMap<>(actual.asignados);
            copia.pendientes = new ArrayList<>(actual.pendientes);
            copia.puntaje = actual.puntaje;
            top.offer(copia);
            if (top.size() > MAX_OPCIONES * 5) top.poll();
            return;
        }

        Inscripcion ins = ordenadas.get(idx);
        Asignatura a = ins.getAsignatura();
        List<Grupo> cands = grupoRepo.findByAsignaturaIdAsignatura(a.getIdAsignatura());
        cands = cands.stream()
                .filter(g -> g.getCupoDisponible() == null || g.getCupoDisponible() > 0 || g.getCupo() > 0)
                .collect(Collectors.toList());
        cands = ordenarCandidatos(cands, jornada, grupoPrefPorAsig.get(a.getIdAsignatura()), priorizaDocente);

        boolean algunaAsignada = false;
        for (Grupo g : cands) {
            List<Horario> gh = horarioRepo.findByGrupoIdGrupo(g.getIdGrupo());
            if (!hayCruce(ocupados, gh)) {
                int pts = puntajeGrupo(g, jornada, grupoPrefPorAsig.get(a.getIdAsignatura()), priorizaDocente);
                actual.asignados.put(a.getIdAsignatura(), g);
                actual.puntaje += pts;
                ocupados.addAll(gh);
                backtrack(ordenadas, idx + 1, ocupados, actual,
                        jornada, grupoPrefPorAsig, priorizaDocente, top);
                ocupados.subList(ocupados.size() - gh.size(), ocupados.size()).clear();
                actual.puntaje -= pts;
                actual.asignados.remove(a.getIdAsignatura());
                algunaAsignada = true;
            }
        }

        if (!algunaAsignada) {
            actual.pendientes.add(a.getIdAsignatura());
            actual.puntaje -= 200;
            backtrack(ordenadas, idx + 1, ocupados, actual,
                    jornada, grupoPrefPorAsig, priorizaDocente, top);
            actual.pendientes.remove(actual.pendientes.size() - 1);
            actual.puntaje += 200;
        } else if (!cands.isEmpty()) {
            actual.pendientes.add(a.getIdAsignatura());
            actual.puntaje -= 200;
            backtrack(ordenadas, idx + 1, ocupados, actual,
                    jornada, grupoPrefPorAsig, priorizaDocente, top);
            actual.pendientes.remove(actual.pendientes.size() - 1);
            actual.puntaje += 200;
        }
    }

    private int puntajeGrupo(Grupo g, PreferenciaEstudiante.Jornada j, Grupo preferido, boolean priorizaDocente) {
        int p = 50;
        if (preferido != null && g.getIdGrupo().equals(preferido.getIdGrupo())) p += 500;
        if (j != PreferenciaEstudiante.Jornada.SIN_PREFERENCIA) p += puntajeJornada(g, j);
        if (priorizaDocente && g.getDocente() != null) p += 15;
        if (g.getSalon() != null) p += 5;
        return p;
    }

    private List<Grupo> ordenarCandidatos(List<Grupo> cands, PreferenciaEstudiante.Jornada j, Grupo preferido, boolean priorizaDocente) {
        if (cands.isEmpty()) return cands;
        List<Grupo> ordenado = new ArrayList<>(cands);
        ordenado.sort((a, b) -> Integer.compare(
                puntajeGrupo(b, j, preferido, priorizaDocente),
                puntajeGrupo(a, j, preferido, priorizaDocente)));
        return ordenado;
    }

    private int puntajeJornada(Grupo g, PreferenciaEstudiante.Jornada j) {
        int p = 0;
        for (Horario h : horarioRepo.findByGrupoIdGrupo(g.getIdGrupo())) {
            PreferenciaEstudiante.Jornada hj = clasificar(h.getHoraInicio());
            if (hj == j) p += 50;
            else if (j == PreferenciaEstudiante.Jornada.MANANA && hj == PreferenciaEstudiante.Jornada.TARDE) p -= 10;
            else if (j == PreferenciaEstudiante.Jornada.TARDE && hj == PreferenciaEstudiante.Jornada.MANANA) p -= 10;
            else if (j == PreferenciaEstudiante.Jornada.TARDE && hj == PreferenciaEstudiante.Jornada.NOCHE) p += 10;
        }
        return p;
    }

    private PreferenciaEstudiante.Jornada clasificar(Object hora) {
        LocalTime t = (hora instanceof LocalTime) ? (LocalTime) hora : LocalTime.parse(String.valueOf(hora));
        if (t.isBefore(LocalTime.of(13, 0))) return PreferenciaEstudiante.Jornada.MANANA;
        if (t.isBefore(LocalTime.of(19, 0))) return PreferenciaEstudiante.Jornada.TARDE;
        return PreferenciaEstudiante.Jornada.NOCHE;
    }

    private boolean hayCruce(List<Horario> ocupados, List<Horario> nuevos) {
        for (Horario n : nuevos) {
            for (Horario o : ocupados) {
                if (!o.getDia().equalsIgnoreCase(n.getDia())) continue;
                LocalTime oi = (o.getHoraInicio() instanceof LocalTime) ? o.getHoraInicio() : LocalTime.parse(String.valueOf(o.getHoraInicio()));
                LocalTime of = (o.getHoraFin() instanceof LocalTime) ? o.getHoraFin() : LocalTime.parse(String.valueOf(o.getHoraFin()));
                LocalTime ni = (n.getHoraInicio() instanceof LocalTime) ? n.getHoraInicio() : LocalTime.parse(String.valueOf(n.getHoraInicio()));
                LocalTime nf = (n.getHoraFin() instanceof LocalTime) ? n.getHoraFin() : LocalTime.parse(String.valueOf(n.getHoraFin()));
                if (ni.isBefore(of) && nf.isAfter(oi)) return true;
            }
        }
        return false;
    }

    private String describir(OpcionParcial op, PreferenciaEstudiante.Jornada jornada, Map<Integer, Grupo> grupoPrefPorAsig) {
        List<String> tags = new ArrayList<>();
        int gruposPref = 0;
        for (Map.Entry<Integer, Grupo> e : op.asignados.entrySet()) {
            Grupo pref = grupoPrefPorAsig.get(e.getKey());
            if (pref != null && pref.getIdGrupo().equals(e.getValue().getIdGrupo())) gruposPref++;
        }
        if (gruposPref > 0) tags.add(gruposPref + " grupo(s) preferido(s)");
        int puntJorn = 0;
        for (Grupo g : op.asignados.values()) puntJorn += puntajeJornada(g, jornada);
        if (jornada != PreferenciaEstudiante.Jornada.SIN_PREFERENCIA && puntJorn > 0)
            tags.add("Ajustado a jornada " + jornada.name().toLowerCase(Locale.ROOT));
        tags.add(op.asignados.size() + "/" + (op.asignados.size() + op.pendientes.size()) + " asignadas");
        return String.join(" · ", tags);
    }
}
