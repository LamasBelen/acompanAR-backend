package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.Empleador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface EmpleadorRepository extends JpaRepository<Empleador, Long> {
}
