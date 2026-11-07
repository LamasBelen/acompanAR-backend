package com.acompanAR.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Datos para crear o editar un tipo de profesional
public class TipoProfesionalDTO {
    @NotBlank(message = "El nombre del tipo de profesional es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
