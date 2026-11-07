package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.TipoProfesionalDTO;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.TipoProfesional;
import com.acompanAR.backend.repository.TipoProfesionalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Lógica de negocio de los tipos de profesional (ABM con baja lógica).
 */
@Service
public class TipoProfesionalService {

    private final TipoProfesionalRepository tipoProfesionalRepository;

    public TipoProfesionalService(TipoProfesionalRepository tipoProfesionalRepository) {
        this.tipoProfesionalRepository = tipoProfesionalRepository;
    }

    public List<TipoProfesional> listarActivos() {
        return tipoProfesionalRepository.findByEstado(Estado.ACTIVO);
    }

    private TipoProfesional buscar(Long id) {
        return tipoProfesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de profesional no encontrado"));
    }

    @Transactional
    public TipoProfesional crear(TipoProfesionalDTO dto) {
        String nombre = dto.getNombre().trim();
        if (tipoProfesionalRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un tipo de profesional llamado '" + nombre + "'");
        }
        TipoProfesional tipo = new TipoProfesional();
        tipo.setNombre(nombre);
        tipo.setEstado(Estado.ACTIVO);
        return tipoProfesionalRepository.save(tipo);
    }

    @Transactional
    public TipoProfesional editar(Long id, TipoProfesionalDTO dto) {
        TipoProfesional tipo = buscar(id);
        String nombre = dto.getNombre().trim();
        if (!tipo.getNombre().equalsIgnoreCase(nombre) && tipoProfesionalRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un tipo de profesional llamado '" + nombre + "'");
        }
        tipo.setNombre(nombre);
        return tipoProfesionalRepository.save(tipo);
    }

    // Baja lógica: no se borra, queda INACTIVO (los profesionales que ya lo tenían lo conservan)
    @Transactional
    public TipoProfesional darDeBaja(Long id) {
        TipoProfesional tipo = buscar(id);
        if (tipo.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El tipo de profesional ya está dado de baja");
        }
        tipo.setEstado(Estado.INACTIVO);
        return tipoProfesionalRepository.save(tipo);
    }
}
