package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.OfertaLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface OfertaLaboralRepository extends JpaRepository <OfertaLaboral, Long> {

}
