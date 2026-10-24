package com.acompanAR.backend.controller;

import com.acompanAR.backend.dto.LoginDTO;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@Valid @RequestBody LoginDTO dto){
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(dto.getNombreUsuario());
        if (usuarioOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("error: usuario o contraseña incorrectos");
        }
        Usuario usuario = usuarioOpt.get();
        if (passwordEncoder.matches(dto.getContrasena(),usuario.getContrasena())){
            //si el login esta bien, se devuelven los datos basicos para que el front los use
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("error: usuario o contraseña incorrectos");
        }
    }
}
