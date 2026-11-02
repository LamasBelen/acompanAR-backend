package com.acompanAR.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "inscripciones")
public class Inscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaInscripcion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInscripcionCapacitacion estado = EstadoInscripcionCapacitacion.CONFIRMADA;

    // Relación con el Profesional
    @ManyToOne
    @JoinColumn(name = "fk_profesional")
    private Profesional profesional;

    // Relación con la Capacitación
    @ManyToOne
    @JoinColumn(name = "fk_capacitacion")
    private Capacitacion capacitacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }
    public EstadoInscripcionCapacitacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInscripcionCapacitacion estado) {
        this.estado = estado;
    }

    public Profesional getProfesional() {
        return profesional;
    }

    public void setProfesional(Profesional profesional) {
        this.profesional = profesional;
    }

    public Capacitacion getCapacitacion() {
        return capacitacion;
    }

    public void setCapacitacion(Capacitacion capacitacion) {
        this.capacitacion = capacitacion;
    }
}