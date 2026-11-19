package com.acompanAR.backend.repository;
import com.acompanAR.backend.entity.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    // Para que el empleador vea quiénes se postularon a una oferta
    List<Postulacion> findByOfertaLaboralId(Long idOferta);

    // Para que el profesional vea sus propias postulaciones
    List<Postulacion> findByProfesionalId(Long idProfesional);

    // Para evitar que el mismo profesional se postule dos veces a la misma oferta
    boolean existsByProfesionalIdAndOfertaLaboralId(Long idProfesional, Long idOferta);

    // Trabajos (postulaciones en ciertos estados) en los que participa un usuario, sea como profesional o como empleador
    @Query("select p from Postulacion p where p.estado in :estados "
            + "and (p.profesional.usuario.id = :idUsuario or p.ofertaLaboral.empleador.usuario.id = :idUsuario)")
    List<Postulacion> buscarDeUsuario(@Param("idUsuario") Long idUsuario,
                                      @Param("estados") java.util.Collection<com.acompanAR.backend.entity.EstadoPostulacion> estados);

    // ¿Este profesional se postuló a alguna oferta de este empleador? (para dejarle ver su perfil)
    @Query("select count(p) > 0 from Postulacion p where p.profesional.id = :idProfesional "
            + "and p.ofertaLaboral.empleador.usuario.nombreUsuario = :nombreUsuarioEmpleador")
    boolean existePostulacionDeProfesionalAOfertasDe(@Param("idProfesional") Long idProfesional,
                                                     @Param("nombreUsuarioEmpleador") String nombreUsuarioEmpleador);
}
