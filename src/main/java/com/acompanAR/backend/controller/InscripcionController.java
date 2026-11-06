package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.InscripcionRegistroDTO;
import com.acompanAR.backend.entity.Inscripcion;
import com.acompanAR.backend.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Capa web: recibe la petición HTTP y delega la lógica en InscripcionService
@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @PostMapping("/nueva")
    public Inscripcion crearInscripcion(@Valid @RequestBody InscripcionRegistroDTO dto, Authentication authentication) {
        return inscripcionService.crear(dto, authentication);
    }

    @GetMapping
    public List<Inscripcion> obtenerTodas() {
        return inscripcionService.listarTodas();
    }

    // El profesional consulta sus inscripciones
    @GetMapping("/profesional/{idProfesional}")
    public List<Inscripcion> porProfesional(@PathVariable Long idProfesional, Authentication authentication) {
        return inscripcionService.listarPorProfesional(idProfesional, authentication);
    }

    // El profesional marca que terminó la capacitación
    @PutMapping("/finalizar/{id}")
    public Inscripcion finalizar(@PathVariable Long id, Authentication authentication) {
        return inscripcionService.finalizar(id, authentication);
    }

    // Baja lógica: el profesional se da de baja de la capacitación
    @PutMapping("/cancelar/{id}")
    public Inscripcion cancelar(@PathVariable Long id, Authentication authentication) {
        return inscripcionService.cancelar(id, authentication);
    }
}
