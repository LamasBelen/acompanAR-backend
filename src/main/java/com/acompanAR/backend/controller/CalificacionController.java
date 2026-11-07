package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.CalificacionRegistroDTO;
import com.acompanAR.backend.dto.CalificacionRespuestaDTO;
import com.acompanAR.backend.dto.ReputacionDTO;
import com.acompanAR.backend.service.CalificacionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Capa web: recibe la petición HTTP y delega la lógica en CalificacionService
@RestController
@RequestMapping("/api/calificaciones")
public class CalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    // Profesional o empleador califica a la otra parte de una postulación aceptada
    @PostMapping("/nueva")
    public CalificacionRespuestaDTO calificar(@Valid @RequestBody CalificacionRegistroDTO dto, Authentication authentication) {
        return calificacionService.calificar(dto, authentication);
    }

    // Ids de las postulaciones que ya calificó el usuario logueado
    @GetMapping("/mias")
    public List<Long> postulacionesCalificadas(Authentication authentication) {
        return calificacionService.postulacionesCalificadas(authentication);
    }

    // Reputación (promedio y comentarios) de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public ReputacionDTO reputacion(@PathVariable Long idUsuario) {
        return calificacionService.reputacionDe(idUsuario);
    }

    // Moderación: solo ADMINISTRADOR (ver SecurityConfig)
    @PutMapping("/baja/{id}")
    public CalificacionRespuestaDTO darDeBaja(@PathVariable Long id) {
        return calificacionService.darDeBaja(id);
    }
}
