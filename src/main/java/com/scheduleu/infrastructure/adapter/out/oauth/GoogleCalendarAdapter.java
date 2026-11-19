package com.scheduleu.infrastructure.adapter.out.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.scheduleu.domain.model.EventoCalendario;
import com.scheduleu.domain.port.out.ProveedorCalendarioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adaptador del puerto {@link ProveedorCalendarioPort} para Google Calendar.
 *
 * El navegador obtiene un token de acceso (scope {@code calendar.events}) y lo
 * envia al backend; este adaptador usa ese token para crear un evento por cada
 * bloque del horario. Cada evento se repite semanalmente durante las semanas
 * configuradas, de modo que el horario queda agendado todo el semestre.
 *
 * Se implementa con las utilidades de la plataforma (java.net.http + Jackson)
 * en lugar de la libreria google-api-client para no cargar el contenedor: el
 * plan gratuito de despliegue dispone de muy poca memoria.
 */
@Component
public class GoogleCalendarAdapter implements ProveedorCalendarioPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarAdapter.class);

    private static final String URL_EVENTOS =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events";
    private static final String ZONA_HORARIA = "America/Bogota";
    private static final int SEMANAS_REPETICION = 16;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Dias tal como los guarda la base de datos (mayusculas, sin tildes). */
    private static final Map<String, DayOfWeek> DIAS = Map.of(
            "LUNES", DayOfWeek.MONDAY,
            "MARTES", DayOfWeek.TUESDAY,
            "MIERCOLES", DayOfWeek.WEDNESDAY,
            "JUEVES", DayOfWeek.THURSDAY,
            "VIERNES", DayOfWeek.FRIDAY,
            "SABADO", DayOfWeek.SATURDAY,
            "DOMINGO", DayOfWeek.SUNDAY);

    private final ObjectMapper objectMapper;
    private final HttpClient clienteHttp;

    public GoogleCalendarAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.clienteHttp = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public int crearEventos(String tokenAcceso, List<EventoCalendario> eventos) {
        int creados = 0;
        for (EventoCalendario evento : eventos) {
            if (crearEvento(tokenAcceso, evento)) {
                creados++;
            }
        }
        return creados;
    }

    /** @return true si el evento se creo en Google Calendar. */
    private boolean crearEvento(String tokenAcceso, EventoCalendario evento) {
        DayOfWeek dia = DIAS.get(evento.getDiaSemana() == null
                ? "" : evento.getDiaSemana().toUpperCase(Locale.ROOT));
        if (dia == null || evento.getHoraInicio() == null || evento.getHoraFin() == null) {
            log.warn("Se omitio un bloque con dia u horario invalido: {}", evento.getDiaSemana());
            return false;
        }

        LocalDate fecha = proximaFecha(dia);
        ObjectNode cuerpo = objectMapper.createObjectNode();
        cuerpo.put("summary", evento.getTitulo());
        if (evento.getDescripcion() != null && !evento.getDescripcion().isBlank()) {
            cuerpo.put("description", evento.getDescripcion());
        }
        if (evento.getUbicacion() != null && !evento.getUbicacion().isBlank()) {
            cuerpo.put("location", evento.getUbicacion());
        }

        ObjectNode inicio = cuerpo.putObject("start");
        inicio.put("dateTime", fecha + "T" + evento.getHoraInicio().format(FORMATO_HORA) + ":00");
        inicio.put("timeZone", ZONA_HORARIA);

        ObjectNode fin = cuerpo.putObject("end");
        fin.put("dateTime", fecha + "T" + evento.getHoraFin().format(FORMATO_HORA) + ":00");
        fin.put("timeZone", ZONA_HORARIA);

        ArrayNode repeticion = cuerpo.putArray("recurrence");
        repeticion.add("RRULE:FREQ=WEEKLY;COUNT=" + SEMANAS_REPETICION);

        enviarPeticion(tokenAcceso, cuerpo);
        return true;
    }

    /**
     * Devuelve la fecha (de hoy en adelante) que cae en el dia de la semana
     * indicado. Si hoy ya es ese dia, se usa hoy.
     */
    private LocalDate proximaFecha(DayOfWeek dia) {
        LocalDate hoy = LocalDate.now(ZoneId.of(ZONA_HORARIA));
        int diferencia = (dia.getValue() - hoy.getDayOfWeek().getValue() + 7) % 7;
        return hoy.plusDays(diferencia);
    }

    private void enviarPeticion(String tokenAcceso, ObjectNode cuerpo) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(URL_EVENTOS))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + tokenAcceso)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(cuerpo)))
                    .build();

            HttpResponse<String> respuesta = clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofString());
            int estado = respuesta.statusCode();

            if (estado == 401) {
                throw new RuntimeException("El permiso de Google Calendar expiro. Intentalo de nuevo.");
            }
            if (estado == 403) {
                throw new RuntimeException("Google denego el acceso al calendario. "
                        + "Verifica que la Calendar API este habilitada y que autorizaste el permiso.");
            }
            if (estado < 200 || estado >= 300) {
                throw new RuntimeException("Google Calendar respondio " + estado + ": " + mensajeDeError(respuesta.body()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Se interrumpio la comunicacion con Google Calendar");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("No se pudo crear el evento en Google Calendar", e);
            throw new RuntimeException("No fue posible agregar las clases al calendario");
        }
    }

    /** Extrae el mensaje de error de la respuesta de Google, si esta disponible. */
    private String mensajeDeError(String cuerpoRespuesta) {
        if (cuerpoRespuesta == null || cuerpoRespuesta.isBlank()) {
            return "sin detalle";
        }
        try {
            JsonNode error = objectMapper.readTree(cuerpoRespuesta).path("error");
            String mensaje = error.path("message").asText("");
            return mensaje.isBlank() ? "sin detalle" : mensaje;
        } catch (Exception e) {
            return "sin detalle";
        }
    }
}
