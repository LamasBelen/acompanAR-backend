package com.acompanAR.backend.dto;
import jakarta.validation.constraints.NotNull;

public class InscripcionRegistroDTO {
    @NotNull(message = "El ID del profesional es obligatorio")
    private Long idProfesional;
    @NotNull(message = "El ID de la capacitación es obligatorio")
    private Long idCapacitacion;

    public Long getIdProfesional() {
        return idProfesional;
    }

    public void setIdProfesional(Long idProfesional) {
        this.idProfesional = idProfesional;
    }

    public Long getIdCapacitacion() {
        return idCapacitacion;
    }

    public void setIdCapacitacion(Long idCapacitacion) {
        this.idCapacitacion = idCapacitacion;
    }
}
