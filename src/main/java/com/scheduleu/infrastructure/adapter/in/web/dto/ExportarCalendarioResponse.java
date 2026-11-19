package com.scheduleu.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Respuesta del endpoint de exportacion del horario a Google Calendar. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportarCalendarioResponse {

    /** Numero de eventos creados en el calendario del usuario. */
    private int eventosCreados;

    /** Mensaje legible para mostrar en la interfaz. */
    private String mensaje;
}
