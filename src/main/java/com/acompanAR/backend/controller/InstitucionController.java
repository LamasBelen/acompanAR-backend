package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.InstitucionAdminDTO;
import com.acompanAR.backend.dto.InstitucionCuentaRegistroDTO;
import com.acompanAR.backend.dto.InstitucionRegistroDTO;
import com.acompanAR.backend.entity.Institucion;
import com.acompanAR.backend.service.InstitucionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Capa web: recibe la petición HTTP y delega la lógica en InstitucionService.
// Registro: público. Verificar, rechazar, crear, editar y dar de baja: solo ADMINISTRADOR (ver SecurityConfig).
@RestController
@RequestMapping("/api/instituciones")
public class InstitucionController {

    private final InstitucionService institucionService;

    public InstitucionController(InstitucionService institucionService) {
        this.institucionService = institucionService;
    }

    // Una institución se registra por su cuenta: queda pendiente de verificación
    @PostMapping("/registro")
    public Institucion registrar(@Valid @RequestBody InstitucionCuentaRegistroDTO dto) {
        return institucionService.registrar(dto);
    }

    // Instituciones activas y verificadas (para elegir en los formularios)
    @GetMapping
    public List<Institucion> listarVerificadas() {
        return institucionService.listarVerificadas();
    }

    // Para el administrador: todas, con su estado de verificación y los datos del responsable
    @GetMapping("/todas")
    public List<InstitucionAdminDTO> listarParaAdmin() {
        return institucionService.listarParaAdmin();
    }

    // La institución del usuario logueado (para su pantalla de inicio)
    @GetMapping("/mia")
    public Institucion miInstitucion(Authentication authentication) {
        return institucionService.obtenerDelUsuario(authentication);
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

    // El administrador aprueba una institución pendiente
    @PutMapping("/verificar/{id}")
    public Institucion verificar(@PathVariable Long id) {
        return institucionService.verificar(id);
    }

    // El administrador rechaza una solicitud pendiente
    @PutMapping("/rechazar/{id}")
    public Institucion rechazar(@PathVariable Long id) {
        return institucionService.rechazar(id);
    }

    @PutMapping("/baja/{id}")
    public Institucion darDeBaja(@PathVariable Long id) {
        return institucionService.darDeBaja(id);
    }
}
