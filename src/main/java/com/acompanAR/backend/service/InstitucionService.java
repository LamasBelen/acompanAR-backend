package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.InstitucionRegistroDTO;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.Institucion;
import com.acompanAR.backend.repository.InstitucionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Lógica de negocio de las instituciones que publican capacitaciones (ABM con baja lógica).
 */
@Service
public class InstitucionService {

    private final InstitucionRepository institucionRepository;

    public InstitucionService(InstitucionRepository institucionRepository) {
        this.institucionRepository = institucionRepository;
    }

    public List<Institucion> listarActivas() {
        return institucionRepository.findByEstado(Estado.ACTIVO);
    }

    public Institucion obtenerPorId(Long id) {
        return institucionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Institución no encontrada"));
    }

    @Transactional
    public Institucion crear(InstitucionRegistroDTO dto) {
        String nombre = dto.getNombre().trim();
        if (institucionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una institución llamada '" + nombre + "'");
        }
        Institucion institucion = new Institucion();
        institucion.setNombre(nombre);
        institucion.setTipo(dto.getTipo());
        institucion.setLocalidad(dto.getLocalidad());
        institucion.setEstado(Estado.ACTIVO);
        return institucionRepository.save(institucion);
    }

    @Transactional
    public Institucion editar(Long id, InstitucionRegistroDTO dto) {
        Institucion institucion = obtenerPorId(id);
        String nombre = dto.getNombre().trim();
        if (!institucion.getNombre().equalsIgnoreCase(nombre) && institucionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una institución llamada '" + nombre + "'");
        }
        institucion.setNombre(nombre);
        institucion.setTipo(dto.getTipo());
        institucion.setLocalidad(dto.getLocalidad());
        return institucionRepository.save(institucion);
    }

    // Baja lógica: queda INACTIVA y ya no se puede elegir para capacitaciones nuevas.
    // Las capacitaciones que ya tenía siguen mostrando su nombre.
    @Transactional
    public Institucion darDeBaja(Long id) {
        Institucion institucion = obtenerPorId(id);
        if (institucion.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La institución ya está dada de baja");
        }
        institucion.setEstado(Estado.INACTIVO);
        return institucionRepository.save(institucion);
    }
}
