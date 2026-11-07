package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    // Para que el empleador vea quiénes se postularon a una oferta
    List<Postulacion> findByOfertaLaboralId(Long idOferta);

    // Para que el profesional vea sus propias postulaciones
    List<Postulacion> findByProfesionalId(Long idProfesional);

    // Para evitar que el mismo profesional se postule dos veces a la misma oferta
    boolean existsByProfesionalIdAndOfertaLaboralId(Long idProfesional, Long idOferta);
}