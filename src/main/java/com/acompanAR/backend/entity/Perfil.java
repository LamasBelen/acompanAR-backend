package com.acompanAR.backend.entity;

import jakarta.persistence.*;

/**
 * Clase abstracta con lo que tienen en común todos los perfiles del sistema
 * (hoy Profesional y Empleador): un id, un estado para la baja lógica, el usuario con el
 * que ingresan y los datos de la persona.
 *
 * Es una @MappedSuperclass: no tiene tabla propia; sus campos se guardan en la tabla de
 * cada clase hija. Así evitamos repetir código (herencia) y las hijas solo agregan lo suyo.
 */
@MappedSuperclass
public abstract class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Baja lógica: un perfil dado de baja queda INACTIVO (no se borra)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVO;

    @OneToOne
    @JoinColumn(name = "fk_usuario")
    private Usuario usuario;

    @OneToOne
    @JoinColumn(name = "persona_id")
    private Persona persona;

    // Polimorfismo: cada tipo de perfil dice qué rol le corresponde
    public abstract Rol rolDelPerfil();

    // Comportamiento común: la baja lógica del perfil y de su usuario (ya no podrá ingresar)
    public void darDeBaja() {
        this.estado = Estado.INACTIVO;
        if (usuario != null) {
            usuario.setEstado(Estado.INACTIVO);
        }
    }

    public boolean estaActivo() {
        return estado == Estado.ACTIVO;
    }

    public String nombreCompleto() {
        return persona == null ? "" : persona.getNombre() + " " + persona.getApellido();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setPersona(Persona persona) {
        this.persona = persona;
    }
}
