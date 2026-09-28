package com.scheduleu.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asignaturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignatura")
    private Integer idAsignatura;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private Integer creditos;

    @Column(length = 20)
    private String codigo;

    @Column(name = "semestre_recomendado")
    private Integer semestreRecomendado;
}
