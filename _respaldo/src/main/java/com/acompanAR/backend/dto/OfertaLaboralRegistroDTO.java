package com.acompanAR.backend.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import org.aspectj.bridge.IMessage;

public class OfertaLaboralRegistroDTO {
    @NotBlank(message = "El titulo es obligatorio y no puede estar en blanco")
    private String titulo;
    @NotBlank(message = "la descripcion no puede estar vacia")
    private String descripcion;
    private String requisitos; //DEJO QUE LOS REQUISITOS SEAN OPCIONALES

    //esto sirve para decirle al sistema que empleador es el que esta publicando la oferta
    //osea que lo identifique con su dni
    @NotNull(message = "el ID del empleador no puede ser nulo")
    private Long idEmpleador;

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

    public Long getIdEmpleador() {
        return idEmpleador;
    }

    public void setIdEmpleador(Long idEmpleador) {
        this.idEmpleador = idEmpleador;
    }
}
