package com.acompanAR.backend.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "profesionales")
public class Profesional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    @Column(length = 20)
    private String matricula;

    // Baja lógica: un profesional dado de baja queda INACTIVO
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVO;

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

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
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
