package com.scheduleu.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Respuesta del asistente academico. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsistenteRespuesta {

    /** Texto generado por el modelo. */
    private String respuesta;
}
