package com.acompanAR.backend.entity;

// Localidades permitidas. En la base se guarda el nombre de la constante (ej: RIO_GRANDE).
public enum Localidad {
    RIO_GRANDE("Río Grande"),
    USHUAIA("Ushuaia"),
    TOLHUIN("Tolhuin");

    private final String nombre; // texto para mostrar en pantalla

    Localidad(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
