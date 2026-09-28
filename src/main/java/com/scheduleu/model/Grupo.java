package com.scheduleu.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grupos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Integer idGrupo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_asignatura", nullable = false)
    private Asignatura asignatura;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_docente")
    private Docente docente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_salon")
    private Salon salon;

    @Column(nullable = false)
    private Integer cupo;

    @Column(length = 10)
    private String nombre;

    @Transient
    public Integer getCupoDisponible() {
        return null;
    }
}
