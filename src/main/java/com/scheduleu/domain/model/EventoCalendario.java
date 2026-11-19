package com.scheduleu.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Clase del horario de un estudiante lista para publicarse como evento en un
 * calendario externo.
 *
 * Es un modelo de dominio: no conoce ningun proveedor concreto (Google, etc.).
 * El adaptador de salida se encarga de traducirlo al formato que cada
 * proveedor espere.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoCalendario {

    /** Titulo visible del evento (normalmente el nombre de la asignatura). */
    private String titulo;

    /** Detalle: grupo, docente, etc. */
    private String descripcion;

    /** Salon o aula donde se dicta la clase. */
    private String ubicacion;

    /** Dia de la semana en mayusculas y sin tildes: LUNES, MARTES, ... */
    private String diaSemana;

    private LocalTime horaInicio;

    private LocalTime horaFin;
}
