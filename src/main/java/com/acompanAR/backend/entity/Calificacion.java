package com.acompanAR.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Calificación mutua: cuando una postulación fue ACEPTADA, el profesional y el empleador
 * pueden puntuarse entre sí (una sola vez cada uno por postulación).
 * La restricción única (postulación + autor) lo garantiza también a nivel base de datos.
 */
@Entity
@Table(name = "calificaciones",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fk_postulacion", "fk_autor"}))
public class Calificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // La postulación aceptada que da origen a la calificación
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_postulacion")
    private Postulacion postulacion;

    // Quien califica
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_autor")
    private Usuario autor;

    // Quien recibe la calificación
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_destinatario")
    private Usuario destinatario;

    // De 1 a 5
    @Column(nullable = false)
    private Integer puntaje;

    @Column(length = 500)
    private String comentario;

    private LocalDate fecha;

    // Baja lógica: un administrador puede ocultar una calificación inapropiada sin borrarla
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Postulacion getPostulacion() {
        return postulacion;
    }

    public void setPostulacion(Postulacion postulacion) {
        this.postulacion = postulacion;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public Integer getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Integer puntaje) {
        this.puntaje = puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
