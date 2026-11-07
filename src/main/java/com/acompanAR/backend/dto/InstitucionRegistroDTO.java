package com.acompanAR.backend.dto;

import com.acompanAR.backend.entity.Localidad;
import com.acompanAR.backend.entity.TipoInstitucion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Datos para crear o editar una institución
public class InstitucionRegistroDTO {
    @NotBlank(message = "El nombre de la institución es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotNull(message = "El tipo de institución es obligatorio (PUBLICA o PRIVADA)")
    private TipoInstitucion tipo;

    @NotNull(message = "La localidad es obligatoria")
    private Localidad localidad;

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
}
