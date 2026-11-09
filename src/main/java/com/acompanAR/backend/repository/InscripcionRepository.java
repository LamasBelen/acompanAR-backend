package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.EstadoInscripcionCapacitacion;
import com.acompanAR.backend.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscripcionRepository extends JpaRepository <Inscripcion,Long> {
    // Para que el profesional vea sus inscripciones
    List<Inscripcion> findByProfesionalId(Long idProfesional);

    // Para que la institución vea quiénes se inscribieron a una de sus capacitaciones
    List<Inscripcion> findByCapacitacionId(Long idCapacitacion);

    // Para evitar que se inscriba dos veces a la misma capacitación mientras sigue vigente
    boolean existsByProfesionalIdAndCapacitacionIdAndEstadoNot(Long idProfesional, Long idCapacitacion, EstadoInscripcionCapacitacion estado);
}
