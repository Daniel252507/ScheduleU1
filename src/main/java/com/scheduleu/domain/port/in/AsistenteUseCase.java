package com.scheduleu.domain.port.in;

import com.scheduleu.infrastructure.adapter.in.web.dto.AsistenteRespuesta;

/**
 * Caso de uso: consultar al asistente academico con IA sobre el horario del
 * estudiante.
 */
public interface AsistenteUseCase {

    /**
     * Responde una pregunta del estudiante usando su horario confirmado como
     * contexto.
     *
     * @param correo   correo del usuario autenticado (extraido del JWT).
     * @param pregunta pregunta en lenguaje natural.
     * @return respuesta generada por el modelo.
     */
    AsistenteRespuesta consultar(String correo, String pregunta);
}
