package com.acompanAR.backend.entity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "capacitaciones")
public class Capacitacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    // La institución es otra entidad (tabla "instituciones"): muchas capacitaciones pueden ser de la misma institución
    @ManyToOne
    @JoinColumn(name = "fk_institucion", nullable = false)
    private Institucion institucion;
    private String linkVideo;
    private int cargaHoraria;
    private LocalDate fechaInicio;

    // Baja lógica: en vez de borrar la capacitación, se marca como INACTIVA.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Institucion getInstitucion() {
        return institucion;
    }

    public void setInstitucion(Institucion institucion) {
        this.institucion = institucion;
    }

    public String getLinkVideo() {
        return linkVideo;
    }

    public void setLinkVideo(String linkVideo) {
        this.linkVideo = linkVideo;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(int cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
