package com.acompanAR.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(org.springframework.security.config.Customizer.withDefaults()) // <-- LÍNEA NUEVA
                .csrf(csrf -> csrf.disable()) // Desactiva la protección contra formularios web cruzados (necesario para APIs)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // Permite usar Postman sin pedir login por ahora
        return http.build();
    }
}
