package com.acompanAR.backend.entity;
import jakarta.persistence.*;
@Entity
@Table (name = "empleadores")

public class Empleador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column (name = "nombre_empresa")
    private String nombreEmpresa;
    @Column( name = "decripcion_puesto")
    private String descripcionPuesto;

    // Baja lógica: en vez de borrar al empleador, se marca como INACTIVO.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVO;
    //la relacion que tiene el empleador con fk_persona
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

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getDescripcionPuesto() {
        return descripcionPuesto;
    }

    public void setDescripcionPuesto(String descripcionPuesto) {
        this.descripcionPuesto = descripcionPuesto;
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
        this.persona =persona;
    }
}
