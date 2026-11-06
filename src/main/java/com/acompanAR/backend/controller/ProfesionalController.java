package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.ProfesionalEdicionDTO;
import com.acompanAR.backend.dto.ProfesionalRegistroDTO;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.security.Permisos;
import com.acompanAR.backend.service.ProfesionalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Capa web: recibe la petición HTTP y delega la lógica en ProfesionalService
@RestController
@RequestMapping("/api/profesionales")
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    public ProfesionalController(ProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }

    @PostMapping("/registro")
    public Profesional registrarProfesional(@Valid @RequestBody ProfesionalRegistroDTO dto) {
        return profesionalService.registrar(dto);
    }

    @GetMapping
    public List<Profesional> obtenerTodos() {
        return profesionalService.listarActivos();
    }

    @GetMapping("/{id}")
    public Profesional obtenerPorId(@PathVariable Long id) {
        return profesionalService.obtenerPorId(id);
    }

    @PutMapping("/editar/{id}")
    public Profesional actualizarProfesional(@PathVariable Long id,
                                             @Valid @RequestBody ProfesionalEdicionDTO dto,
                                             Authentication authentication) {
        return profesionalService.editar(id, dto, authentication);
    }

    // Baja lógica: la hace el propio profesional o un administrador
    @PutMapping("/baja/{id}")
    public Profesional darDeBaja(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Profesional profesional = profesionalService.darDeBaja(id, authentication);

        // Si se dio de baja a sí mismo, se cierra su sesión (esto es del mundo HTTP, por eso va acá)
        if (Permisos.esElMismoUsuario(profesional.getUsuario().getNombreUsuario(), authentication)) {
            HttpSession sesion = request.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }
        }
        return profesional;
    }
}
