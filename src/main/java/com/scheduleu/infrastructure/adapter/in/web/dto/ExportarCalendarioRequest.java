package com.scheduleu.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuerpo del endpoint de exportacion. Solo trae el token de acceso de Google;
 * el horario se obtiene en el servidor a partir del usuario autenticado, de
 * modo que el cliente no puede inventar clases.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportarCalendarioRequest {

    /** Token OAuth obtenido en el navegador con el scope {@code calendar.events}. */
    private String accessToken;
}
