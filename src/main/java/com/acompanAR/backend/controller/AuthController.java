package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.LoginDTO;
import com.acompanAR.backend.dto.UsuarioRespuestaDTO;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.EmpleadorRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import com.acompanAR.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProfesionalRepository profesionalRepository;
    @Autowired
    private EmpleadorRepository empleadorRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private SecurityContextRepository securityContextRepository;

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@Valid @RequestBody LoginDTO dto,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        try {
            // Spring verifica usuario, contraseña (BCrypt) y que el usuario esté activo
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getNombreUsuario(), dto.getContrasena()));

            // Si había una sesión anterior, se cambia su id (evita ataques de "session fixation")
            if (request.getSession(false) != null) {
                request.changeSessionId();
            }

            // Se guarda la autenticación en la sesión: a partir de acá el navegador
            // manda la cookie JSESSIONID y el usuario queda logueado
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            Usuario usuario = usuarioRepository.findByNombreUsuario(authentication.getName()).orElseThrow();
            return ResponseEntity.ok(armarRespuesta(usuario));
        } catch (AuthenticationException e) {
            // Mismo mensaje para usuario inexistente, contraseña mala o usuario dado de baja
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("error: usuario o contraseña incorrectos");
        }
    }

    // Devuelve los datos del usuario logueado (el front lo usa para saber si hay sesión activa)
    @GetMapping("/me")
    public ResponseEntity<?> usuarioActual(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        return ResponseEntity.ok(armarRespuesta(usuario));
    }

    // Arma el DTO de respuesta sin exponer la contraseña
    private UsuarioRespuestaDTO armarRespuesta(Usuario usuario) {
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
        return respuesta;
    }
}
