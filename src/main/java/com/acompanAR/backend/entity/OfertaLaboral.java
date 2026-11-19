package com.acompanAR.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
@Entity
@Table (name = "ofertas_laborales")
public class OfertaLaboral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    @Column(length = 1000)
    private String descripcion;
    // Otros requisitos, en texto libre (los más comunes se eligen del catálogo, ver requisitosComunes)
    private String requisitos;
    private LocalDate fechaPublicacion;

    // Cuándo empieza el trabajo y cuántas semanas dura. Con eso se calcula el progreso (son opcionales).
    private LocalDate fechaInicio;
    private Integer duracionSemanas;

    // Requisitos comunes elegidos del catálogo (relación muchos a muchos, tabla intermedia ofertas_requisitos)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "ofertas_requisitos",
            joinColumns = @JoinColumn(name = "fk_oferta"),
            inverseJoinColumns = @JoinColumn(name = "fk_requisito"))
    private Set<Requisito> requisitosComunes = new LinkedHashSet<>();

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

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Integer getDuracionSemanas() {
        return duracionSemanas;
    }

    public void setDuracionSemanas(Integer duracionSemanas) {
        this.duracionSemanas = duracionSemanas;
    }

    public Set<Requisito> getRequisitosComunes() {
        return requisitosComunes;
    }

    public void setRequisitosComunes(Set<Requisito> requisitosComunes) {
        this.requisitosComunes = requisitosComunes;
    }
}
