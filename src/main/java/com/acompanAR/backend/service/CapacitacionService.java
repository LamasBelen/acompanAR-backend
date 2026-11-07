package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.CapacitacionRegistroDTO;
import com.acompanAR.backend.entity.Capacitacion;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.Institucion;
import com.acompanAR.backend.repository.CapacitacionRepository;
import com.acompanAR.backend.repository.InstitucionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio de las capacitaciones (ABM con baja lógica).
 */
@Service
public class CapacitacionService {

    private final CapacitacionRepository capacitacionRepository;
    private final InstitucionRepository institucionRepository;

    public CapacitacionService(CapacitacionRepository capacitacionRepository,
                               InstitucionRepository institucionRepository) {
        this.capacitacionRepository = capacitacionRepository;
        this.institucionRepository = institucionRepository;
    }

    // Busca la institución elegida y verifica que exista y no esté dada de baja
    private Institucion buscarInstitucion(Long id) {
        Institucion institucion = institucionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La institución elegida no existe"));
        if (institucion.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La institución elegida está dada de baja");
        }
        return institucion;
    }

    @Transactional
    public Capacitacion crear(CapacitacionRegistroDTO dto) {
        Capacitacion capacitacion = new Capacitacion();
        capacitacion.setTitulo(dto.getTitulo());
        capacitacion.setDescripcion(dto.getDescripcion());
        capacitacion.setInstitucion(buscarInstitucion(dto.getInstitucionId()));
        capacitacion.setCargaHoraria(dto.getCargaHoraria());
        capacitacion.setLinkVideo(dto.getLinkVideo());
        // Si no mandan fecha de inicio, se usa hoy + 15 días
        capacitacion.setFechaInicio(dto.getFechaInicio() != null ? dto.getFechaInicio() : LocalDate.now().plusDays(15));
        capacitacion.setEstado(Estado.ACTIVO);
        return capacitacionRepository.save(capacitacion);
    }

    // Solo las capacitaciones activas (las dadas de baja no se muestran)
    public List<Capacitacion> listarActivas() {
        return capacitacionRepository.findByEstado(Estado.ACTIVO);
    }

    public Capacitacion obtenerPorId(Long id) {
        return capacitacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capacitación no encontrada"));
    }

    @Transactional
    public Capacitacion editar(Long id, CapacitacionRegistroDTO dto) {
        Capacitacion capacitacion = obtenerPorId(id);

        capacitacion.setTitulo(dto.getTitulo());
        capacitacion.setDescripcion(dto.getDescripcion());
        capacitacion.setInstitucion(buscarInstitucion(dto.getInstitucionId()));
        capacitacion.setCargaHoraria(dto.getCargaHoraria());
        capacitacion.setLinkVideo(dto.getLinkVideo());
        if (dto.getFechaInicio() != null) {
            capacitacion.setFechaInicio(dto.getFechaInicio());
        }
        return capacitacionRepository.save(capacitacion);
    }

    // Baja lógica: no se borra el registro, solo se marca como INACTIVO
    @Transactional
    public Capacitacion darDeBaja(Long id) {
        Capacitacion capacitacion = obtenerPorId(id);
        capacitacion.setEstado(Estado.INACTIVO);
        return capacitacionRepository.save(capacitacion);
    }
}
