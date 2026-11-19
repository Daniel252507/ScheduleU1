package com.scheduleu.application.service;

import com.scheduleu.domain.model.EventoCalendario;
import com.scheduleu.domain.port.in.EstudianteHorarioUseCase;
import com.scheduleu.domain.port.in.ExportarHorarioCalendarioUseCase;
import com.scheduleu.domain.port.out.ProveedorCalendarioPort;
import com.scheduleu.infrastructure.adapter.in.web.dto.ExportarCalendarioResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.HorarioGeneradoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de aplicacion del caso de uso "Añadir a mi calendario".
 *
 * Orquesta el flujo: obtiene el horario confirmado del estudiante (reutilizando
 * {@link EstudianteHorarioUseCase}), traduce cada bloque a un
 * {@link EventoCalendario} de dominio y delega la escritura al puerto de salida
 * {@link ProveedorCalendarioPort}.
 */
@Service
@RequiredArgsConstructor
public class CalendarioService implements ExportarHorarioCalendarioUseCase {

    private final EstudianteHorarioUseCase estudianteHorario;
    private final ProveedorCalendarioPort proveedorCalendario;

    @Override
    public ExportarCalendarioResponse exportar(String correo, String tokenAcceso) {
        if (tokenAcceso == null || tokenAcceso.isBlank()) {
            throw new RuntimeException("No se recibio el permiso de Google Calendar");
        }

        List<EventoCalendario> eventos = construirEventos(correo);
        if (eventos.isEmpty()) {
            throw new RuntimeException("No tienes clases asignadas para agregar al calendario");
        }

        int creados = proveedorCalendario.crearEventos(tokenAcceso, eventos);

        return new ExportarCalendarioResponse(creados,
                creados + " clase(s) agregada(s) a tu Google Calendar");
    }

    /**
     * Recorre las asignaciones confirmadas del estudiante y convierte cada
     * bloque horario en un evento de dominio.
     */
    private List<EventoCalendario> construirEventos(String correo) {
        List<HorarioGeneradoResponse.FilaAsignacion> asignaciones =
                estudianteHorario.listarMisAsignaciones(correo);

        List<EventoCalendario> eventos = new ArrayList<>();
        for (HorarioGeneradoResponse.FilaAsignacion asignacion : asignaciones) {
            if (!"ASIGNADO".equals(asignacion.getEstado())) {
                continue;
            }
            for (String bloque : asignacion.getHorarios()) {
                EventoCalendario evento = construirEvento(asignacion, bloque);
                if (evento != null) {
                    eventos.add(evento);
                }
            }
        }
        return eventos;
    }

    /**
     * Convierte un bloque textual del horario (por ejemplo "LUNES 08:00-10:00")
     * en un evento. El formato lo genera {@link EstudianteHorarioService}, por
     * lo que siempre es "DIA HH:MM-HH:MM".
     */
    private EventoCalendario construirEvento(HorarioGeneradoResponse.FilaAsignacion asignacion, String bloque) {
        if (bloque == null || bloque.isBlank()) {
            return null;
        }
        try {
            String[] partes = bloque.trim().split("\\s+");
            if (partes.length < 2) {
                return null;
            }
            String dia = partes[0].toUpperCase(Locale.ROOT);
            String[] rango = partes[1].split("-");
            if (rango.length != 2) {
                return null;
            }

            String titulo = asignacion.getNombreAsignatura() == null
                    ? "Clase" : asignacion.getNombreAsignatura();

            List<String> detalle = new ArrayList<>();
            if (asignacion.getNombreGrupo() != null && !asignacion.getNombreGrupo().isBlank()) {
                detalle.add("Grupo: " + asignacion.getNombreGrupo());
            }
            if (asignacion.getDocente() != null && !asignacion.getDocente().isBlank()) {
                detalle.add("Docente: " + asignacion.getDocente());
            }
            detalle.add("Agendado desde ScheduleU");

            return new EventoCalendario(
                    titulo,
                    String.join("\n", detalle),
                    asignacion.getSalon() == null ? "" : asignacion.getSalon(),
                    dia,
                    LocalTime.parse(rango[0].trim()),
                    LocalTime.parse(rango[1].trim()));
        } catch (RuntimeException e) {
            // Un bloque con formato inesperado no debe impedir exportar el resto.
            return null;
        }
    }
}
