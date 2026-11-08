package com.acompanAR.backend.dto;

import com.acompanAR.backend.entity.MotivoDenuncia;
import com.acompanAR.backend.entity.ObjetoDenunciado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DenunciaRegistroDTO {
    @NotNull(message = "Tenés que indicar qué querés denunciar")
    private ObjetoDenunciado objeto;
    @NotNull(message = "Falta el identificador de lo denunciado")
    private Long idObjeto;
    @NotNull(message = "Elegí el motivo de la denuncia")
    private MotivoDenuncia motivo;
    @NotBlank(message = "Contanos brevemente qué pasó")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    public ObjetoDenunciado getObjeto() {
        return objeto;
    }

    public void setObjeto(ObjetoDenunciado objeto) {
        this.objeto = objeto;
    }

    public Long getIdObjeto() {
        return idObjeto;
    }

    public void setIdObjeto(Long idObjeto) {
        this.idObjeto = idObjeto;
    }

    public MotivoDenuncia getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoDenuncia motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
