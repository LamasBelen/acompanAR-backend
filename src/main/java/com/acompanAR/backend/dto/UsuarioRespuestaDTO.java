package com.acompanAR.backend.dto;

import com.acompanAR.backend.entity.Rol;

/**
 * Datos que se le devuelven al front después del login.
 * A propósito NO incluye la contraseña (ni siquiera hasheada).
 */
public class UsuarioRespuestaDTO {
    private Long id;
    private String nombreUsuario;
    private Rol rol;
    private String nombre;
    private String apellido;
    private Long idProfesional; // solo si el usuario es PROFESIONAL
    private Long idEmpleador;   // solo si el usuario es EMPLEADOR
    private Long idInstitucion; // solo si el usuario es INSTITUCION

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Long getIdProfesional() {
        return idProfesional;
    }

    public void setIdProfesional(Long idProfesional) {
        this.idProfesional = idProfesional;
    }

    public Long getIdEmpleador() {
        return idEmpleador;
    }

    public void setIdEmpleador(Long idEmpleador) {
        this.idEmpleador = idEmpleador;
    }

    public Long getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(Long idInstitucion) {
        this.idInstitucion = idInstitucion;
    }
}
