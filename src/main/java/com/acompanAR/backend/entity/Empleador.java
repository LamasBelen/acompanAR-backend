package com.acompanAR.backend.entity;

import jakarta.persistence.*;

// Hereda de Perfil: id, estado, usuario y persona. Acá solo van los datos propios del empleador.
@Entity
@Table(name = "empleadores")
public class Empleador extends Perfil {

    @Column(name = "nombre_empresa")
    private String nombreEmpresa;
    @Column(name = "decripcion_puesto")
    private String descripcionPuesto;

    @Override
    public Rol rolDelPerfil() {
        return Rol.EMPLEADOR;
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
}
