package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.TipoProfesionalDTO;
import com.acompanAR.backend.entity.TipoProfesional;
import com.acompanAR.backend.service.TipoProfesionalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ABM de tipos de profesional. Listar es público (se necesita en el registro);
// crear, editar y dar de baja solo puede el ADMINISTRADOR (ver SecurityConfig).
@RestController
@RequestMapping("/api/tipos-profesional")
public class TipoProfesionalController {

    private final TipoProfesionalService tipoProfesionalService;

    public TipoProfesionalController(TipoProfesionalService tipoProfesionalService) {
        this.tipoProfesionalService = tipoProfesionalService;
    }

    @GetMapping
    public List<TipoProfesional> listarActivos() {
        return tipoProfesionalService.listarActivos();
    }

    @PostMapping("/nuevo")
    public TipoProfesional crear(@Valid @RequestBody TipoProfesionalDTO dto) {
        return tipoProfesionalService.crear(dto);
    }

    @PutMapping("/editar/{id}")
    public TipoProfesional editar(@PathVariable Long id, @Valid @RequestBody TipoProfesionalDTO dto) {
        return tipoProfesionalService.editar(id, dto);
    }

    @PutMapping("/baja/{id}")
    public TipoProfesional darDeBaja(@PathVariable Long id) {
        return tipoProfesionalService.darDeBaja(id);
    }
}
