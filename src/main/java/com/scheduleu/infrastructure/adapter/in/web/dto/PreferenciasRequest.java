package com.scheduleu.infrastructure.adapter.in.web.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PreferenciasRequest {
    private Long idEstudiante;
    private String jornadaPreferida;
    private Boolean priorizaDocente;
    private String observaciones;
    private List<MateriaSeleccionada> materias;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class MateriaSeleccionada {
        private Long idAsignatura;
        private Long idGrupoPreferido;
        private Integer prioridad;
    }
}
