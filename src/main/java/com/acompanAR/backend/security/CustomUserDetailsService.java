package com.acompanAR.backend.security;

import com.acompanAR.backend.entity.Estado;

import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Le explica a Spring Security cómo buscar un usuario en nuestra base de datos.
 * Spring la usa automáticamente cuando llamamos a authenticationManager.authenticate(...).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Spring Security espera que los roles empiecen con "ROLE_"
        return new User(
                usuario.getNombreUsuario(),
                usuario.getContrasena(),          // ya viene hasheada con BCrypt
                usuario.getEstado() == Estado.ACTIVO,               // enabled: un usuario dado de baja no puede loguearse
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
        );
    }
}
