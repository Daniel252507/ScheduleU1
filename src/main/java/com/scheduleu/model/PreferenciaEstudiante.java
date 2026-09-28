package com.scheduleu.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "preferencias_estudiante")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PreferenciaEstudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_preferencia")
    private Long idPreferencia;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estudiante", nullable = false, unique = true)
    private Estudiante estudiante;

    @Column(name = "jornada_preferida", length = 20)
    @Enumerated(EnumType.STRING)
    private Jornada jornadaPreferida = Jornada.SIN_PREFERENCIA;

    @Column(name = "prioriza_docente")
    private Boolean priorizaDocente = false;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    public enum Jornada {
        MANANA, TARDE, NOCHE, SIN_PREFERENCIA
    }
}
