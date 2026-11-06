package com.acompanAR.backend.controller;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.dto.OfertaLaboralEdicionDTO;
import com.acompanAR.backend.dto.OfertaLaboralRegistroDTO;
import com.acompanAR.backend.entity.Empleador;
import com.acompanAR.backend.entity.OfertaLaboral;
import com.acompanAR.backend.repository.EmpleadorRepository;
import com.acompanAR.backend.repository.OfertaLaboralRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
    public OfertaLaboral publicarOferta(@Valid @RequestBody OfertaLaboralRegistroDTO dto, Authentication authentication){
        //aca se busca al empleador en la base de datos usando el id que mando
        Empleador empleador = empleadorRepository.findById(dto.getIdEmpleador())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleador no encontrado"));

        // Un empleador solo puede publicar ofertas a su propio nombre
        verificarDuenio(empleador, authentication);

        //aca se crea la oferta vacia y se empieza a llenar
        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setTitulo(dto.getTitulo());
        oferta.setDescripcion(dto.getDescripcion());
        oferta.setRequisitos(dto.getRequisitos());

        // La fecha y el estado los ponemos automáticamente por código, no se los pedimos al usuario
        oferta.setFechaPublicacion(LocalDate.now());
        oferta.setEstado(Estado.ACTIVO);

        // Vinculo la oferta con el empleador
        oferta.setEmpleador(empleador);

        return ofertaLaboralRepository.save(oferta);
    }

    // este es el metodo para ver las ofertas publicadas (solo las activas)
    @GetMapping
    public List<OfertaLaboral> obtenerTodas() {
        return ofertaLaboralRepository.findByEstado(Estado.ACTIVO);
    }

    // Ver una oferta puntual (se usa, por ejemplo, para cargar el formulario de edición)
    @GetMapping("/{id}")
    public OfertaLaboral obtenerPorId(@PathVariable Long id) {
        return ofertaLaboralRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta laboral no encontrada"));
    }

    // Las ofertas activas de un empleador (solo él puede consultarlas)
    @GetMapping("/empleador/{idEmpleador}")
    public List<OfertaLaboral> porEmpleador(@PathVariable Long idEmpleador, Authentication authentication) {
        Empleador empleador = empleadorRepository.findById(idEmpleador)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleador no encontrado"));
        verificarDuenio(empleador, authentication);
        return ofertaLaboralRepository.findByEmpleadorIdAndEstado(idEmpleador, Estado.ACTIVO);
    }

    // Modificar una oferta ya publicada (solo su dueño)
    @PutMapping("/editar/{id}")
    public OfertaLaboral editarOferta(@PathVariable Long id,
                                      @Valid @RequestBody OfertaLaboralEdicionDTO dto,
                                      Authentication authentication) {
        OfertaLaboral oferta = ofertaLaboralRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta laboral no encontrada"));

        // Solo el empleador que publicó la oferta puede modificarla
        verificarDuenio(oferta.getEmpleador(), authentication);

        // No tiene sentido modificar una oferta dada de baja
        if (oferta.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La oferta ya fue dada de baja");
        }

        oferta.setTitulo(dto.getTitulo());
        oferta.setDescripcion(dto.getDescripcion());
        oferta.setRequisitos(dto.getRequisitos());
        return ofertaLaboralRepository.save(oferta);
    }

    //aca pasa la baja logica
    @PutMapping("/baja/{id}")
    public OfertaLaboral darDeBaja(@PathVariable Long id, Authentication authentication){
        //busco la oferta por el id que tiene
        OfertaLaboral oferta = ofertaLaboralRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta laboral no encontrada"));

        // La puede dar de baja su dueño o un administrador
        if (!esAdministrador(authentication)) {
            verificarDuenio(oferta.getEmpleador(), authentication);
        }

        //aca es donde pasa la baja logica porque le cambio el estado en vez de borrarla
        oferta.setEstado(Estado.INACTIVO);
        return ofertaLaboralRepository.save(oferta);
    }

    // Lanza 403 si el empleador no es el usuario logueado
    private void verificarDuenio(Empleador empleador, Authentication authentication) {
        if (!empleador.getUsuario().getNombreUsuario().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permiso sobre los datos de otro empleador");
        }
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }
}
