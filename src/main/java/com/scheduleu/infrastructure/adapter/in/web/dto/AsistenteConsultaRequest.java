package com.scheduleu.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cuerpo del endpoint del asistente: la pregunta del estudiante. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsistenteConsultaRequest {

    /** Pregunta en lenguaje natural. */
    private String pregunta;
}
