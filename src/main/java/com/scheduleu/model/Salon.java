package com.scheduleu.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Salon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salon")
    private Integer idSalon;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(length = 50)
    private String ubicacion;
}
