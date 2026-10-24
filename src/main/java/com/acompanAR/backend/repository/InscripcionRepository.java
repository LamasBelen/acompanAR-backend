package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
public interface InscripcionRepository extends JpaRepository <Inscripcion,Long> {
}
