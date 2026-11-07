package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.InstitucionRegistroDTO;
import com.acompanAR.backend.entity.Institucion;
import com.acompanAR.backend.service.InstitucionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ABM de instituciones que publican capacitaciones.
// Listar: cualquier usuario logueado. Crear, editar y dar de baja: solo ADMINISTRADOR (ver SecurityConfig).
@RestController
@RequestMapping("/api/instituciones")
public class InstitucionController {

    private final InstitucionService institucionService;

    public InstitucionController(InstitucionService institucionService) {
        this.institucionService = institucionService;
    }

    @GetMapping
    public List<Institucion> listarActivas() {
        return institucionService.listarActivas();
    }

    @GetMapping("/{id}")
    public Institucion obtenerPorId(@PathVariable Long id) {
        return institucionService.obtenerPorId(id);
    }

    @PostMapping("/nueva")
    public Institucion crear(@Valid @RequestBody InstitucionRegistroDTO dto) {
        return institucionService.crear(dto);
    }

    @PutMapping("/editar/{id}")
    public Institucion editar(@PathVariable Long id, @Valid @RequestBody InstitucionRegistroDTO dto) {
        return institucionService.editar(id, dto);
    }

    @PutMapping("/baja/{id}")
    public Institucion darDeBaja(@PathVariable Long id) {
        return institucionService.darDeBaja(id);
    }
}
