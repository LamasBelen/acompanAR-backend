package com.acompanAR.backend.controller;
import com.acompanAR.backend.entity.Institucion;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.dto.CapacitacionRegistroDTO;
import com.acompanAR.backend.entity.Capacitacion;
import com.acompanAR.backend.repository.CapacitacionRepository;
import com.acompanAR.backend.repository.InstitucionRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping ("/api/capacitaciones")
public class CapacitacionController {
    @Autowired
    private CapacitacionRepository capacitacionRepository;
    @Autowired
    private InstitucionRepository institucionRepository;

    // Busca la institución elegida y verifica que exista y no esté dada de baja
    private Institucion buscarInstitucion(Long id) {
        Institucion institucion = institucionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La institución elegida no existe"));
        if (institucion.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La institución elegida está dada de baja");
        }
        return institucion;
    }

    @PostMapping("/nueva")
    public Capacitacion crearCapacitacion(@Valid @RequestBody CapacitacionRegistroDTO dto){
        Capacitacion capacitacion = new Capacitacion();
        capacitacion.setTitulo(dto.getTitulo());
        capacitacion.setDescripcion(dto.getDescripcion());
        capacitacion.setInstitucion(buscarInstitucion(dto.getInstitucionId()));
        capacitacion.setCargaHoraria(dto.getCargaHoraria());
        capacitacion.setLinkVideo(dto.getLinkVideo());
        // Si no mandan fecha de inicio, se usa hoy + 15 dias
        capacitacion.setFechaInicio(dto.getFechaInicio() != null ? dto.getFechaInicio() : LocalDate.now().plusDays(15));
        capacitacion.setEstado(Estado.ACTIVO);

        return capacitacionRepository.save(capacitacion);
    }

    // Solo devuelve las capacitaciones activas (las dadas de baja no se muestran)
    @GetMapping
    public List<Capacitacion> obtenerTodas() {
        return capacitacionRepository.findByEstado(Estado.ACTIVO);
    }

    // Ver una capacitacion puntual (se usa para cargar el formulario de edición)
    @GetMapping("/{id}")
    public Capacitacion obtenerPorId(@PathVariable Long id) {
        return capacitacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capacitación no encontrada"));
    }

    // Modificar una capacitacion existente
    @PutMapping("/editar/{id}")
    public Capacitacion editarCapacitacion(@PathVariable Long id, @Valid @RequestBody CapacitacionRegistroDTO dto) {
        Capacitacion capacitacion = capacitacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capacitación no encontrada"));

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

    // Baja lógica: no se borra el registro, solo se marca como INACTIVA
    @PutMapping("/baja/{id}")
    public Capacitacion darDeBajaCapacitacion(@PathVariable Long id) {
        Capacitacion capacitacion = capacitacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capacitación no encontrada"));

        capacitacion.setEstado(Estado.INACTIVO);

        return capacitacionRepository.save(capacitacion);
    }
}
