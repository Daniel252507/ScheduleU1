package com.scheduleu.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asignaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Integer idAsignacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_inscripcion")
    private Inscripcion inscripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_grupo", nullable = true)
    private Grupo grupo;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EstadoAsignacion estado;

    public enum EstadoAsignacion {
        ASIGNADO,
        PENDIENTE,
        CONFLICTO
    }
}
