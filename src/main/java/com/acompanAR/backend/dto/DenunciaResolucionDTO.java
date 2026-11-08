package com.acompanAR.backend.dto;

import com.acompanAR.backend.entity.AccionDenuncia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DenunciaResolucionDTO {
    @NotNull(message = "Elegí qué hacer con la denuncia")
    private AccionDenuncia accion;
    @Size(max = 300, message = "La nota no puede superar los 300 caracteres")
    private String nota;

    public AccionDenuncia getAccion() {
        return accion;
    }

    public void setAccion(AccionDenuncia accion) {
        this.accion = accion;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

}
