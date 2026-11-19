package com.scheduleu.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduleu.domain.port.in.AsistenteUseCase;
import com.scheduleu.domain.port.in.EstudianteHorarioUseCase;
import com.scheduleu.domain.port.out.AsistenteIAPort;
import com.scheduleu.infrastructure.adapter.in.web.dto.AsistenteRespuesta;
import com.scheduleu.infrastructure.adapter.in.web.dto.HorarioGeneradoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de aplicacion del asistente academico.
 *
 * Estrategia hibrida:
 *  1. Java organiza el horario confirmado del estudiante (por dia, con huecos).
 *  2. La IA solo CLASIFICA la pregunta (devuelve un JSON corto con la intencion).
 *  3. Java arma la respuesta con los datos reales (rapido y sin inventar nada).
 *  4. Si la pregunta no encaja en ninguna intencion, se usa la IA con el
 *     horario completo como contexto.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsistenteService implements AsistenteUseCase {

    private static final String INSTRUCCION_CLASIFICADOR = """
            Eres un clasificador de preguntas para el asistente de horarios de una universidad.
            Lee la pregunta del estudiante y responde UNICAMENTE con un objeto JSON, sin texto adicional
            ni bloques de codigo, con esta forma:
            {"intencion":"...","dia":"...","materia":"..."}

            Valores posibles de "intencion":
            - CLASES_DIA: quiere saber que clases tiene un dia concreto. Usa "dia".
            - PRIMERA_CLASE: quiere saber cual es su primera clase de la semana o a que hora empieza.
            - HUECOS: quiere saber sus horas libres o huecos entre clases. "dia" es opcional.
            - DETALLE_MATERIA: pregunta por una materia concreta (salon, docente, horario, grupo). Usa "materia".
            - DIAS_LIBRES: quiere saber que dias no tiene clases.
            - RESUMEN: pide un resumen o vista general de todo su horario.
            - OTRO: cualquier otra pregunta.

            Reglas:
            - "dia" solo puede ser LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO o DOMINGO
              (mayusculas y sin tildes), o "" si no aplica.
            - "materia" debe ser exactamente uno de los nombres de la lista de materias que se te da,
              o "" si no aplica.
            - Si la pregunta menciona "hoy" o "manana", usa el dia indicado en "Hoy es ... Manana es ...".
            """;

    private static final String INSTRUCCION_SISTEMA = """
            Eres el asistente academico de ScheduleU, un sistema de gestion de horarios universitarios.
            Ayudas al estudiante a entender su horario: que clases tiene cada dia, en que salon y con
            que docente, sus huecos libres, su primera clase y los cruces de horario.
            Responde SIEMPRE en espanol, de forma breve, clara y amable (maximo 5 lineas).
            Usa unicamente el horario que se te entrega. Ya esta ordenado por dia y de menor a mayor hora,
            y los huecos libres ya estan calculados: usalos tal cual, no los recalcules.
            Si un dato no aparece en el horario, dilo con honestidad en lugar de inventarlo.
            """;

    private static final String MENSAJE_SIN_HORARIO =
            "Aún no tienes un horario confirmado. Ve a «Mi Horario», genera y confirma una opción, "
                    + "y luego vuelve a preguntarme.";

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private static final List<String> ORDEN_DIAS =
            List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO");

    private static final Pattern PATRON_HORARIO =
            Pattern.compile("^(\\S+)\\s+(\\d{1,2}:\\d{2}(?::\\d{2})?)\\s*-\\s*(\\d{1,2}:\\d{2}(?::\\d{2})?)$");

    private static final DateTimeFormatter FORMATO_ENTRADA = DateTimeFormatter.ofPattern("H:mm[:ss]");
    private static final DateTimeFormatter FORMATO_SALIDA = DateTimeFormatter.ofPattern("HH:mm");

    private record Bloque(LocalTime inicio, LocalTime fin, String materia,
                          String grupo, String docente, String salon) {

        String detalle() {
            List<String> extras = new ArrayList<>();
            if (!grupo.isEmpty()) extras.add("grupo " + grupo);
            if (!docente.isEmpty()) extras.add("docente " + docente);
            if (!salon.isEmpty()) extras.add("salón " + salon);
            return extras.isEmpty() ? "" : " (" + String.join(", ", extras) + ")";
        }

        String descripcion() {
            return materia + detalle();
        }
    }

    private record HorarioEstudiante(Map<String, List<Bloque>> porDia, List<String> pendientes,
                                     List<String> sinFormato, List<String> materias) { }

    private record Clasificacion(String intencion, String dia, String materia) { }

    private final EstudianteHorarioUseCase estudianteHorario;
    private final AsistenteIAPort asistenteIA;
    private final ObjectMapper objectMapper;

    // ====================== flujo principal ======================

    @Override
    public AsistenteRespuesta consultar(String correo, String pregunta) {
        if (pregunta == null || pregunta.isBlank()) {
            throw new RuntimeException("Escribe una pregunta para el asistente");
        }
        String consulta = pregunta.trim();

        long t0 = System.nanoTime();
        List<HorarioGeneradoResponse.FilaAsignacion> asignaciones =
                estudianteHorario.listarMisAsignaciones(correo);
        if (asignaciones.isEmpty()) {
            return new AsistenteRespuesta(MENSAJE_SIN_HORARIO);
        }
        HorarioEstudiante horario = organizar(asignaciones);
        log.info("Horario armado en {} ms", milisegundos(t0));

        long t1 = System.nanoTime();
        Clasificacion c = clasificar(consulta, horario.materias());
        log.info("Pregunta clasificada como {} (dia='{}', materia='{}') en {} ms",
                c.intencion(), c.dia(), c.materia(), milisegundos(t1));

        String respuesta = switch (c.intencion()) {
            case "CLASES_DIA" -> responderClasesDia(horario, c.dia());
            case "PRIMERA_CLASE" -> responderPrimeraClase(horario);
            case "HUECOS" -> responderHuecos(horario, c.dia());
            case "DETALLE_MATERIA" -> responderDetalleMateria(horario, c.materia());
            case "DIAS_LIBRES" -> responderDiasLibres(horario);
            case "RESUMEN" -> responderResumen(horario);
            default -> responderConIA(horario, consulta);
        };
        return new AsistenteRespuesta(respuesta);
    }

    // ====================== clasificacion con IA ======================

    private Clasificacion clasificar(String pregunta, List<String> materias) {
        String consulta = contextoFecha()
                + "\nMaterias del estudiante: " + String.join(" | ", materias)
                + "\nPregunta del estudiante: " + pregunta;

        String texto = asistenteIA.responder(INSTRUCCION_CLASIFICADOR, consulta);
        try {
            int ini = texto.indexOf('{');
            int fin = texto.lastIndexOf('}');
            if (ini < 0 || fin <= ini) {
                return new Clasificacion("OTRO", "", "");
            }
            JsonNode n = objectMapper.readTree(texto.substring(ini, fin + 1));
            String intencion = n.path("intencion").asText("OTRO").trim().toUpperCase(Locale.ROOT);
            String dia = normalizarDia(n.path("dia").asText(""));
            String materia = n.path("materia").asText("").trim();
            return new Clasificacion(intencion, ORDEN_DIAS.contains(dia) ? dia : "", materia);
        } catch (Exception e) {
            log.warn("No se pudo interpretar la clasificacion del asistente: {}", texto);
            return new Clasificacion("OTRO", "", "");
        }
    }

    private String contextoFecha() {
        LocalDate hoy = LocalDate.now(ZONA);
        String dHoy = ORDEN_DIAS.get(hoy.getDayOfWeek().getValue() - 1);
        String dManana = ORDEN_DIAS.get(hoy.plusDays(1).getDayOfWeek().getValue() - 1);
        return "Hoy es " + dHoy + ". Manana es " + dManana + ".";
    }

    // ====================== respuestas armadas por Java ======================

    private String responderClasesDia(HorarioEstudiante h, String dia) {
        if (dia.isEmpty()) {
            return "¿De qué día quieres saber? Dime un día de lunes a domingo.";
        }
        List<Bloque> bloques = h.porDia().getOrDefault(dia, List.of());
        if (bloques.isEmpty()) {
            return "El " + nombreDia(dia) + " no tienes clases.";
        }
        StringBuilder sb = new StringBuilder("Tus clases del ")
                .append(nombreDia(dia)).append(" (").append(bloques.size()).append("):\n");
        for (Bloque b : bloques) {
            sb.append("• ").append(hora(b.inicio())).append("-").append(hora(b.fin()))
                    .append(" ").append(b.descripcion()).append('\n');
        }
        return sb.toString().trim();
    }

    private String responderPrimeraClase(HorarioEstudiante h) {
        for (String dia : diasConClases(h)) {
            Bloque b = h.porDia().get(dia).get(0);
            return "Tu primera clase de la semana es el " + nombreDia(dia) + " a las "
                    + hora(b.inicio()) + ": " + b.descripcion() + ".";
        }
        return "No encontré clases con horario en tu horario confirmado.";
    }

    private String responderHuecos(HorarioEstudiante h, String dia) {
        if (!dia.isEmpty()) {
            List<Bloque> bloques = h.porDia().getOrDefault(dia, List.of());
            if (bloques.isEmpty()) {
                return "El " + nombreDia(dia) + " no tienes clases.";
            }
            List<String> huecos = huecosDe(bloques);
            if (huecos.isEmpty()) {
                return "El " + nombreDia(dia) + " no tienes huecos libres entre tus clases.";
            }
            StringBuilder sb = new StringBuilder("Huecos libres del ").append(nombreDia(dia)).append(":\n");
            for (String hueco : huecos) {
                sb.append("• ").append(hueco).append('\n');
            }
            return sb.toString().trim();
        }

        StringBuilder sb = new StringBuilder();
        for (String d : diasConClases(h)) {
            List<String> huecos = huecosDe(h.porDia().get(d));
            if (!huecos.isEmpty()) {
                sb.append("• ").append(cap(nombreDia(d))).append(": ")
                        .append(String.join(", ", huecos)).append('\n');
            }
        }
        if (sb.length() == 0) {
            return "No tienes huecos libres entre clases en ningún día.";
        }
        return ("Tus huecos libres entre clases:\n" + sb).trim();
    }

    private String responderDetalleMateria(HorarioEstudiante h, String materiaPedida) {
        if (materiaPedida == null || materiaPedida.isBlank()) {
            return "¿De qué materia quieres saber? Las tuyas son: " + String.join(", ", h.materias()) + ".";
        }
        String materia = buscarMateria(materiaPedida, h.materias());
        if (materia == null) {
            return "No encontré esa materia en tu horario confirmado. Tus materias son: "
                    + String.join(", ", h.materias()) + ".";
        }

        List<String> sesiones = new ArrayList<>();
        Bloque primero = null;
        for (String dia : ORDEN_DIAS) {
            for (Bloque b : h.porDia().getOrDefault(dia, List.of())) {
                if (b.materia().equals(materia)) {
                    sesiones.add(nombreDia(dia) + " " + hora(b.inicio()) + "-" + hora(b.fin()));
                    if (primero == null) primero = b;
                }
            }
        }
        if (primero == null) {
            return materia + " está pendiente de asignar, así que todavía no tiene horario.";
        }

        StringBuilder sb = new StringBuilder(materia).append(":\n");
        if (!primero.grupo().isEmpty()) sb.append("• Grupo: ").append(primero.grupo()).append('\n');
        if (!primero.docente().isEmpty()) sb.append("• Docente: ").append(primero.docente()).append('\n');
        if (!primero.salon().isEmpty()) sb.append("• Salón: ").append(primero.salon()).append('\n');
        sb.append("• Horario: ").append(String.join("; ", sesiones));
        return sb.toString();
    }

    private String responderDiasLibres(HorarioEstudiante h) {
        List<String> libres = new ArrayList<>();
        for (String dia : ORDEN_DIAS.subList(0, 6)) {
            if (h.porDia().getOrDefault(dia, List.of()).isEmpty()) {
                libres.add(nombreDia(dia));
            }
        }
        if (libres.isEmpty()) {
            return "Tienes clases todos los días, de lunes a sábado.";
        }
        return "No tienes clases el: " + String.join(", ", libres) + ".";
    }

    private String responderResumen(HorarioEstudiante h) {
        StringBuilder sb = new StringBuilder("Resumen de tu horario confirmado:\n");
        int total = 0;
        for (String dia : diasConClases(h)) {
            List<String> items = new ArrayList<>();
            for (Bloque b : h.porDia().get(dia)) {
                items.add(b.materia() + " (" + hora(b.inicio()) + "-" + hora(b.fin()) + ")");
                total++;
            }
            sb.append("• ").append(cap(nombreDia(dia))).append(": ").append(String.join(", ", items)).append('\n');
        }
        sb.append("Total: ").append(total).append(" bloques de clase por semana.");
        if (!h.pendientes().isEmpty()) {
            sb.append("\nPendientes de asignar: ").append(String.join(", ", h.pendientes())).append(".");
        }
        return sb.toString();
    }

    /** Respaldo: preguntas libres, la IA responde con el horario completo como contexto. */
    private String responderConIA(HorarioEstudiante h, String pregunta) {
        String consulta = contextoParaIA(h) + "\nPregunta del estudiante: " + pregunta;
        return asistenteIA.responder(INSTRUCCION_SISTEMA, consulta);
    }

    // ====================== organizacion del horario ======================

    private HorarioEstudiante organizar(List<HorarioGeneradoResponse.FilaAsignacion> asignaciones) {
        Map<String, List<Bloque>> porDia = new LinkedHashMap<>();
        for (String dia : ORDEN_DIAS) {
            porDia.put(dia, new ArrayList<>());
        }
        List<String> pendientes = new ArrayList<>();
        List<String> sinFormato = new ArrayList<>();
        Set<String> materias = new LinkedHashSet<>();

        for (HorarioGeneradoResponse.FilaAsignacion a : asignaciones) {
            String materia = nombreSeguro(a.getNombreAsignatura());
            materias.add(materia);

            if (!"ASIGNADO".equalsIgnoreCase(a.getEstado())) {
                pendientes.add(materia);
                continue;
            }

            List<String> horarios = a.getHorarios() == null ? List.of() : a.getHorarios();
            if (horarios.isEmpty()) {
                sinFormato.add(materia + " (sin horario registrado)");
                continue;
            }

            for (String texto : horarios) {
                Matcher m = PATRON_HORARIO.matcher(texto == null ? "" : texto.trim());
                if (m.matches()) {
                    try {
                        LocalTime inicio = LocalTime.parse(m.group(2), FORMATO_ENTRADA);
                        LocalTime fin = LocalTime.parse(m.group(3), FORMATO_ENTRADA);
                        porDia.computeIfAbsent(normalizarDia(m.group(1)), k -> new ArrayList<>())
                                .add(new Bloque(inicio, fin, materia,
                                        limpio(a.getNombreGrupo()), limpio(a.getDocente()), limpio(a.getSalon())));
                        continue;
                    } catch (RuntimeException ignorado) {
                        // si no se puede interpretar, se informa tal cual abajo
                    }
                }
                sinFormato.add(materia + ": " + texto);
            }
        }

        for (List<Bloque> lista : porDia.values()) {
            lista.sort(Comparator.comparing(Bloque::inicio));
        }
        return new HorarioEstudiante(porDia, pendientes, sinFormato, new ArrayList<>(materias));
    }

    private List<String> diasConClases(HorarioEstudiante h) {
        List<String> dias = new ArrayList<>();
        for (Map.Entry<String, List<Bloque>> e : h.porDia().entrySet()) {
            if (!e.getValue().isEmpty()) {
                dias.add(e.getKey());
            }
        }
        return dias;
    }

    private List<String> huecosDe(List<Bloque> bloques) {
        List<String> huecos = new ArrayList<>();
        LocalTime finPrevio = null;
        for (Bloque b : bloques) {
            if (finPrevio != null && b.inicio().isAfter(finPrevio)) {
                huecos.add(hora(finPrevio) + "-" + hora(b.inicio()) + " (" + duracion(finPrevio, b.inicio()) + ")");
            }
            if (finPrevio == null || b.fin().isAfter(finPrevio)) {
                finPrevio = b.fin();
            }
        }
        return huecos;
    }

    /** Texto completo del horario para el modo de respaldo con IA. */
    private String contextoParaIA(HorarioEstudiante h) {
        StringBuilder sb = new StringBuilder(
                "Horario confirmado del estudiante, agrupado por dia (huecos libres ya calculados):\n");
        List<String> diasSinClases = new ArrayList<>();
        String primeraClase = null;
        int total = 0;

        for (Map.Entry<String, List<Bloque>> entrada : h.porDia().entrySet()) {
            List<Bloque> bloques = entrada.getValue();
            if (bloques.isEmpty()) {
                if (ORDEN_DIAS.contains(entrada.getKey())) {
                    diasSinClases.add(entrada.getKey());
                }
                continue;
            }
            sb.append('\n').append(entrada.getKey()).append(":\n");
            LocalTime finPrevio = null;
            for (Bloque b : bloques) {
                if (finPrevio != null) {
                    if (b.inicio().isAfter(finPrevio)) {
                        sb.append("  * Hueco libre ").append(hora(finPrevio)).append("-").append(hora(b.inicio()))
                                .append(" (").append(duracion(finPrevio, b.inicio())).append(")\n");
                    } else if (b.inicio().isBefore(finPrevio)) {
                        sb.append("  * ATENCION: cruce de horario con la clase anterior\n");
                    }
                }
                sb.append("  - ").append(hora(b.inicio())).append("-").append(hora(b.fin()))
                        .append(" ").append(b.descripcion()).append('\n');
                if (primeraClase == null) {
                    primeraClase = entrada.getKey() + " a las " + hora(b.inicio()) + " - " + b.descripcion();
                }
                if (finPrevio == null || b.fin().isAfter(finPrevio)) {
                    finPrevio = b.fin();
                }
                total++;
            }
        }

        sb.append('\n');
        if (primeraClase != null) {
            sb.append("Primera clase de la semana: ").append(primeraClase).append('\n');
        }
        if (!diasSinClases.isEmpty()) {
            sb.append("Dias sin clases: ").append(String.join(", ", diasSinClases)).append('\n');
        }
        sb.append("Total de bloques de clase por semana: ").append(total).append('\n');
        if (!h.pendientes().isEmpty()) {
            sb.append("Materias pendientes de asignar (aun sin horario): ")
                    .append(String.join(", ", h.pendientes())).append('\n');
        }
        for (String linea : h.sinFormato()) {
            sb.append("Otra clase con horario no interpretable: ").append(linea).append('\n');
        }
        return sb.toString();
    }

    // ====================== utilidades ======================

    private String buscarMateria(String pedida, List<String> materias) {
        String p = simplificar(pedida);
        for (String m : materias) {
            if (simplificar(m).equals(p)) return m;
        }
        for (String m : materias) {
            String s = simplificar(m);
            if (s.contains(p) || p.contains(s)) return m;
        }
        return null;
    }

    private String simplificar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarDia(String dia) {
        return Normalizer.normalize(dia == null ? "" : dia, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase(Locale.ROOT);
    }

    private String nombreDia(String dia) {
        return switch (dia) {
            case "MIERCOLES" -> "miércoles";
            case "SABADO" -> "sábado";
            default -> dia.toLowerCase(Locale.ROOT);
        };
    }

    private String cap(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private String hora(LocalTime t) {
        return t.format(FORMATO_SALIDA);
    }

    private String duracion(LocalTime desde, LocalTime hasta) {
        long minutos = Duration.between(desde, hasta).toMinutes();
        long h = minutos / 60;
        long m = minutos % 60;
        if (h == 0) return m + " min";
        if (m == 0) return h + " h";
        return h + " h " + m + " min";
    }

    private long milisegundos(long desdeNanos) {
        return (System.nanoTime() - desdeNanos) / 1_000_000;
    }

    private String limpio(String s) {
        return (s == null || s.isBlank()) ? "" : s.trim();
    }

    private String nombreSeguro(String nombre) {
        return (nombre == null || nombre.isBlank()) ? "Asignatura sin nombre" : nombre.trim();
    }
}