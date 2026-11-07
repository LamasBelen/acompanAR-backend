package com.acompanAR.backend.entity;

import jakarta.persistence.*;

// Institución (pública o privada) que publica capacitaciones.
// Una institución puede tener muchas capacitaciones (relación @ManyToOne desde Capacitacion).
@Entity
@Table(name = "instituciones")
public class Institucion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInstitucion tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Localidad localidad;

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

    public TipoInstitucion getTipo() {
        return tipo;
    }

    public void setTipo(TipoInstitucion tipo) {
        this.tipo = tipo;
    }

    public Localidad getLocalidad() {
        return localidad;
    }

    public void setLocalidad(Localidad localidad) {
        this.localidad = localidad;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
