package com.scheduleu.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class HorarioGeneradoResponse {
    private Integer idEstudiante;
    private String nombreEstudiante;
    private String jornadaSeleccionada;
    private Integer totalSolicitadas;

    private List<OpcionHorario> opciones;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class OpcionHorario {
        private Integer indice;
        private Integer puntajeTotal;
        private Integer totalAsignadas;
        private Integer totalPendientes;
        private String descripcion;
        private List<FilaAsignacion> resultados;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class FilaAsignacion {
        private Integer idAsignacion;
        private Integer idAsignatura;
        private String nombreAsignatura;
        private Integer idGrupo;
        private String nombreGrupo;
        private String docente;
        private String salon;
        private String estado;
        private List<String> horarios;
        private String razonPendiente;
    }
}
