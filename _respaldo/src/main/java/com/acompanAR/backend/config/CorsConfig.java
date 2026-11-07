package com.acompanAR.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    // Orígenes del front permitidos. Se pueden cambiar en application.properties con:
    // app.cors.allowed-origins=http://localhost:5173,https://mi-sitio.com
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost:4200}")
    private String[] allowedOrigins;

    // Spring Security usa este bean (por llamarse corsConfigurationSource) cuando activamos .cors(...)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // Necesario para que el navegador envíe y reciba la cookie de sesión (JSESSIONID).
        // Con allowCredentials(true) NO se puede usar "*" como origen: hay que listar los orígenes.
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}