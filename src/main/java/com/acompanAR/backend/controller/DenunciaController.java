package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.DenunciaRegistroDTO;
import com.acompanAR.backend.dto.DenunciaResolucionDTO;
import com.acompanAR.backend.dto.DenunciaRespuestaDTO;
import com.acompanAR.backend.entity.EstadoDenuncia;
import com.acompanAR.backend.service.DenunciaService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Capa web: recibe la petición HTTP y delega la lógica en DenunciaService.
// Denunciar: profesionales, empleadores e instituciones. Ver y resolver: solo ADMINISTRADOR (ver SecurityConfig).
@RestController
@RequestMapping("/api/denuncias")
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping("/nueva")
    public DenunciaRespuestaDTO denunciar(@Valid @RequestBody DenunciaRegistroDTO dto, Authentication authentication) {
        return denunciaService.denunciar(dto, authentication);
    }

    // Todas las denuncias o, con ?estado=PENDIENTE, solo las de ese estado
    @GetMapping
    public List<DenunciaRespuestaDTO> listar(@RequestParam(required = false) EstadoDenuncia estado) {
        return denunciaService.listar(estado);
    }

    // El administrador da de baja lo denunciado o descarta la denuncia
    @PutMapping("/resolver/{id}")
    public DenunciaRespuestaDTO resolver(@PathVariable Long id,
                                         @Valid @RequestBody DenunciaResolucionDTO dto,
                                         Authentication authentication) {
        return denunciaService.resolver(id, dto, authentication);
    }
}
