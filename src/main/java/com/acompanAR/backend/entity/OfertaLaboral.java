package com.acompanAR.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table (name = "ofertas_laborales")
public class OfertaLaboral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    @Column(length = 1000)
    private String descripcion;
    private String requisitos;
    private LocalDate fechaPublicacion;
    // Baja lógica: una oferta dada de baja queda INACTIVA y deja de mostrarse.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVO;

    //la relacion que tiene es que muchas ofertas pueden pertenecer a un mismo empleador
    @ManyToOne
    @JoinColumn(name = "fk_empleador")
    private Empleador empleador;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Empleador getEmpleador() {
        return empleador;
    }

    public void setEmpleador(Empleador empleador) {
        this.empleador = empleador;
    }
}


