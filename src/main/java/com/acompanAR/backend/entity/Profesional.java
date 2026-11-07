package com.acompanAR.backend.entity;

import jakarta.persistence.*;

// Hereda de Perfil: id, estado, usuario y persona. Acá solo van los datos propios del profesional.
@Entity
@Table(name = "profesionales")
public class Profesional extends Perfil {

    // El tipo de profesional está en su propia tabla (tipos_profesional)
    @ManyToOne
    @JoinColumn(name = "fk_tipo_profesional", nullable = false)
    private TipoProfesional tipoProfesional;

    // Experiencia: un número (años) y, aparte, un texto que la describe
    @Column(name = "anios_experiencia", nullable = false)
    private Integer aniosExperiencia = 0;
    @Column(name = "descripcion_experiencia", length = 1500)
    private String descripcionExperiencia;

    private String disponibilidad;

    // La matrícula tiene como máximo 20 caracteres (también se valida en los DTOs)
    // Es única: dos profesionales no pueden tener el mismo número (puede quedar vacía/null)
    @Column(length = 20, unique = true)
    private String matricula;

    @Override
    public Rol rolDelPerfil() {
        return Rol.PROFESIONAL;
    }

    public TipoProfesional getTipoProfesional() {
        return tipoProfesional;
    }

    public void setTipoProfesional(TipoProfesional tipoProfesional) {
        this.tipoProfesional = tipoProfesional;
    }

    public Integer getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(Integer aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }

    public String getDescripcionExperiencia() {
        return descripcionExperiencia;
    }

    public void setDescripcionExperiencia(String descripcionExperiencia) {
        this.descripcionExperiencia = descripcionExperiencia;
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
}
