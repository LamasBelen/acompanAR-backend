package com.acompanAR.backend.repository;

import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoVerificacion;
import com.acompanAR.backend.entity.Institucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitucionRepository extends JpaRepository<Institucion, Long> {
    List<Institucion> findByEstado(Estado estado);

    // Las que se pueden elegir para publicar capacitaciones: activas y verificadas
    List<Institucion> findByEstadoAndEstadoVerificacion(Estado estado, EstadoVerificacion estadoVerificacion);

    boolean existsByNombreIgnoreCase(String nombre);

    // La institución que corresponde a un usuario (si el usuario es una institución)
    Optional<Institucion> findByUsuarioId(Long idUsuario);

    Optional<Institucion> findByUsuarioNombreUsuario(String nombreUsuario);
}
