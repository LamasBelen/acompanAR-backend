package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.UsuarioRespuestaDTO;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.EmpleadorRepository;
import com.acompanAR.backend.repository.InstitucionRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import com.acompanAR.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Arma los datos del usuario logueado que le devolvemos al front (sin exponer la contraseña).
 * El login en sí (sesión, cookie) queda en AuthController porque depende de HTTP.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionalRepository;
    private final EmpleadorRepository empleadorRepository;
    private final InstitucionRepository institucionRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          ProfesionalRepository profesionalRepository,
                          EmpleadorRepository empleadorRepository,
                          InstitucionRepository institucionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.profesionalRepository = profesionalRepository;
        this.empleadorRepository = empleadorRepository;
        this.institucionRepository = institucionRepository;
    }

    // Datos del usuario a partir de su nombre de usuario
    public UsuarioRespuestaDTO obtenerRespuesta(String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        UsuarioRespuestaDTO respuesta = new UsuarioRespuestaDTO();
        respuesta.setId(usuario.getId());
        respuesta.setNombreUsuario(usuario.getNombreUsuario());
        respuesta.setRol(usuario.getRol());

        Persona persona = usuario.getPersona();
        if (persona != null) {
            respuesta.setNombre(persona.getNombre());
            respuesta.setApellido(persona.getApellido());
        }

        profesionalRepository.findByUsuarioId(usuario.getId())
                .ifPresent(p -> respuesta.setIdProfesional(p.getId()));
        empleadorRepository.findByUsuarioId(usuario.getId())
                .ifPresent(e -> respuesta.setIdEmpleador(e.getId()));
        institucionRepository.findByUsuarioId(usuario.getId())
                .ifPresent(i -> respuesta.setIdInstitucion(i.getId()));
        return respuesta;
    }
}
