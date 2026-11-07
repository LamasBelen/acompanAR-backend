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
    private String institucion;
    private String linkVideo;
    private int cargaHoraria;
    private LocalDate fechaInicio;

    // Baja lógica: en vez de borrar la capacitación, se marca como inactiva.
    // El "default true" permite que Hibernate agregue la columna aunque ya existan filas en la tabla.
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean activa = true;

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

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
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

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}