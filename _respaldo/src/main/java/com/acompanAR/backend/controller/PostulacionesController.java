package com.acompanAR.backend.controller;
import com.acompanAR.backend.dto.PostulacionRegistroDTO;
import com.acompanAR.backend.entity.Estadopostulacion;
import com.acompanAR.backend.entity.OfertaLaboral;
import com.acompanAR.backend.entity.Postulacion;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.repository.OfertaLaboralRepository;
import com.acompanAR.backend.repository.PostulacionRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public Postulacion crearPostulacion(@Valid @RequestBody PostulacionRegistroDTO dto, Authentication authentication){
        //primero buscamos que el profesional exista
        Profesional profesional = profesionalRepository.findById(dto.getIdProfesional())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
        // Solo podés postularte a nombre tuyo
        verificarProfesional(profesional, authentication);

        //ahora buscamos que exista la oferta
        OfertaLaboral oferta = ofertaLaboralRepository.findById(dto.getIdOfertaLaboral())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta laboral no encontrada"));

        // No se puede postular a una oferta que fue dada de baja
        if (!oferta.isActiva()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La oferta laboral ya no está activa");
        }
        // No se puede postular dos veces a la misma oferta
        if (postulacionRepository.existsByProfesionalIdAndOfertaLaboralId(profesional.getId(), oferta.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya te postulaste a esta oferta");
        }

        //se crea la postulacion
        Postulacion postulacion = new Postulacion();
        postulacion.setProfesional(profesional);
        postulacion.setOfertaLaboral(oferta);
        postulacion.setFechaPostulacion(LocalDate.now());
        postulacion.setEstado(Estadopostulacion.PENDIENTE); // toda postulación nueva arranca pendiente
        return postulacionRepository.save(postulacion);
    }

    // Listado completo: solo para administradores (ver SecurityConfig)
    @GetMapping
    public List<Postulacion> obtenerTodas(){
        return postulacionRepository.findAll();
    }

    // El empleador consulta quiénes se postularon a SU oferta
    @GetMapping("/oferta/{idOferta}")
    public List<Postulacion> porOferta(@PathVariable Long idOferta, Authentication authentication) {
        OfertaLaboral oferta = buscarOferta(idOferta);
        verificarEmpleador(oferta, authentication);
        return postulacionRepository.findByOfertaLaboralId(idOferta);
    }

    // El profesional consulta sus propias postulaciones
    @GetMapping("/profesional/{idProfesional}")
    public List<Postulacion> porProfesional(@PathVariable Long idProfesional, Authentication authentication) {
        Profesional profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
        verificarProfesional(profesional, authentication);
        return postulacionRepository.findByProfesionalId(idProfesional);
    }

    // El empleador acepta la postulación
    @PutMapping("/aceptar/{id}")
    public Postulacion aceptar(@PathVariable Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);
        verificarEmpleador(postulacion.getOfertaLaboral(), authentication);
        return cambiarEstado(postulacion, Estadopostulacion.ACEPTADA);
    }

    // El empleador rechaza la postulación
    @PutMapping("/rechazar/{id}")
    public Postulacion rechazar(@PathVariable Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);
        verificarEmpleador(postulacion.getOfertaLaboral(), authentication);
        return cambiarEstado(postulacion, Estadopostulacion.RECHAZADA);
    }

    // El profesional cancela su postulación (baja lógica: el registro queda, con estado CANCELADA)
    @PutMapping("/cancelar/{id}")
    public Postulacion cancelar(@PathVariable Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);
        verificarProfesional(postulacion.getProfesional(), authentication);
        return cambiarEstado(postulacion, Estadopostulacion.CANCELADA);
    }

    // Regla de negocio: solo una postulación PENDIENTE puede pasar a otro estado.
    // Una vez aceptada, rechazada o cancelada, queda cerrada.
    private Postulacion cambiarEstado(Postulacion postulacion, Estadopostulacion nuevoEstado) {
        if (postulacion.getEstado() != Estadopostulacion.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede modificar una postulación pendiente. Estado actual: " + postulacion.getEstado());
        }
        postulacion.setEstado(nuevoEstado);
        return postulacionRepository.save(postulacion);
    }

    private Postulacion buscarPostulacion(Long id) {
        return postulacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Postulación no encontrada"));
    }

    private OfertaLaboral buscarOferta(Long id) {
        return ofertaLaboralRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta laboral no encontrada"));
    }

    // 403 si la oferta no pertenece al empleador logueado
    private void verificarEmpleador(OfertaLaboral oferta, Authentication authentication) {
        if (!oferta.getEmpleador().getUsuario().getNombreUsuario().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta oferta no es tuya");
        }
    }

    // 403 si el profesional no es el usuario logueado
    private void verificarProfesional(Profesional profesional, Authentication authentication) {
        if (!profesional.getUsuario().getNombreUsuario().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permiso sobre los datos de otro profesional");
        }
    }
}