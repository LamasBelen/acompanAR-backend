package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
}
