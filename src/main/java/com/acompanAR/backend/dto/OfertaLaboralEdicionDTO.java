package com.acompanAR.backend.dto;
import jakarta.validation.constraints.NotBlank;

// Datos que se pueden modificar de una oferta ya publicada.
// No incluye el empleador: una oferta no puede cambiar de dueño.
public class OfertaLaboralEdicionDTO {
    @NotBlank(message = "El titulo es obligatorio y no puede estar en blanco")
    private String titulo;
    @NotBlank(message = "La descripcion no puede estar vacia")
    private String descripcion;
    private String requisitos; // opcional

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

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }
}