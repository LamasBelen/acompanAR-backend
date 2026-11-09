package com.acompanAR.backend.dto;

import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoVerificacion;
import com.acompanAR.backend.entity.Localidad;
import com.acompanAR.backend.entity.TipoInstitucion;

// Lo que ve el administrador de cada institución (incluye los datos del responsable, que el público no ve).
public class InstitucionAdminDTO {
    private Long id;
    private String nombre;
    private TipoInstitucion tipo;
    private Localidad localidad;
    private Estado estado;
    private EstadoVerificacion estadoVerificacion;
    private boolean tieneCuenta;
    private String nombreUsuario;
    private String responsable;
    private String telefono;
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoInstitucion getTipo() {
        return tipo;
    }

    public void setTipo(TipoInstitucion tipo) {
        this.tipo = tipo;
    }

    public Localidad getLocalidad() {
        return localidad;
    }

    public void setLocalidad(Localidad localidad) {
        this.localidad = localidad;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public EstadoVerificacion getEstadoVerificacion() {
        return estadoVerificacion;
    }

    public void setEstadoVerificacion(EstadoVerificacion estadoVerificacion) {
        this.estadoVerificacion = estadoVerificacion;
    }

    public boolean isTieneCuenta() {
        return tieneCuenta;
    }

    public void setTieneCuenta(boolean tieneCuenta) {
        this.tieneCuenta = tieneCuenta;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

}
