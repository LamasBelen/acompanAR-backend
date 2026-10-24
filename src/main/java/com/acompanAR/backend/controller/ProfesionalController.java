package com.acompanAR.backend.controller;


import com.acompanAR.backend.dto.ProfesionalRegistroDTO;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.entity.Rol;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.PersonaRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import com.acompanAR.backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profesionales")
public class ProfesionalController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProfesionalRepository profesionalRepository;

    @PostMapping("/registro")
    @Transactional // Esto asegura que si algo falla, no se guarde nada por la mitad
    public Profesional registrarProfesional(@RequestBody ProfesionalRegistroDTO dto) {

        // Creo y guardo la Persona
        Persona persona = new Persona();
        persona.setNombre(dto.getNombre());
        persona.setApellido(dto.getApellido());
        persona.setTelefono(dto.getTelefono());
        persona.setLocalidad(dto.getLocalidad());
        persona = personaRepository.save(persona); // Se guarda y se le asigna un ID

        //  Creo y guardo el Usuario (Lo vinculo con la Persona)
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(dto.getNombreUsuario());
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setRol(Rol.PROFESIONAL); // Le asigno el rol automáticamente
        usuario.setEstado(true); // Lo dejo activo por defecto
        usuario.setPersona(persona); //  hago la relación fk_persona
        usuario = usuarioRepository.save(usuario);

        //  Creo y guardo el Profesional (Lo vinculo con Persona y Usuario)
        Profesional profesional = new Profesional();
        profesional.setTipoProfesional(dto.getTipoProfesional());
        profesional.setExperiencia(dto.getExperiencia());
        profesional.setDisponibilidad(dto.getDisponibilidad());
        profesional.setMatricula(dto.getMatricula());
        profesional.setEstado(true);
        profesional.setPersona(persona); //  relación fk_persona
        profesional.setUsuario(usuario); //  la relación fk_usuario

        return profesionalRepository.save(profesional);


    }
    @GetMapping
    public List<Profesional> obtenerTodos() {
        return profesionalRepository.findAll(); // Esto hace un SELECT * automático
    }
    @PutMapping("/editar/{id}")
    public Profesional actualizarProfesional(@PathVariable Long id, @RequestBody ProfesionalRegistroDTO dto) {

        // 1. Buscamos que el profesional exista
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        // 2. Actualizamos los datos personales (no tocamos el usuario ni la contraseña acá)
        profesional.getPersona().setNombre(dto.getNombre());
        profesional.getPersona().setApellido(dto.getApellido());
        profesional.getPersona().setTelefono(dto.getTelefono());
        profesional.getPersona().setLocalidad(dto.getLocalidad());

        // 3. Guardamos los cambios
        return profesionalRepository.save(profesional);
    }
}