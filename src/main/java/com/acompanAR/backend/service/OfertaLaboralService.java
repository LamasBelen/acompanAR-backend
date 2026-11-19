package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.OfertaLaboralEdicionDTO;
import com.acompanAR.backend.dto.OfertaLaboralRegistroDTO;
import com.acompanAR.backend.entity.Empleador;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.OfertaLaboral;
import com.acompanAR.backend.repository.EmpleadorRepository;
import com.acompanAR.backend.repository.OfertaLaboralRepository;
import com.acompanAR.backend.security.Permisos;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio de las ofertas laborales: publicar, editar, listar y baja lógica.
 */
@Service
public class OfertaLaboralService {

    private final OfertaLaboralRepository ofertaLaboralRepository;
    private final EmpleadorRepository empleadorRepository;
    private final RequisitoService requisitoService;

    public OfertaLaboralService(OfertaLaboralRepository ofertaLaboralRepository,
                                EmpleadorRepository empleadorRepository,
                                RequisitoService requisitoService) {
        this.ofertaLaboralRepository = ofertaLaboralRepository;
        this.empleadorRepository = empleadorRepository;
        this.requisitoService = requisitoService;
    }

    // Regla: la fecha de inicio y la duración van juntas (o no se carga ninguna) y la fecha no puede ser pasada
    private void validarPeriodo(LocalDate fechaInicio, Integer duracionSemanas, boolean esNueva) {
        if ((fechaInicio == null) != (duracionSemanas == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Para mostrar el progreso hay que indicar la fecha de inicio y la duración en semanas (o dejar ambas vacías)");
        }
        if (esNueva && fechaInicio != null && fechaInicio.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de inicio no puede ser anterior a hoy");
        }
    }

    private Empleador buscarEmpleador(Long id) {
        return empleadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleador no encontrado"));
    }

    // Lanza 403 si el empleador no es el usuario logueado
    private void verificarDuenio(Empleador empleador, Authentication authentication) {
        if (!Permisos.esElMismoUsuario(empleador.getUsuario().getNombreUsuario(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permiso sobre los datos de otro empleador");
        }
    }

    @Transactional
    public OfertaLaboral publicar(OfertaLaboralRegistroDTO dto, Authentication authentication) {
        Empleador empleador = buscarEmpleador(dto.getIdEmpleador());
        validarPeriodo(dto.getFechaInicio(), dto.getDuracionSemanas(), true);

        // Un empleador solo puede publicar ofertas a su propio nombre
        verificarDuenio(empleador, authentication);

        OfertaLaboral oferta = new OfertaLaboral();
        oferta.setTitulo(dto.getTitulo());
        oferta.setDescripcion(dto.getDescripcion());
        oferta.setRequisitos(dto.getRequisitos());
        oferta.setRequisitosComunes(requisitoService.buscarActivos(dto.getIdsRequisitos()));
        oferta.setFechaInicio(dto.getFechaInicio());
        oferta.setDuracionSemanas(dto.getDuracionSemanas());
        // La fecha y el estado los pone el sistema, no el usuario
        oferta.setFechaPublicacion(LocalDate.now());
        oferta.setEstado(Estado.ACTIVO);
        oferta.setEmpleador(empleador);
        return ofertaLaboralRepository.save(oferta);
    }

    // Solo las ofertas activas
    public List<OfertaLaboral> listarActivas() {
        return ofertaLaboralRepository.findByEstado(Estado.ACTIVO);
    }

    public OfertaLaboral obtenerPorId(Long id) {
        return ofertaLaboralRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta laboral no encontrada"));
    }

    // Las ofertas activas de un empleador (solo él puede consultarlas)
    public List<OfertaLaboral> listarPorEmpleador(Long idEmpleador, Authentication authentication) {
        Empleador empleador = buscarEmpleador(idEmpleador);
        verificarDuenio(empleador, authentication);
        return ofertaLaboralRepository.findByEmpleadorIdAndEstado(idEmpleador, Estado.ACTIVO);
    }

    @Transactional
    public OfertaLaboral editar(Long id, OfertaLaboralEdicionDTO dto, Authentication authentication) {
        OfertaLaboral oferta = obtenerPorId(id);

        // Solo el empleador que publicó la oferta puede modificarla
        verificarDuenio(oferta.getEmpleador(), authentication);

        // No tiene sentido modificar una oferta dada de baja
        if (oferta.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La oferta ya fue dada de baja");
        }

        oferta.setTitulo(dto.getTitulo());
        oferta.setDescripcion(dto.getDescripcion());
        oferta.setRequisitos(dto.getRequisitos());
        // Al editar se permite mantener una fecha que ya pasó (el trabajo puede estar en curso)
        validarPeriodo(dto.getFechaInicio(), dto.getDuracionSemanas(), false);
        oferta.setRequisitosComunes(requisitoService.buscarActivos(dto.getIdsRequisitos()));
        oferta.setFechaInicio(dto.getFechaInicio());
        oferta.setDuracionSemanas(dto.getDuracionSemanas());
        return ofertaLaboralRepository.save(oferta);
    }

    // Baja lógica: no se borra, se cambia el estado. La puede hacer su dueño o un administrador.
    @Transactional
    public OfertaLaboral darDeBaja(Long id, Authentication authentication) {
        OfertaLaboral oferta = obtenerPorId(id);

        if (!Permisos.esAdministrador(authentication)) {
            verificarDuenio(oferta.getEmpleador(), authentication);
        }

        oferta.setEstado(Estado.INACTIVO);
        return ofertaLaboralRepository.save(oferta);
    }
}
