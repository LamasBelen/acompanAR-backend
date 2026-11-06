package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.EmpleadorEdicionDTO;
import com.acompanAR.backend.dto.EmpleadorRegistroDTO;
import com.acompanAR.backend.entity.Empleador;
import com.acompanAR.backend.security.Permisos;
import com.acompanAR.backend.service.EmpleadorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Capa web: recibe la petición HTTP y delega la lógica en EmpleadorService
@RestController
@RequestMapping("/api/empleadores")
public class EmpleadorController {

    private final EmpleadorService empleadorService;

    public EmpleadorController(EmpleadorService empleadorService) {
        this.empleadorService = empleadorService;
    }

    @PostMapping("/registro")
    public Empleador registrarEmpleador(@Valid @RequestBody EmpleadorRegistroDTO dto) {
        return empleadorService.registrar(dto);
    }

    @GetMapping
    public List<Empleador> obtenerTodos() {
        return empleadorService.listarActivos();
    }

    @GetMapping("/{id}")
    public Empleador obtenerPorId(@PathVariable Long id) {
        return empleadorService.obtenerPorId(id);
    }

    @PutMapping("/editar/{id}")
    public Empleador actualizarEmpleador(@PathVariable Long id,
                                         @Valid @RequestBody EmpleadorEdicionDTO dto,
                                         Authentication authentication) {
        return empleadorService.editar(id, dto, authentication);
    }

    // Baja lógica: la hace el propio empleador o un administrador
    @PutMapping("/baja/{id}")
    public Empleador darDeBaja(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Empleador empleador = empleadorService.darDeBaja(id, authentication);

        // Si se dio de baja a sí mismo, se cierra su sesión
        if (Permisos.esElMismoUsuario(empleador.getUsuario().getNombreUsuario(), authentication)) {
            HttpSession sesion = request.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }
        }
        return empleador;
    }
}
