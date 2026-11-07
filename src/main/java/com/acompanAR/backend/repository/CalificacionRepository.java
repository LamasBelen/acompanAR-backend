package com.acompanAR.backend.repository;

import com.acompanAR.backend.entity.Calificacion;
import com.acompanAR.backend.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    // Para impedir que alguien califique dos veces la misma postulación
    boolean existsByPostulacionIdAndAutorId(Long idPostulacion, Long idAutor);

    // Calificaciones que recibió un usuario (para su reputación)
    List<Calificacion> findByDestinatarioIdAndEstado(Long idDestinatario, Estado estado);

    // Calificaciones que hizo un usuario (para saber qué postulaciones ya calificó)
    List<Calificacion> findByAutorIdAndEstado(Long idAutor, Estado estado);
}
