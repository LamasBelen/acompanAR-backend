package com.acompanAR.backend.repository;

import com.acompanAR.backend.entity.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {
    // Busca el perfil profesional que pertenece a un usuario (usuario.id)
    Optional<Profesional> findByUsuarioId(Long usuarioId);

    // Solo los profesionales que no fueron dados de baja
    java.util.List<Profesional> findByEstadoTrue();
}