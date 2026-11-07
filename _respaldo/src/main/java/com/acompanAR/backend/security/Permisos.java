package com.acompanAR.backend.security;

import org.springframework.security.core.Authentication;

/**
 * Pequeñas funciones para preguntar "¿quién es el usuario logueado?" en los controllers.
 */
public final class Permisos {

    private Permisos() {
        // clase de utilidades: no se instancia
    }

    // true si el usuario logueado tiene rol ADMINISTRADOR
    public static boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    // true si el nombre de usuario recibido es el del usuario logueado
    public static boolean esElMismoUsuario(String nombreUsuario, Authentication authentication) {
        return nombreUsuario.equals(authentication.getName());
    }
}