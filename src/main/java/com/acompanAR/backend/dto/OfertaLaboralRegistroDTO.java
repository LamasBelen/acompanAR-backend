package com.acompanAR.backend.dto;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;



public class OfertaLaboralRegistroDTO {
    @NotBlank(message = "El titulo es obligatorio y no puede estar en blanco")
    @Size(max = 255, message = "El título no puede superar los 255 caracteres")
    private String titulo;
    @NotBlank(message = "la descripcion no puede estar vacia")
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;
    @Size(max = 255, message = "Los requisitos no pueden superar los 255 caracteres")
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
