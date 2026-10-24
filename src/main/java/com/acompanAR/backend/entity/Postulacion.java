package com.acompanAR.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name = "postulaciones")
public class Postulacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaPostulacion;
    private boolean estado; //para que diga si esta pendiente, aceptada, o rechazada
    //la recion con el profesional que se va a postular
    @ManyToOne
    @JoinColumn(name = "fk_profesional")
    private  Profesional profesional;

    //la relacion que va a tener el profesional con la oferta a la que se postula
    @ManyToOne
    @JoinColumn(name = "fk_oferta_laboral")
    private OfertaLaboral ofertaLaboral;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void setFechaPostulacion(LocalDate fechaPostulacion) {
        this.fechaPostulacion = fechaPostulacion;
    }

    public boolean getEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public Profesional getProfesional() {
        return profesional;
    }

    public void setProfesional(Profesional profesional) {
        this.profesional = profesional;
    }

    public OfertaLaboral getOfertaLaboral() {
        return ofertaLaboral;
    }

    public void setOfertaLaboral(OfertaLaboral ofertaLaboral) {
        this.ofertaLaboral = ofertaLaboral;
    }
}
