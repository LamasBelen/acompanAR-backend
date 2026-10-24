package com.acompanAR.backend.controller;
import com.acompanAR.backend.dto.CapacitacionRegistroDTO;
import com.acompanAR.backend.entity.Capacitacion;
import com.acompanAR.backend.repository.CapacitacionRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping ("/api/capacitaciones")
public class CapacitacionController {
    @Autowired
    private CapacitacionRepository capacitacionRepository;
    @PostMapping("/nueva")
    public Capacitacion crearCapacitacion(@Valid @RequestBody CapacitacionRegistroDTO dto){
        Capacitacion capacitacion = new Capacitacion();
        capacitacion.setTitulo(dto.getTitulo());
        capacitacion.setDescripcion(dto.getDescripcion());
        capacitacion.setInstitucion(dto.getInstitucion());
        capacitacion.setCargaHoraria(dto.getCargaHoraria());
        capacitacion.setFechaInicio(LocalDate.now().plusDays(15)); // Le ponemos que empieza en 15 días por defecto

        return capacitacionRepository.save(capacitacion);
    }

    @GetMapping
    public List<Capacitacion> obtenerTodas() {
        return capacitacionRepository.findAll();
    }
    @PutMapping("/baja/{id}")
    public Capacitacion darDeBajaCapacitacion(@PathVariable Long id) {
        Capacitacion capacitacion = capacitacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Capacitación no encontrada"));

        // Usá setEstado(false) o setActiva(false) dependiendo de cómo llamaste a tu variable booleana
        capacitacion.setEstado(false);

        return capacitacionRepository.save(capacitacion);
    }
}
