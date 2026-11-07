package com.acompanAR.backend.dto;

import java.time.LocalDate;

// Lo que se muestra de una calificación (sin exponer datos de más del autor)
public class CalificacionRespuestaDTO {
    private Long id;
    private Integer puntaje;
    private String comentario;
    private LocalDate fecha;
    private String nombreAutor;
    private String tituloOferta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Integer puntaje) {
        this.puntaje = puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNombreAutor() {
        return nombreAutor;
    }

    public void setNombreAutor(String nombreAutor) {
        this.nombreAutor = nombreAutor;
    }

    public String getTituloOferta() {
        return tituloOferta;
    }

    public void setTituloOferta(String tituloOferta) {
        this.tituloOferta = tituloOferta;
    }
}
