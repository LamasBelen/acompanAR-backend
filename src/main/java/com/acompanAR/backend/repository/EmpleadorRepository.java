package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.Empleador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpleadorRepository extends JpaRepository<Empleador, Long> {
    // Busca el perfil de empleador que pertenece a un usuario (usuario.id)
    Optional<Empleador> findByUsuarioId(Long usuarioId);

    // Solo los empleadores que no fueron dados de baja
    java.util.List<Empleador> findByEstadoTrue();
}
