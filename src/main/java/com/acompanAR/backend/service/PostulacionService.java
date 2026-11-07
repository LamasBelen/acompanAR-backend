package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.PostulacionRegistroDTO;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoPostulacion;
import com.acompanAR.backend.entity.OfertaLaboral;
import com.acompanAR.backend.entity.Postulacion;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.repository.OfertaLaboralRepository;
import com.acompanAR.backend.repository.PostulacionRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import com.acompanAR.backend.security.Permisos;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio de las postulaciones: un profesional se postula a una oferta,
 * el empleador la acepta o rechaza, y el profesional puede cancelarla.
 */
@Service
public class PostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final ProfesionalRepository profesionalRepository;
    private final OfertaLaboralRepository ofertaLaboralRepository;

    public PostulacionService(PostulacionRepository postulacionRepository,
                              ProfesionalRepository profesionalRepository,
                              OfertaLaboralRepository ofertaLaboralRepository) {
        this.postulacionRepository = postulacionRepository;
        this.profesionalRepository = profesionalRepository;
        this.ofertaLaboralRepository = ofertaLaboralRepository;
    }

    @Transactional
    public Postulacion crear(PostulacionRegistroDTO dto, Authentication authentication) {
        Profesional profesional = buscarProfesional(dto.getIdProfesional());
        // Solo podés postularte a nombre tuyo
        verificarProfesional(profesional, authentication);

        OfertaLaboral oferta = buscarOferta(dto.getIdOfertaLaboral());

        // No se puede postular a una oferta que fue dada de baja
        if (oferta.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La oferta laboral ya no está activa");
        }
        // No se puede postular dos veces a la misma oferta
        if (postulacionRepository.existsByProfesionalIdAndOfertaLaboralId(profesional.getId(), oferta.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya te postulaste a esta oferta");
        }

        Postulacion postulacion = new Postulacion();
        postulacion.setProfesional(profesional);
        postulacion.setOfertaLaboral(oferta);
        postulacion.setFechaPostulacion(LocalDate.now());
        postulacion.setEstado(EstadoPostulacion.PENDIENTE); // toda postulación nueva arranca pendiente
        return postulacionRepository.save(postulacion);
    }

    // Listado completo: solo para administradores (ver SecurityConfig)
    public List<Postulacion> listarTodas() {
        return postulacionRepository.findAll();
    }

    // El empleador consulta quiénes se postularon a SU oferta
    public List<Postulacion> listarPorOferta(Long idOferta, Authentication authentication) {
        OfertaLaboral oferta = buscarOferta(idOferta);
        verificarEmpleador(oferta, authentication);
        return postulacionRepository.findByOfertaLaboralId(idOferta);
    }

    // El profesional consulta sus propias postulaciones
    public List<Postulacion> listarPorProfesional(Long idProfesional, Authentication authentication) {
        Profesional profesional = buscarProfesional(idProfesional);
        verificarProfesional(profesional, authentication);
        return postulacionRepository.findByProfesionalId(idProfesional);
    }

    // El empleador acepta la postulación
    @Transactional
    public Postulacion aceptar(Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);
        verificarEmpleador(postulacion.getOfertaLaboral(), authentication);
        return cambiarEstado(postulacion, EstadoPostulacion.ACEPTADA);
    }

    // El empleador rechaza la postulación
    @Transactional
    public Postulacion rechazar(Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);
        verificarEmpleador(postulacion.getOfertaLaboral(), authentication);
        return cambiarEstado(postulacion, EstadoPostulacion.RECHAZADA);
    }

    // El profesional cancela su postulación (baja lógica: el registro queda, con estado CANCELADA)
    @Transactional
    public Postulacion cancelar(Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);
        verificarProfesional(postulacion.getProfesional(), authentication);
        return cambiarEstado(postulacion, EstadoPostulacion.CANCELADA);
    }

    // Regla de negocio: solo una postulación PENDIENTE puede pasar a otro estado.
    // Una vez aceptada, rechazada o cancelada, queda cerrada.
    private Postulacion cambiarEstado(Postulacion postulacion, EstadoPostulacion nuevoEstado) {
        if (postulacion.getEstado() != EstadoPostulacion.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede modificar una postulación pendiente. Estado actual: " + postulacion.getEstado());
        }
        postulacion.setEstado(nuevoEstado);
        return postulacionRepository.save(postulacion);
    }

    private Postulacion buscarPostulacion(Long id) {
        return postulacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Postulación no encontrada"));
    }

    private OfertaLaboral buscarOferta(Long id) {
        return ofertaLaboralRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta laboral no encontrada"));
    }

    private Profesional buscarProfesional(Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
    }

    // 403 si la oferta no pertenece al empleador logueado
    private void verificarEmpleador(OfertaLaboral oferta, Authentication authentication) {
        if (!Permisos.esElMismoUsuario(oferta.getEmpleador().getUsuario().getNombreUsuario(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta oferta no es tuya");
        }
    }

    // 403 si el profesional no es el usuario logueado
    private void verificarProfesional(Profesional profesional, Authentication authentication) {
        if (!Permisos.esElMismoUsuario(profesional.getUsuario().getNombreUsuario(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permiso sobre los datos de otro profesional");
        }
    }
}
