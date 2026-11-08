package com.acompanAR.backend.repository;

import com.acompanAR.backend.entity.Denuncia;
import com.acompanAR.backend.entity.EstadoDenuncia;
import com.acompanAR.backend.entity.ObjetoDenunciado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {
    // Para no permitir que la misma persona denuncie dos veces lo mismo mientras sigue en revisión
    boolean existsByDenuncianteIdAndObjetoAndIdObjetoAndEstado(Long idDenunciante, ObjetoDenunciado objeto,
                                                               Long idObjeto, EstadoDenuncia estado);

    List<Denuncia> findByEstado(EstadoDenuncia estado);

    // Todas las denuncias pendientes sobre lo mismo (se resuelven juntas cuando se da de baja)
    List<Denuncia> findByObjetoAndIdObjetoAndEstado(ObjetoDenunciado objeto, Long idObjeto, EstadoDenuncia estado);
}
