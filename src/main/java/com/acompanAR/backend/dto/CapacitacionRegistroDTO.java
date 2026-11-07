package com.acompanAR.backend.dto;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class CapacitacionRegistroDTO {
    @NotBlank (message = "El titulo de la capacitacion es obligatorio")
    @Size(max = 50, message = "El título no puede superar los 50 caracteres")
    private String titulo;
    @NotBlank (message = "La descripcion no puede estar vacia")
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;
    @NotNull(message = "La institución es obligatoria")
    private Long institucionId; // id de un registro de la tabla instituciones
    @NotNull(message = "La carga horaria no puede estar vacia")
    @Positive(message = "La carga horaria debe ser mayor a 0")
    private Integer cargaHoraria;

    // Opcionales
    @Size(max = 255, message = "El enlace no puede superar los 255 caracteres")
    private String linkVideo;
    private LocalDate fechaInicio; // formato JSON: "2026-12-01". Si no se manda, se usa hoy + 15 dias

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

    public Long getInstitucionId() {
        return institucionId;
    }

    public void setInstitucionId(Long institucionId) {
        this.institucionId = institucionId;
    }

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public String getLinkVideo() {
        return linkVideo;
    }

    public void setLinkVideo(String linkVideo) {
        this.linkVideo = linkVideo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
}
