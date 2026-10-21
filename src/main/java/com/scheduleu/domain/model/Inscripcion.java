package com.scheduleu.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inscripciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Integer idInscripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_asignatura", nullable = false)
    private Asignatura asignatura;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EstadoInscripcion estado;

    public enum EstadoInscripcion {
        INSCRITO,
        PENDIENTE,
        CANCELADO,
        COMPLETADO
    }
}
