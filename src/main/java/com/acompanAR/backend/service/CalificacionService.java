package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.CalificacionRegistroDTO;
import com.acompanAR.backend.dto.CalificacionRespuestaDTO;
import com.acompanAR.backend.dto.ReputacionDTO;
import com.acompanAR.backend.entity.Calificacion;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoPostulacion;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Postulacion;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.CalificacionRepository;
import com.acompanAR.backend.repository.PostulacionRepository;
import com.acompanAR.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Calificación mutua entre profesional y empleador.
 * Regla de negocio: solo se puede calificar cuando la postulación fue ACEPTADA,
 * solo participan las dos partes de esa postulación, y cada una califica una sola vez.
 */
@Service
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final PostulacionRepository postulacionRepository;
    private final UsuarioRepository usuarioRepository;

    public CalificacionService(CalificacionRepository calificacionRepository,
                               PostulacionRepository postulacionRepository,
                               UsuarioRepository usuarioRepository) {
        this.calificacionRepository = calificacionRepository;
        this.postulacionRepository = postulacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public CalificacionRespuestaDTO calificar(CalificacionRegistroDTO dto, Authentication authentication) {
        Postulacion postulacion = postulacionRepository.findById(dto.getIdPostulacion())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Postulación no encontrada"));

        Usuario autor = usuarioRepository.findByNombreUsuario(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Usuario usuarioProfesional = postulacion.getProfesional().getUsuario();
        Usuario usuarioEmpleador = postulacion.getOfertaLaboral().getEmpleador().getUsuario();

        // Quién es el destinatario depende de quién califica; si no participa de la postulación, no puede
        Usuario destinatario;
        if (autor.getId().equals(usuarioProfesional.getId())) {
            destinatario = usuarioEmpleador;
        } else if (autor.getId().equals(usuarioEmpleador.getId())) {
            destinatario = usuarioProfesional;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No participaste en esta postulación");
        }

        if (postulacion.getEstado() != EstadoPostulacion.ACEPTADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede calificar cuando la postulación fue aceptada");
        }
        if (calificacionRepository.existsByPostulacionIdAndAutorId(postulacion.getId(), autor.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya calificaste esta postulación");
        }

        Calificacion calificacion = new Calificacion();
        calificacion.setPostulacion(postulacion);
        calificacion.setAutor(autor);
        calificacion.setDestinatario(destinatario);
        calificacion.setPuntaje(dto.getPuntaje());
        calificacion.setComentario(dto.getComentario() == null || dto.getComentario().isBlank()
                ? null : dto.getComentario().trim());
        calificacion.setFecha(LocalDate.now());
        calificacion.setEstado(Estado.ACTIVO);
        return aDTO(calificacionRepository.save(calificacion));
    }

    // Ids de las postulaciones que el usuario logueado ya calificó (el front los usa para ocultar el botón)
    public List<Long> postulacionesCalificadas(Authentication authentication) {
        Usuario autor = usuarioRepository.findByNombreUsuario(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        return calificacionRepository.findByAutorIdAndEstado(autor.getId(), Estado.ACTIVO).stream()
                .map(c -> c.getPostulacion().getId())
                .toList();
    }

    // Reputación de un usuario: promedio (1 decimal) y detalle de las calificaciones que recibió
    public ReputacionDTO reputacionDe(Long idUsuario) {
        List<Calificacion> recibidas = calificacionRepository.findByDestinatarioIdAndEstado(idUsuario, Estado.ACTIVO);

        ReputacionDTO reputacion = new ReputacionDTO();
        reputacion.setCantidad(recibidas.size());
        reputacion.setCalificaciones(recibidas.stream().map(this::aDTO).toList());
        if (!recibidas.isEmpty()) {
            double promedio = recibidas.stream().mapToInt(Calificacion::getPuntaje).average().orElse(0);
            reputacion.setPromedio(Math.round(promedio * 10) / 10.0);
        }
        return reputacion;
    }

    // Baja lógica (moderación): el administrador oculta una calificación inapropiada
    @Transactional
    public CalificacionRespuestaDTO darDeBaja(Long id) {
        Calificacion calificacion = calificacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calificación no encontrada"));
        if (calificacion.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La calificación ya está dada de baja");
        }
        calificacion.setEstado(Estado.INACTIVO);
        return aDTO(calificacionRepository.save(calificacion));
    }

    private CalificacionRespuestaDTO aDTO(Calificacion calificacion) {
        CalificacionRespuestaDTO dto = new CalificacionRespuestaDTO();
        dto.setId(calificacion.getId());
        dto.setPuntaje(calificacion.getPuntaje());
        dto.setComentario(calificacion.getComentario());
        dto.setFecha(calificacion.getFecha());
        dto.setTituloOferta(calificacion.getPostulacion().getOfertaLaboral().getTitulo());
        Persona persona = calificacion.getAutor().getPersona();
        dto.setNombreAutor(persona != null
                ? persona.getNombre() + " " + persona.getApellido()
                : calificacion.getAutor().getNombreUsuario());
        return dto;
    }
}
