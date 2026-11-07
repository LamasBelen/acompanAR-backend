package com.acompanAR.backend.dto;

import jakarta.validation.constraints.NotNull;

public class PostulacionRegistroDTO {
    @NotNull(message = "El ID del profesional es obligatorio")
    private Long idProfesional;
    @NotNull (message = "El ID de la oferta laboral es obligatorio")
    private Long idOfertaLaboral;

    public Long getIdProfesional() {
        return idProfesional;
    }

    public void setIdProfesional(Long idProfesional) {
        this.idProfesional = idProfesional;
    }

    public Long getIdOfertaLaboral() {
        return idOfertaLaboral;
    }

    public void setIdOfertaLaboral(Long idOfertaLaboral) {
        this.idOfertaLaboral = idOfertaLaboral;
    }
}
