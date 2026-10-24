package com.acompanAR.backend.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CapacitacionRegistroDTO {
    @NotBlank (message = "El nombre de la capacitacion es obligatorio")
    private String titulo;
    @NotBlank (message = "La descripcion no puede estar vacia")
    private String descripcion;
    @NotBlank (message = "Poner el nombre de la instucion es obligatorio")
    private String institucion;
    @NotNull(message = "La carga horaria no puede estar vacia ")
    private Integer CargaHoraria;

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

    public Integer getCargaHoraria() {
        return CargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        CargaHoraria = cargaHoraria;
    }
}
