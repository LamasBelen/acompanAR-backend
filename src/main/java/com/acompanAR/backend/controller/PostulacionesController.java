package com.acompanAR.backend.controller;
import com.acompanAR.backend.dto.PostulacionRegistroDTO;
import com.acompanAR.backend.entity.OfertaLaboral;
import com.acompanAR.backend.entity.Postulacion;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.repository.OfertaLaboralRepository;
import com.acompanAR.backend.repository.PostulacionRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/postulaciones")

public class PostulacionesController {
    @Autowired
    private PostulacionRepository postulacionRepository;
    @Autowired
    private ProfesionalRepository profesionalRepository;
    @Autowired
    private OfertaLaboralRepository ofertaLaboralRepository;

    @PostMapping("/nueva")
    public Postulacion crearPostulacion( @Valid @RequestBody PostulacionRegistroDTO dto){
        //primero buscamos que el profesional exista
        Profesional profesional = profesionalRepository.findById(dto.getIdProfesional())
                .orElseThrow(() -> new RuntimeException("profesional no encontrado"));
        //ahora buscamos que exista la oferta
        OfertaLaboral oferta = ofertaLaboralRepository.findById(dto.getIdOfertaLaboral())
                .orElseThrow(() -> new RuntimeException("oferta laboral no en contrada"));
        //se crea la postulacion
        Postulacion postulacion = new Postulacion();
        postulacion.setProfesional(profesional);
        postulacion.setOfertaLaboral(oferta);
        postulacion.setFechaPostulacion(LocalDate.now());
        postulacion.setEstado(true); //porque se va a inicializar como pendiente en revsision
        return postulacionRepository.save(postulacion);
    }
        @GetMapping
        public List <Postulacion> obtenerTodas(){
            return postulacionRepository.findAll();
        }
    // Método para cancelar (Baja Lógica) la postulación[cite: 1]
    @PutMapping("/baja/{id}")
    public Postulacion cancelarPostulacion(@PathVariable Long id) {
        Postulacion postulacion = postulacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

        // Cambiamos el estado a falso para ocultarla
        postulacion.setEstado(false);

        return postulacionRepository.save(postulacion);
    }



}
