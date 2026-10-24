package com.acompanAR.backend.controller;
import com.acompanAR.backend.dto.InscripcionRegistroDTO;
import com.acompanAR.backend.entity.Capacitacion;
import com.acompanAR.backend.entity.Inscripcion;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.repository.CapacitacionRepository;
import com.acompanAR.backend.repository.InscripcionRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Inscripcion crearInscripcion(@RequestBody InscripcionRegistroDTO dto) {

        // Verificamos que el profesional exista
        Profesional profesional = profesionalRepository.findById(dto.getIdProfesional())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        // Verificamos que el curso exista
        Capacitacion capacitacion = capacitacionRepository.findById(dto.getIdCapacitacion())
                .orElseThrow(() -> new RuntimeException("Capacitación no encontrada"));

        // se genera la inscripcion
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setProfesional(profesional);
        inscripcion.setCapacitacion(capacitacion);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setEstado("Confirmada"); // Asumimos que la inscripción es directa

        return inscripcionRepository.save(inscripcion);
    }

    @GetMapping
    public List<Inscripcion> obtenerTodas() {
        return inscripcionRepository.findAll();
    }
}
