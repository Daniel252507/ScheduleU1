package com.scheduleu.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ConfirmarHorarioRequest {
    private Long idEstudiante;
    private Integer indiceOpcion;
}
