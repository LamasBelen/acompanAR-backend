package com.acompanAR.backend.config;

import com.acompanAR.backend.entity.Estado;
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
 * Usuario y contraseña NO están escritos en el código: se leen de las variables de entorno
 * ADMIN_USUARIO y ADMIN_CONTRASENA (ver application.properties).
 * Si no están definidas, la aplicación no arranca y avisa cuál falta.
 */
@Configuration
public class AdminInicial {
/*el @Bean lo use para anotación que se coloca sobre un método para indicarle al framework que el objeto devuelto por dicho método debe ser registrado, instanciado y gestionado por el contenedor de Spring*/
    @Bean
    CommandLineRunner crearAdministrador(UsuarioRepository usuarioRepository,
                                         PasswordEncoder passwordEncoder,
                                         @Value("${app.admin.usuario}") String nombreUsuario,
                                         @Value("${app.admin.contrasena}") String contrasena) {
        return args -> {
            if (usuarioRepository.existsByNombreUsuario(nombreUsuario)) {
                return; // ya existe, no hacemos nada
            }
            Usuario admin = new Usuario();
            admin.setNombreUsuario(nombreUsuario);
            admin.setContrasena(passwordEncoder.encode(contrasena)); // se guarda hasheada
            admin.setRol(Rol.ADMINISTRADOR);
            admin.setEstado(Estado.ACTIVO);
            usuarioRepository.save(admin);
            System.out.println(">>> Usuario administrador creado: " + nombreUsuario);
        };
    }
}
