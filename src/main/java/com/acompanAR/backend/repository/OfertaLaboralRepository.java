package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.OfertaLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfertaLaboralRepository extends JpaRepository <OfertaLaboral, Long> {
    // Solo las ofertas que no fueron dadas de baja
    List<OfertaLaboral> findByEstado(Estado estado);

    // Las ofertas activas de un empleador (para su pantalla "Mis ofertas")
    List<OfertaLaboral> findByEmpleadorIdAndEstado(Long idEmpleador, Estado estado);
}
