package com.acompanAR.backend.dto;

import com.acompanAR.backend.entity.EstadoDenuncia;
import com.acompanAR.backend.entity.MotivoDenuncia;
import com.acompanAR.backend.entity.ObjetoDenunciado;
import java.time.LocalDate;

// Lo que ve el administrador de cada denuncia.
public class DenunciaRespuestaDTO {
    private Long id;
    private ObjetoDenunciado objeto;
    private Long idObjeto;
    private String descripcionObjeto;
    private boolean objetoActivo;
    private MotivoDenuncia motivo;
    private String descripcion;
    private String nombreDenunciante;
    private LocalDate fecha;
    private EstadoDenuncia estado;
    private String resolucion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getDescripcionObjeto() {
        return descripcionObjeto;
    }

    public void setDescripcionObjeto(String descripcionObjeto) {
        this.descripcionObjeto = descripcionObjeto;
    }

    public boolean isObjetoActivo() {
        return objetoActivo;
    }

    public void setObjetoActivo(boolean objetoActivo) {
        this.objetoActivo = objetoActivo;
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

    public String getNombreDenunciante() {
        return nombreDenunciante;
    }

    public void setNombreDenunciante(String nombreDenunciante) {
        this.nombreDenunciante = nombreDenunciante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public EstadoDenuncia getEstado() {
        return estado;
    }

    public void setEstado(EstadoDenuncia estado) {
        this.estado = estado;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

}
