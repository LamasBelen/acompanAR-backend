package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.InscripcionRegistroDTO;
import com.acompanAR.backend.entity.Capacitacion;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoInscripcionCapacitacion;
import com.acompanAR.backend.entity.Inscripcion;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.repository.CapacitacionRepository;
import com.acompanAR.backend.repository.InscripcionRepository;
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
 * Lógica de negocio de las inscripciones de los profesionales a las capacitaciones.
 */
@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final ProfesionalRepository profesionalRepository;
    private final CapacitacionRepository capacitacionRepository;

    public InscripcionService(InscripcionRepository inscripcionRepository,
                              ProfesionalRepository profesionalRepository,
                              CapacitacionRepository capacitacionRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.profesionalRepository = profesionalRepository;
        this.capacitacionRepository = capacitacionRepository;
    }

    @Transactional
    public Inscripcion crear(InscripcionRegistroDTO dto, Authentication authentication) {
        Profesional profesional = buscarProfesional(dto.getIdProfesional());
        // Solo podés inscribirte a nombre tuyo
        verificarProfesional(profesional, authentication);

        Capacitacion capacitacion = capacitacionRepository.findById(dto.getIdCapacitacion())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capacitación no encontrada"));

        if (capacitacion.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La capacitación ya no está disponible");
        }
        // No se puede estar inscripto dos veces (salvo que la anterior se haya cancelado)
        if (inscripcionRepository.existsByProfesionalIdAndCapacitacionIdAndEstadoNot(
                profesional.getId(), capacitacion.getId(), EstadoInscripcionCapacitacion.CANCELADA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya estás inscripto en esta capacitación");
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setProfesional(profesional);
        inscripcion.setCapacitacion(capacitacion);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setEstado(EstadoInscripcionCapacitacion.CONFIRMADA); // la inscripción es directa
        return inscripcionRepository.save(inscripcion);
    }

    public List<Inscripcion> listarTodas() {
        return inscripcionRepository.findAll();
    }

    // El profesional consulta sus inscripciones
    public List<Inscripcion> listarPorProfesional(Long idProfesional, Authentication authentication) {
        Profesional profesional = buscarProfesional(idProfesional);
        verificarProfesional(profesional, authentication);
        return inscripcionRepository.findByProfesionalId(idProfesional);
    }

    // El profesional marca que terminó la capacitación
    @Transactional
    public Inscripcion finalizar(Long id, Authentication authentication) {
        return cambiarEstado(id, EstadoInscripcionCapacitacion.FINALIZADA, authentication);
    }

    // Baja lógica: el profesional se da de baja de la capacitación (el registro queda como CANCELADA)
    @Transactional
    public Inscripcion cancelar(Long id, Authentication authentication) {
        return cambiarEstado(id, EstadoInscripcionCapacitacion.CANCELADA, authentication);
    }

    // Reglas: la inscripción tiene que ser del usuario logueado y estar CONFIRMADA
    private Inscripcion cambiarEstado(Long id, EstadoInscripcionCapacitacion nuevoEstado, Authentication authentication) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscripción no encontrada"));

        if (!Permisos.esElMismoUsuario(inscripcion.getProfesional().getUsuario().getNombreUsuario(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta inscripción no es tuya");
        }
        if (inscripcion.getEstado() != EstadoInscripcionCapacitacion.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede modificar una inscripción confirmada. Estado actual: " + inscripcion.getEstado());
        }
        inscripcion.setEstado(nuevoEstado);
        return inscripcionRepository.save(inscripcion);
    }

    private Profesional buscarProfesional(Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
    }

    // 403 si el profesional no es el usuario logueado
    private void verificarProfesional(Profesional profesional, Authentication authentication) {
        if (!Permisos.esElMismoUsuario(profesional.getUsuario().getNombreUsuario(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permiso sobre los datos de otro profesional");
        }
    }
}
