package com.acompanAR.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Es el "motor" que verifica usuario y contraseña. Lo usa AuthController en el login.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Guarda la sesión autenticada en la HttpSession (la cookie JSESSIONID que recibe el navegador)
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                .cors(Customizer.withDefaults()) // usa el bean corsConfigurationSource de CorsConfig
                // CSRF desactivado para simplificar el desarrollo con un front separado.
                // En producción convendría activarlo (o usar tokens JWT).
                .csrf(csrf -> csrf.disable())
                .securityContext(sc -> sc.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        // ---- Páginas del front (carpeta static): se pueden abrir sin login.
                        // Cada página protegida pide la sesión con JavaScript, y los DATOS
                        // siguen protegidos porque la API (/api/**) sí exige login. ----
                        .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()

                        // ---- Rutas públicas de la API (sin login) ----
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/profesionales/registro").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/empleadores/registro").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight de CORS

                        // ---- Solo PROFESIONAL ----
                        .requestMatchers(HttpMethod.POST, "/api/postulaciones/nueva").hasRole("PROFESIONAL")
                        .requestMatchers(HttpMethod.PUT, "/api/postulaciones/cancelar/**").hasRole("PROFESIONAL")
                        .requestMatchers(HttpMethod.GET, "/api/postulaciones/profesional/**").hasRole("PROFESIONAL")

                        // ---- Postulaciones: el empleador las ve y responde ----
                        .requestMatchers(HttpMethod.PUT, "/api/postulaciones/aceptar/**", "/api/postulaciones/rechazar/**").hasRole("EMPLEADOR")
                        .requestMatchers(HttpMethod.GET, "/api/postulaciones/oferta/**").hasRole("EMPLEADOR")
                        .requestMatchers(HttpMethod.POST, "/api/inscripciones/nueva").hasRole("PROFESIONAL")
                        .requestMatchers(HttpMethod.PUT, "/api/inscripciones/finalizar/**", "/api/inscripciones/cancelar/**").hasRole("PROFESIONAL")
                        .requestMatchers(HttpMethod.GET, "/api/inscripciones/profesional/**").hasRole("PROFESIONAL")
                        .requestMatchers(HttpMethod.PUT, "/api/profesionales/editar/**").hasRole("PROFESIONAL")
                        .requestMatchers(HttpMethod.PUT, "/api/profesionales/baja/**").hasAnyRole("PROFESIONAL", "ADMINISTRADOR")

                        // ---- Solo EMPLEADOR ----
                        .requestMatchers(HttpMethod.POST, "/api/ofertas/publicar").hasRole("EMPLEADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/ofertas/baja/**").hasAnyRole("EMPLEADOR", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/ofertas/editar/**").hasRole("EMPLEADOR")
                        .requestMatchers(HttpMethod.GET, "/api/ofertas/empleador/**").hasRole("EMPLEADOR")

                        // ---- Listado completo de postulaciones: solo ADMINISTRADOR ----
                        .requestMatchers(HttpMethod.GET, "/api/postulaciones").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/empleadores/editar/**").hasRole("EMPLEADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/empleadores/baja/**").hasAnyRole("EMPLEADOR", "ADMINISTRADOR")

                        // ---- Solo ADMINISTRADOR (por ahora quien publica capacitaciones) ----
                        .requestMatchers(HttpMethod.POST, "/api/capacitaciones/nueva").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/capacitaciones/baja/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/capacitaciones/editar/**").hasRole("ADMINISTRADOR")

                        // ---- Consulta del listado de profesionales: empleadores y admin ----
                        .requestMatchers(HttpMethod.GET, "/api/profesionales").hasAnyRole("EMPLEADOR", "ADMINISTRADOR")

                        // ---- Listado de empleadores: profesionales y admin ----
                        .requestMatchers(HttpMethod.GET, "/api/empleadores").hasAnyRole("PROFESIONAL", "ADMINISTRADOR")

                        // ---- Todo lo demás requiere estar logueado ----
                        .anyRequest().authenticated()
                )
                // Si no hay sesión, responde 401 (en vez del 403 por defecto)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Cerrar sesión: POST /api/auth/logout
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value()))
                        .deleteCookies("JSESSIONID")
                );
        return http.build();
    }
}