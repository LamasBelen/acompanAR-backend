package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.CapacitacionRegistroDTO;
import com.acompanAR.backend.entity.Capacitacion;
import com.acompanAR.backend.service.CapacitacionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Capa web: recibe la petición HTTP y delega la lógica en CapacitacionService
@RestController
@RequestMapping("/api/capacitaciones")
public class CapacitacionController {

    private final CapacitacionService capacitacionService;

    public CapacitacionController(CapacitacionService capacitacionService) {
        this.capacitacionService = capacitacionService;
    }

    @PostMapping("/nueva")
    public Capacitacion crearCapacitacion(@Valid @RequestBody CapacitacionRegistroDTO dto) {
        return capacitacionService.crear(dto);
    }

    // Solo devuelve las capacitaciones activas
    @GetMapping
    public List<Capacitacion> obtenerTodas() {
        return capacitacionService.listarActivas();
    }

    @GetMapping("/{id}")
    public Capacitacion obtenerPorId(@PathVariable Long id) {
        return capacitacionService.obtenerPorId(id);
    }

    @PutMapping("/editar/{id}")
    public Capacitacion editarCapacitacion(@PathVariable Long id, @Valid @RequestBody CapacitacionRegistroDTO dto) {
        return capacitacionService.editar(id, dto);
    }

    // Baja lógica
    @PutMapping("/baja/{id}")
    public Capacitacion darDeBajaCapacitacion(@PathVariable Long id) {
        return capacitacionService.darDeBaja(id);
    }
}
