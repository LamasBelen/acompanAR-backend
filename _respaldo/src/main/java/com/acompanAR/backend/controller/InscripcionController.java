package com.acompanAR.backend.controller;
import com.acompanAR.backend.dto.InscripcionRegistroDTO;
import com.acompanAR.backend.entity.Capacitacion;
import com.acompanAR.backend.entity.EstadoInscripcionCapacitacion;
import com.acompanAR.backend.entity.Inscripcion;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.repository.CapacitacionRepository;
import com.acompanAR.backend.repository.InscripcionRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {
    @Autowired
    private InscripcionRepository inscripcionRepository;
    @Autowired
    private ProfesionalRepository profesionalRepository;
    @Autowired
    private CapacitacionRepository capacitacionRepository;

    @PostMapping("/nueva")
    public Inscripcion crearInscripcion(@RequestBody InscripcionRegistroDTO dto, Authentication authentication) {

        // Verificamos que el profesional exista
        Profesional profesional = profesionalRepository.findById(dto.getIdProfesional())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));

        // Solo podés inscribirte a nombre tuyo
        verificarProfesional(profesional, authentication);

        // Verificamos que el curso exista
        Capacitacion capacitacion = capacitacionRepository.findById(dto.getIdCapacitacion())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capacitación no encontrada"));

        if (!capacitacion.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La capacitación ya no está disponible");
        }
        // No se puede estar inscripto dos veces (salvo que la anterior se haya cancelado)
        if (inscripcionRepository.existsByProfesionalIdAndCapacitacionIdAndEstadoNot(
                profesional.getId(), capacitacion.getId(), EstadoInscripcionCapacitacion.CANCELADA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya estás inscripto en esta capacitación");
        }

        // se genera la inscripcion
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setProfesional(profesional);
        inscripcion.setCapacitacion(capacitacion);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setEstado(EstadoInscripcionCapacitacion.CONFIRMADA); // la inscripción es directa

        return inscripcionRepository.save(inscripcion);
    }

    @GetMapping
    public List<Inscripcion> obtenerTodas() {
        return inscripcionRepository.findAll();
    }

    // El profesional consulta sus inscripciones
    @GetMapping("/profesional/{idProfesional}")
    public List<Inscripcion> porProfesional(@PathVariable Long idProfesional, Authentication authentication) {
        Profesional profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
        verificarProfesional(profesional, authentication);
        return inscripcionRepository.findByProfesionalId(idProfesional);
    }

    // 403 si el profesional no es el usuario logueado
    private void verificarProfesional(Profesional profesional, Authentication authentication) {
        if (!profesional.getUsuario().getNombreUsuario().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permiso sobre los datos de otro profesional");
        }
    }

    // Modificación: el profesional marca que terminó la capacitación
    @PutMapping("/finalizar/{id}")
    public Inscripcion finalizar(@PathVariable Long id, Authentication authentication) {
        return cambiarEstado(id, EstadoInscripcionCapacitacion.FINALIZADA, authentication);
    }

    // Baja lógica: el profesional se da de baja de la capacitación (el registro queda como CANCELADA)
    @PutMapping("/cancelar/{id}")
    public Inscripcion cancelar(@PathVariable Long id, Authentication authentication) {
        return cambiarEstado(id, EstadoInscripcionCapacitacion.CANCELADA, authentication);
    }

    // Reglas: la inscripción tiene que ser del usuario logueado y estar CONFIRMADA
    private Inscripcion cambiarEstado(Long id, EstadoInscripcionCapacitacion nuevoEstado, Authentication authentication) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscripción no encontrada"));

        String duenio = inscripcion.getProfesional().getUsuario().getNombreUsuario();
        if (!duenio.equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta inscripción no es tuya");
        }
        if (inscripcion.getEstado() != EstadoInscripcionCapacitacion.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede modificar una inscripción confirmada. Estado actual: " + inscripcion.getEstado());
        }
        inscripcion.setEstado(nuevoEstado);
        return inscripcionRepository.save(inscripcion);
    }
}