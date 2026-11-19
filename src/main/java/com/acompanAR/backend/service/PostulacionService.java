package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.PostulacionRegistroDTO;
import com.acompanAR.backend.dto.ProgresoTrabajoDTO;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoPostulacion;
import com.acompanAR.backend.entity.OfertaLaboral;
import com.acompanAR.backend.entity.Postulacion;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.OfertaLaboralRepository;
import com.acompanAR.backend.repository.PostulacionRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import com.acompanAR.backend.security.Permisos;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio de las postulaciones: un profesional se postula a una oferta,
 * el empleador la acepta o rechaza, y el profesional puede cancelarla.
 */
@Service
public class PostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final ProfesionalRepository profesionalRepository;
    private final OfertaLaboralRepository ofertaLaboralRepository;
    private final NotificacionService notificacionService;

    public PostulacionService(PostulacionRepository postulacionRepository,
                              ProfesionalRepository profesionalRepository,
                              OfertaLaboralRepository ofertaLaboralRepository,
                              NotificacionService notificacionService) {
        this.postulacionRepository = postulacionRepository;
        this.profesionalRepository = profesionalRepository;
        this.ofertaLaboralRepository = ofertaLaboralRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public Postulacion crear(PostulacionRegistroDTO dto, Authentication authentication) {
        Profesional profesional = buscarProfesional(dto.getIdProfesional());
        // Solo podés postularte a nombre tuyo
        verificarProfesional(profesional, authentication);

        OfertaLaboral oferta = buscarOferta(dto.getIdOfertaLaboral());

        // No se puede postular a una oferta que fue dada de baja
        if (oferta.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La oferta laboral ya no está activa");
        }
        // No se puede postular dos veces a la misma oferta
        if (postulacionRepository.existsByProfesionalIdAndOfertaLaboralId(profesional.getId(), oferta.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya te postulaste a esta oferta");
        }

        Postulacion postulacion = new Postulacion();
        postulacion.setProfesional(profesional);
        postulacion.setOfertaLaboral(oferta);
        postulacion.setFechaPostulacion(LocalDate.now());
        postulacion.setEstado(EstadoPostulacion.PENDIENTE); // toda postulación nueva arranca pendiente
        postulacion = postulacionRepository.save(postulacion);

        // Aviso para el empleador dueño de la oferta
        notificacionService.notificar(oferta.getEmpleador().getUsuario(),
                profesional.nombreCompleto() + " se postuló a tu oferta «" + oferta.getTitulo() + "»",
                "postulantes.html?oferta=" + oferta.getId());
        return postulacion;
    }

    // Listado completo: solo para administradores (ver SecurityConfig)
    public List<Postulacion> listarTodas() {
        return postulacionRepository.findAll();
    }

    // El empleador consulta quiénes se postularon a SU oferta
    public List<Postulacion> listarPorOferta(Long idOferta, Authentication authentication) {
        OfertaLaboral oferta = buscarOferta(idOferta);
        verificarEmpleador(oferta, authentication);
        return postulacionRepository.findByOfertaLaboralId(idOferta);
    }

    // El profesional consulta sus propias postulaciones
    public List<Postulacion> listarPorProfesional(Long idProfesional, Authentication authentication) {
        Profesional profesional = buscarProfesional(idProfesional);
        verificarProfesional(profesional, authentication);
        return postulacionRepository.findByProfesionalId(idProfesional);
    }

    // Progreso del trabajo: lo pueden ver las dos partes de una postulación ACEPTADA o FINALIZADA
    public ProgresoTrabajoDTO progreso(Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);

        boolean esElProfesional = Permisos.esElMismoUsuario(
                postulacion.getProfesional().getUsuario().getNombreUsuario(), authentication);
        boolean esElEmpleador = Permisos.esElMismoUsuario(
                postulacion.getOfertaLaboral().getEmpleador().getUsuario().getNombreUsuario(), authentication);
        if (!esElProfesional && !esElEmpleador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No participaste en esta postulación");
        }
        if (postulacion.getEstado() != EstadoPostulacion.ACEPTADA && postulacion.getEstado() != EstadoPostulacion.FINALIZADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El progreso solo existe cuando la postulación fue aceptada");
        }

        OfertaLaboral oferta = postulacion.getOfertaLaboral();
        ProgresoTrabajoDTO progreso = CalculadorProgreso.calcular(
                oferta.getFechaInicio(), oferta.getDuracionSemanas(), LocalDate.now());
        progreso.setTituloOferta(oferta.getTitulo());
        return progreso;
    }

    // El empleador acepta la postulación
    @Transactional
    public Postulacion aceptar(Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);
        verificarEmpleador(postulacion.getOfertaLaboral(), authentication);
        Postulacion aceptada = cambiarEstado(postulacion, EstadoPostulacion.ACEPTADA);
        avisarAlProfesional(aceptada, "¡Te aceptaron en la oferta «%s»! Ya podés ver el contacto y el progreso.");
        return aceptada;
    }

    // El empleador rechaza la postulación
    @Transactional
    public Postulacion rechazar(Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);
        verificarEmpleador(postulacion.getOfertaLaboral(), authentication);
        Postulacion rechazada = cambiarEstado(postulacion, EstadoPostulacion.RECHAZADA);
        avisarAlProfesional(rechazada, "Tu postulación a la oferta «%s» no fue seleccionada.");
        return rechazada;
    }

    // El profesional cancela su postulación (baja lógica: el registro queda, con estado CANCELADA)
    @Transactional
    public Postulacion cancelar(Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);
        verificarProfesional(postulacion.getProfesional(), authentication);
        return cambiarEstado(postulacion, EstadoPostulacion.CANCELADA);
    }

    // Cualquiera de las dos partes da por terminado el trabajo. Solo se puede finalizar una postulación ACEPTADA.
    // Desde ese momento se habilita la calificación mutua y la conversación queda solo de lectura.
    @Transactional
    public Postulacion finalizar(Long id, Authentication authentication) {
        Postulacion postulacion = buscarPostulacion(id);
        Usuario usuarioProfesional = postulacion.getProfesional().getUsuario();
        Usuario usuarioEmpleador = postulacion.getOfertaLaboral().getEmpleador().getUsuario();

        boolean esElProfesional = Permisos.esElMismoUsuario(usuarioProfesional.getNombreUsuario(), authentication);
        boolean esElEmpleador = Permisos.esElMismoUsuario(usuarioEmpleador.getNombreUsuario(), authentication);
        if (!esElProfesional && !esElEmpleador) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No participaste en esta postulación");
        }
        if (postulacion.getEstado() != EstadoPostulacion.ACEPTADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede finalizar un trabajo aceptado. Estado actual: " + postulacion.getEstado());
        }

        postulacion.setEstado(EstadoPostulacion.FINALIZADA);
        postulacion = postulacionRepository.save(postulacion);

        // Aviso para la otra parte
        String titulo = postulacion.getOfertaLaboral().getTitulo();
        if (esElProfesional) {
            notificacionService.notificar(usuarioEmpleador,
                    postulacion.getProfesional().nombreCompleto() + " dio por finalizado el trabajo «" + titulo + "». Ya podés calificar.",
                    "postulantes.html?oferta=" + postulacion.getOfertaLaboral().getId());
        } else {
            notificacionService.notificar(usuarioProfesional,
                    "El trabajo «" + titulo + "» fue dado por finalizado. Ya podés calificar al empleador.",
                    "mis-postulaciones.html");
        }
        return postulacion;
    }

    // Aviso para el profesional cuando el empleador responde su postulación
    private void avisarAlProfesional(Postulacion postulacion, String plantilla) {
        notificacionService.notificar(postulacion.getProfesional().getUsuario(),
                String.format(plantilla, postulacion.getOfertaLaboral().getTitulo()),
                "mis-postulaciones.html");
    }

    // Regla de negocio: solo una postulación PENDIENTE puede pasar a otro estado.
    // Una vez aceptada, rechazada o cancelada, queda cerrada.
    private Postulacion cambiarEstado(Postulacion postulacion, EstadoPostulacion nuevoEstado) {
        if (postulacion.getEstado() != EstadoPostulacion.PENDIENTE) {
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

    private Profesional buscarProfesional(Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
    }

    // 403 si la oferta no pertenece al empleador logueado
    private void verificarEmpleador(OfertaLaboral oferta, Authentication authentication) {
        if (!Permisos.esElMismoUsuario(oferta.getEmpleador().getUsuario().getNombreUsuario(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta oferta no es tuya");
        }
    }

    // 403 si el profesional no es el usuario logueado
    private void verificarProfesional(Profesional profesional, Authentication authentication) {
        if (!Permisos.esElMismoUsuario(profesional.getUsuario().getNombreUsuario(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permiso sobre los datos de otro profesional");
        }
    }
}
