package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.PostulacionRegistroDTO;
import com.acompanAR.backend.entity.Postulacion;
import com.acompanAR.backend.service.PostulacionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Capa web: recibe la petición HTTP y delega la lógica en PostulacionService
@RestController
@RequestMapping("/api/postulaciones")
public class PostulacionesController {

    private final PostulacionService postulacionService;

    public PostulacionesController(PostulacionService postulacionService) {
        this.postulacionService = postulacionService;
    }

    @PostMapping("/nueva")
    public Postulacion crearPostulacion(@Valid @RequestBody PostulacionRegistroDTO dto, Authentication authentication) {
        return postulacionService.crear(dto, authentication);
    }

    // Listado completo: solo para administradores (ver SecurityConfig)
    @GetMapping
    public List<Postulacion> obtenerTodas() {
        return postulacionService.listarTodas();
    }

    // El empleador consulta quiénes se postularon a SU oferta
    @GetMapping("/oferta/{idOferta}")
    public List<Postulacion> porOferta(@PathVariable Long idOferta, Authentication authentication) {
        return postulacionService.listarPorOferta(idOferta, authentication);
    }

    // El profesional consulta sus propias postulaciones
    @GetMapping("/profesional/{idProfesional}")
    public List<Postulacion> porProfesional(@PathVariable Long idProfesional, Authentication authentication) {
        return postulacionService.listarPorProfesional(idProfesional, authentication);
    }

    @PutMapping("/aceptar/{id}")
    public Postulacion aceptar(@PathVariable Long id, Authentication authentication) {
        return postulacionService.aceptar(id, authentication);
    }

    @PutMapping("/rechazar/{id}")
    public Postulacion rechazar(@PathVariable Long id, Authentication authentication) {
        return postulacionService.rechazar(id, authentication);
    }

    @PutMapping("/cancelar/{id}")
    public Postulacion cancelar(@PathVariable Long id, Authentication authentication) {
        return postulacionService.cancelar(id, authentication);
    }
}
