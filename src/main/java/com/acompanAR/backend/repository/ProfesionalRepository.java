package com.acompanAR.backend.repository;

import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {
    // Busca el perfil profesional que pertenece a un usuario (usuario.id)
    Optional<Profesional> findByUsuarioId(Long usuarioId);

    // Para que dos profesionales no tengan la misma matrícula (no distingue mayúsculas)
    boolean existsByMatriculaIgnoreCase(String matricula);

    // Igual, pero ignorando al propio profesional (se usa al editar el perfil)
    boolean existsByMatriculaIgnoreCaseAndIdNot(String matricula, Long id);

    // Solo los profesionales que no fueron dados de baja
    java.util.List<Profesional> findByEstado(Estado estado);
}
