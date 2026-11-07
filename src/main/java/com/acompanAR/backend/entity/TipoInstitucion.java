package com.acompanAR.backend.entity;

// Una institución que publica capacitaciones puede ser del Estado o privada.
public enum TipoInstitucion {
    PUBLICA("Pública"),
    PRIVADA("Privada");

    private final String nombre;

    TipoInstitucion(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
