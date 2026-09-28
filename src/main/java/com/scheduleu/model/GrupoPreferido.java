package com.scheduleu.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grupo_preferido")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GrupoPreferido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_preferencia", nullable = false)
    private PreferenciaEstudiante preferencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_asignatura", nullable = false)
    private Asignatura asignatura;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_grupo")
    private Grupo grupo;

    @Column(name = "prioridad")
    private Integer prioridad = 1;
}
