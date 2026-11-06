package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.LoginDTO;
import com.acompanAR.backend.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.usuarioService = usuarioService;
    }

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

            return ResponseEntity.ok(usuarioService.obtenerRespuesta(authentication.getName()));
        } catch (AuthenticationException e) {
            // Mismo mensaje para usuario inexistente, contraseña mala o usuario dado de baja
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("error: usuario o contraseña incorrectos");
        }
    }

    // Devuelve los datos del usuario logueado (el front lo usa para saber si hay sesión activa)
    @GetMapping("/me")
    public ResponseEntity<?> usuarioActual(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.obtenerRespuesta(authentication.getName()));
    }
}
