package com.acompanAR.backend.dto;

import java.util.List;

// Reputación de un usuario: promedio, cantidad y detalle de lo que recibió
public class ReputacionDTO {
    private Double promedio; // null si todavía no tiene calificaciones
    private int cantidad;
    private List<CalificacionRespuestaDTO> calificaciones;

    public Double getPromedio() {
        return promedio;
    }

    public void setPromedio(Double promedio) {
        this.promedio = promedio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public List<CalificacionRespuestaDTO> getCalificaciones() {
        return calificaciones;
    }

    public void setCalificaciones(List<CalificacionRespuestaDTO> calificaciones) {
        this.calificaciones = calificaciones;
    }
}
