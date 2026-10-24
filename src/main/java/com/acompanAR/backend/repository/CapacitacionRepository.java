package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.Capacitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
public interface CapacitacionRepository extends JpaRepository <Capacitacion, Long> {
}
