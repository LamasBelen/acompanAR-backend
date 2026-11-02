package com.acompanAR.backend.config;

import com.acompanAR.backend.entity.Rol;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Al arrancar la aplicación, crea un usuario ADMINISTRADOR si todavía no existe.
 * Es necesario porque no hay ninguna pantalla ni endpoint para crear administradores.
 *
 * Usuario y contraseña se pueden cambiar en application.properties:
 *   app.admin.usuario=admin
 *   app.admin.contrasena=otraClave
 * o con las variables de entorno ADMIN_USUARIO y ADMIN_CONTRASENA.
 */
@Configuration
public class AdminInicial {

    @Bean
    CommandLineRunner crearAdministrador(UsuarioRepository usuarioRepository,
                                         PasswordEncoder passwordEncoder,
                                         @Value("${app.admin.usuario:${ADMIN_USUARIO:admin}}") String nombreUsuario,
                                         @Value("${app.admin.contrasena:${ADMIN_CONTRASENA:admin1234}}") String contrasena) {
        return args -> {
            if (usuarioRepository.existsByNombreUsuario(nombreUsuario)) {
                return; // ya existe, no hacemos nada
            }
            Usuario admin = new Usuario();
            admin.setNombreUsuario(nombreUsuario);
            admin.setContrasena(passwordEncoder.encode(contrasena)); // se guarda hasheada
            admin.setRol(Rol.ADMINISTRADOR);
            admin.setEstado(true);
            usuarioRepository.save(admin);
            System.out.println(">>> Usuario administrador creado: " + nombreUsuario);
        };
    }
}


