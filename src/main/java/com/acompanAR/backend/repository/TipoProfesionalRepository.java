package com.acompanAR.backend.repository;

import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.TipoProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoProfesionalRepository extends JpaRepository<TipoProfesional, Long> {
    List<TipoProfesional> findByEstado(Estado estado);

    boolean existsByNombreIgnoreCase(String nombre);
}
