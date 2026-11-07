package com.acompanAR.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CalificacionRegistroDTO {
    @NotNull(message = "La postulación es obligatoria")
    private Long idPostulacion;

    @NotNull(message = "El puntaje es obligatorio")
    @Min(value = 1, message = "El puntaje mínimo es 1")
    @Max(value = 5, message = "El puntaje máximo es 5")
    private Integer puntaje;

    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
    private String comentario;

    public Long getIdPostulacion() {
        return idPostulacion;
    }

    public void setIdPostulacion(Long idPostulacion) {
        this.idPostulacion = idPostulacion;
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
}
