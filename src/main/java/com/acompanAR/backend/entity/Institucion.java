package com.acompanAR.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

// Institución (pública o privada) que publica capacitaciones.
// Hereda de Perfil: id, estado (baja lógica), usuario (si tiene cuenta propia) y persona (el responsable).
// Se ocultan usuario y persona en el JSON para no mostrar datos del responsable en los listados públicos.
@Entity
@Table(name = "instituciones")
@JsonIgnoreProperties({"usuario", "persona"})
public class Institucion extends Perfil {
    @Column(nullable = false, unique = true, length = 150)
    private String nombre;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInstitucion tipo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Localidad localidad;
    // Las instituciones cargadas por el administrador nacen VERIFICADAS; las que se registran solas, PENDIENTES.
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion", nullable = false, length = 20)
    @ColumnDefault("'VERIFICADA'")
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.VERIFICADA;

    @Override
    public Rol rolDelPerfil() {
        return Rol.INSTITUCION;
    }

    // ¿Puede publicar capacitaciones? Solo si está activa y el administrador la verificó
    public boolean puedePublicar() {
        return estaActivo() && estadoVerificacion == EstadoVerificacion.VERIFICADA;
    } //esto forma parte de una regla de negocio porque yo lo pongo como condicion

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

    public EstadoVerificacion getEstadoVerificacion() {
        return estadoVerificacion;
    }

    public void setEstadoVerificacion(EstadoVerificacion estadoVerificacion) {
        this.estadoVerificacion = estadoVerificacion;
    }

}
