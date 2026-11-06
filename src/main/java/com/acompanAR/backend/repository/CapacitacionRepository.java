package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.Capacitacion;
import com.acompanAR.backend.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapacitacionRepository extends JpaRepository <Capacitacion, Long> {
    // Spring genera la consulta solo a partir del nombre: SELECT * FROM capacitaciones WHERE estado = ?
    List<Capacitacion> findByEstado(Estado estado);
}
