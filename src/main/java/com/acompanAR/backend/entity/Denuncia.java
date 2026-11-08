package com.acompanAR.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

// Denuncia de un usuario sobre algo de la plataforma (estafa, contenido inapropiado, etc.).
// La atiende el administrador: puede dar de baja lo denunciado o descartar la denuncia.
@Entity
@Table(name = "denuncias")
public class Denuncia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Quién denuncia
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_denunciante")
    private Usuario denunciante;
    // Qué se denuncia: un tipo (perfil, oferta, capacitación...) y el id de ese registro
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObjetoDenunciado objeto;
    @Column(name = "id_objeto", nullable = false)
    private Long idObjeto;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoDenuncia motivo;
    @Column(nullable = false, length = 500)
    private String descripcion;
    private LocalDate fecha;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDenuncia estado = EstadoDenuncia.PENDIENTE;
    // Lo que respondió el administrador al resolverla
    @Column(length = 300)
    private String resolucion;
    private LocalDate fechaResolucion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getDenunciante() {
        return denunciante;
    }

    public void setDenunciante(Usuario denunciante) {
        this.denunciante = denunciante;
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

    public LocalDate getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDate fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

}
