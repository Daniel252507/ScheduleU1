package com.scheduleu.infrastructure.adapter.in.web.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ConfirmarHorarioRequest {
    private Long idEstudiante;
    private Integer indiceOpcion;
}
