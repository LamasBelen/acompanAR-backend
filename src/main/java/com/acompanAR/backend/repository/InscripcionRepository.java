package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.EstadoInscripcionCapacitacion;
import com.acompanAR.backend.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
public interface InscripcionRepository extends JpaRepository <Inscripcion,Long> {
    //esto es para que el profesional pueda ver sus inscripciones
    List<Inscripcion> findByProfesionalId(Long idProfesional);
    // Para evitar que se inscriba dos veces a la misma capacitacion mientras sigue vigente
    boolean existsByProfesionalIdAndCapacitacionIdAndEstadoNot(Long idProfesional, Long idCapacitacion, EstadoInscripcionCapacitacion estado);
}
