package com.scheduleu.domain.port.in;

import com.scheduleu.infrastructure.adapter.in.web.dto.ExportarCalendarioResponse;

/**
 * Caso de uso: exportar el horario confirmado del estudiante a su calendario
 * externo de Google.
 */
public interface ExportarHorarioCalendarioUseCase {

    /**
     * Agrega las clases del horario confirmado del estudiante a su Google
     * Calendar.
     *
     * @param correo      correo del usuario autenticado (extraido del JWT).
     * @param tokenAcceso token OAuth con permiso {@code calendar.events}.
     * @return resumen con el numero de eventos creados.
     */
    ExportarCalendarioResponse exportar(String correo, String tokenAcceso);
}
