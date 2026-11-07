package com.acompanAR.backend.entity;

import jakarta.persistence.*;

// Tabla propia con los tipos de profesional (Enfermero/a, Acompañante terapéutico, ...).
// Así el administrador puede agregar tipos nuevos sin tocar el código.
@Entity
@Table(name = "tipos_profesional")
public class TipoProfesional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    // Baja lógica
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
