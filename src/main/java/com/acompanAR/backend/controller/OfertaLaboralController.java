package com.acompanAR.backend.controller;
import com.acompanAR.backend.dto.OfertaLaboralRegistroDTO;
import com.acompanAR.backend.entity.Empleador;
import com.acompanAR.backend.entity.OfertaLaboral;
import com.acompanAR.backend.repository.EmpleadorRepository;
import com.acompanAR.backend.repository.OfertaLaboralRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/ofertas")
public class OfertaLaboralController {
    @Autowired
    private OfertaLaboralRepository ofertaLaboralRepository;
    @Autowired
    private EmpleadorRepository empleadorRepository;
    @PostMapping("/publicar")
    public OfertaLaboral publicarOferta(@Valid @RequestBody OfertaLaboralRegistroDTO dto){
        //aca se busca al empleador en la base de datos usando el id que mando
        Empleador empleador = empleadorRepository.findById(dto.getIdEmpleador())
                .orElseThrow(()-> new RuntimeException("empleador no encontrado"));
                //aca se crea la oferta vacia y se empieza a llenar
        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setTitulo(dto.getTitulo());
        oferta.setDescripcion(dto.getDescripcion());
        oferta.setRequisitos(dto.getRequisitos());

        // La fecha y el estado los ponemos automáticamente por código, no se los pedimos al usuario
        oferta.setFechaPublicacion(LocalDate.now());
        oferta.setActiva(true);

        // Vinculo la oferta con el empleador
        oferta.setEmpleador(empleador);

        return ofertaLaboralRepository.save(oferta);
    }
    // este es el metodo para ver las ofertas publicadas
    @GetMapping
    public List<OfertaLaboral> obtenerTodas() {
        return ofertaLaboralRepository.findAll();
    }
    //aca voy a implementar lo de la baja logica
    @PutMapping("/baja/{id}")
    public OfertaLaboral darDeBaja(@PathVariable Long id){
        //busco la oderta por el id que tiene
        OfertaLaboral oferta = ofertaLaboralRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("oferta laboral no encontrada"));
        //aca es donde pasa la baja logica porque le cambio el estado en vez de borrarla
        oferta.setActiva(false);
        return ofertaLaboralRepository.save(oferta);
    }
}
