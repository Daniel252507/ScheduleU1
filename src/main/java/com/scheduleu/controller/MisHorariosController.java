package com.scheduleu.controller;

import com.scheduleu.dto.ConfirmarHorarioRequest;
import com.scheduleu.dto.HorarioGeneradoResponse;
import com.scheduleu.dto.PreferenciasRequest;
import com.scheduleu.model.*;
import com.scheduleu.repository.*;
import com.scheduleu.security.JwtTokenProvider;
import com.scheduleu.service.EstudianteHorarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/mis-horarios")
@RequiredArgsConstructor
public class MisHorariosController {

    private final EstudianteHorarioService service;
    private final EstudianteRepository estudianteRepo;
    private final AsignaturaRepository asignaturaRepo;
    private final GrupoRepository grupoRepo;
    private final HorarioRepository horarioRepo;
    private final AsignacionRepository asignacionRepo;
    private final JwtTokenProvider jwtTokenProvider;

    private Integer extraerIdEstudiante(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new RuntimeException("No autorizado");
        String correo = jwtTokenProvider.getCorreoFromToken(header.substring(7));
        Estudiante e = estudianteRepo.findAll().stream()
                .filter(x -> x.getUsuario() != null && x.getUsuario().getCorreo().equalsIgnoreCase(correo))
                .findFirst().orElse(null);
        if (e == null) throw new RuntimeException("No existe registro de estudiante para tu usuario. Contacta al administrador.");
        return e.getIdEstudiante();
    }

    @GetMapping("/perfil")
    public ResponseEntity<Map<String, Object>> perfil(HttpServletRequest req) {
        Integer id = extraerIdEstudiante(req);
        Estudiante e = estudianteRepo.findById(id).orElseThrow();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("idEstudiante", e.getIdEstudiante());
        out.put("nombre", e.getNombre());
        out.put("codigo", e.getCodigo());
        out.put("semestre", e.getSemestre());
        out.put("correo", e.getCorreo());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/asignaturas-disponibles")
    public ResponseEntity<List<Map<String, Object>>> asignaturasDisponibles(HttpServletRequest req) {
        Integer id = extraerIdEstudiante(req);
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
        return ResponseEntity.ok(out);
    }

    @GetMapping("/preferencias")
    public ResponseEntity<Map<String, Object>> verPreferencias(HttpServletRequest req) {
        Integer id = extraerIdEstudiante(req);
        Map<String, Object> dto = service.obtenerPreferenciasDTO(id);
        dto.put("idEstudiante", Long.valueOf(id));
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/guardar-preferencias")
    public ResponseEntity<?> guardarPreferencias(@RequestBody PreferenciasRequest body, HttpServletRequest req) {
        Integer id = extraerIdEstudiante(req);
        body.setIdEstudiante(Long.valueOf(id));
        PreferenciaEstudiante p = service.guardarPreferencias(body);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mensaje", "Preferencias guardadas correctamente");
        out.put("jornadaPreferida", p.getJornadaPreferida().name());
        out.put("materiasRegistradas", body.getMaterias() == null ? 0 : body.getMaterias().size());
        return ResponseEntity.ok(out);
    }

    @PostMapping("/generar")
    public ResponseEntity<HorarioGeneradoResponse> generarMiHorario(HttpServletRequest req) {
        Integer id = extraerIdEstudiante(req);
        HorarioGeneradoResponse res = service.generarHorarioParaEstudiante(id);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarOpcion(@RequestBody HorarioGeneradoResponse.OpcionHorario opcion,
                                             HttpServletRequest req) {
        Integer id = extraerIdEstudiante(req);
        HorarioGeneradoResponse.OpcionHorario confirmada = service.confirmarHorario(id, opcion);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mensaje", "Horario confirmado correctamente. Tus asignaciones han sido guardadas.");
        out.put("totalAsignadas", confirmada.getTotalAsignadas());
        out.put("totalPendientes", confirmada.getTotalPendientes());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/asignaciones")
    public ResponseEntity<List<HorarioGeneradoResponse.FilaAsignacion>> misAsignaciones(HttpServletRequest req) {
        Integer id = extraerIdEstudiante(req);
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
        return ResponseEntity.ok(out);
    }
}
