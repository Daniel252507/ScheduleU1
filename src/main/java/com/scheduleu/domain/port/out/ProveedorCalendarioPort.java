package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.EventoCalendario;

import java.util.List;

/**
 * Puerto de salida hacia un calendario externo.
 *
 * La implementacion concreta decide el proveedor (por ejemplo Google Calendar)
 * y el protocolo de comunicacion. La capa de aplicacion solo conoce este
 * contrato.
 */
public interface ProveedorCalendarioPort {

    /**
     * Crea en el calendario del usuario los eventos indicados.
     *
     * @param tokenAcceso token OAuth con permiso de escritura sobre el calendario.
     * @param eventos     clases/eventos a crear.
     * @return numero de eventos creados correctamente.
     */
    int crearEventos(String tokenAcceso, List<EventoCalendario> eventos);
}
