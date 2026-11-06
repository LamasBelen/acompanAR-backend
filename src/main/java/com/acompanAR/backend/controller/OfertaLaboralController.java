package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.OfertaLaboralEdicionDTO;
import com.acompanAR.backend.dto.OfertaLaboralRegistroDTO;
import com.acompanAR.backend.entity.OfertaLaboral;
import com.acompanAR.backend.service.OfertaLaboralService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Capa web: recibe la petición HTTP y delega la lógica en OfertaLaboralService
@RestController
@RequestMapping("/api/ofertas")
public class OfertaLaboralController {

    private final OfertaLaboralService ofertaLaboralService;

    public OfertaLaboralController(OfertaLaboralService ofertaLaboralService) {
        this.ofertaLaboralService = ofertaLaboralService;
    }

    @PostMapping("/publicar")
    public OfertaLaboral publicarOferta(@Valid @RequestBody OfertaLaboralRegistroDTO dto, Authentication authentication) {
        return ofertaLaboralService.publicar(dto, authentication);
    }

    // Ofertas publicadas (solo las activas)
    @GetMapping
    public List<OfertaLaboral> obtenerTodas() {
        return ofertaLaboralService.listarActivas();
    }

    // Una oferta puntual (se usa para cargar el formulario de edición)
    @GetMapping("/{id}")
    public OfertaLaboral obtenerPorId(@PathVariable Long id) {
        return ofertaLaboralService.obtenerPorId(id);
    }

    // Las ofertas activas de un empleador (solo él puede consultarlas)
    @GetMapping("/empleador/{idEmpleador}")
    public List<OfertaLaboral> porEmpleador(@PathVariable Long idEmpleador, Authentication authentication) {
        return ofertaLaboralService.listarPorEmpleador(idEmpleador, authentication);
    }

    @PutMapping("/editar/{id}")
    public OfertaLaboral editarOferta(@PathVariable Long id,
                                      @Valid @RequestBody OfertaLaboralEdicionDTO dto,
                                      Authentication authentication) {
        return ofertaLaboralService.editar(id, dto, authentication);
    }

    // Baja lógica
    @PutMapping("/baja/{id}")
    public OfertaLaboral darDeBaja(@PathVariable Long id, Authentication authentication) {
        return ofertaLaboralService.darDeBaja(id, authentication);
    }
}
