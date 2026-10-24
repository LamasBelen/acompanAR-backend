package com.acompanAR.backend.controller;
import com.acompanAR.backend.dto.EmpleadorRegistroDTO;
import com.acompanAR.backend.entity.Empleador;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Rol;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.EmpleadorRepository;
import com.acompanAR.backend.repository.PersonaRepository;
import com.acompanAR.backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/empleadores")
public class EmpleadorController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpleadorRepository empleadorRepository;
    @PostMapping("/registro")
    @Transactional //evita que se guarde a medias si ahy un error
    public Empleador RegistrarEmpleador(@RequestBody EmpleadorRegistroDTO dto) {
        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())){
            throw new RuntimeException("Error: El nombre de usuario '" + dto.getNombreUsuario() + "' ya está en uso. Por favor, elegí otro." );
        }

        Persona persona = new Persona();
        persona.setNombre(dto.getNombre());
        persona.setApellido(dto.getApellido());
        persona.setTelefono(dto.getTelefono());
        persona.setLocalidad(dto.getLocalidad());
        persona = personaRepository.save(persona);
        //creamos el usuario con rol empleador

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(dto.getNombreUsuario());
        // para encriptar la contraseña
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setRol(Rol.EMPLEADOR); // Le asignamos el rol correspondiente
        usuario.setEstado(true);
        usuario.setPersona(persona);
        usuario = usuarioRepository.save(usuario);

        // 3. Creamos el Empleador
        Empleador empleador = new Empleador();
        empleador.setNombreEmpresa(dto.getNombreEmpresa());
        empleador.setDescripcionPuesto(dto.getDescripcionPuesto());
        empleador.setPersona(persona);
        empleador.setUsuario(usuario);

        return empleadorRepository.save(empleador);

    }
    @PutMapping("/editar/{id}")
    public Empleador actualizarEmpleador(@PathVariable Long id, @RequestBody EmpleadorRegistroDTO dto) {

        // 1. Buscamos que el empleador exista
        Empleador empleador = empleadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleador no encontrado"));

        // 2. Actualizamos los datos personales a través de getPersona()
        empleador.getPersona().setNombre(dto.getNombre());
        empleador.getPersona().setApellido(dto.getApellido());
        empleador.getPersona().setTelefono(dto.getTelefono());
        empleador.getPersona().setLocalidad(dto.getLocalidad());

        // Si el empleador tiene campos propios (como nombre de la empresa, CUIT, etc.), los actualizás directo así:
        // empleador.setNombreEmpresa(dto.getNombreEmpresa());

        // 3. Guardamos los cambios
        return empleadorRepository.save(empleador);
    }
}
