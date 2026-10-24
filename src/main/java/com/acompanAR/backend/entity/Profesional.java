package com.acompanAR.backend.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "profesionales")
public class Profesional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column (name = "tipo_profesional")
    private String tipoProfesional;
    @Column(length = 1500)
    private String experiencia;
    private String disponibilidad;
    private String matricula;

    private boolean estado;

    //la relacion que tiene con fk_usuario
    @OneToOne
    @JoinColumn(name = "fk_usuario")
    private Usuario usuario;
    @OneToOne
    @JoinColumn(name = "persona_id")
    private Persona persona;


    public Long getId() {
        return id;

    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoProfesional() {
        return tipoProfesional;
    }

    public void setTipoProfesional(String tipoProfesional) {
        this.tipoProfesional = tipoProfesional;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Persona getPersona() {
        return persona;
    }
    public void setPersona(Persona persona){
        this.persona = persona;
    }

}
