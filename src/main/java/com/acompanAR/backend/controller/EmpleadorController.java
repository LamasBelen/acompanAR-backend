package com.acompanAR.backend.controller;
import com.acompanAR.backend.dto.EmpleadorEdicionDTO;
import com.acompanAR.backend.dto.EmpleadorRegistroDTO;
import com.acompanAR.backend.entity.Empleador;
import com.acompanAR.backend.entity.EstadoPostulacion;
import com.acompanAR.backend.entity.OfertaLaboral;
import com.acompanAR.backend.entity.Postulacion;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Rol;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.security.Permisos;
import com.acompanAR.backend.repository.EmpleadorRepository;
import com.acompanAR.backend.repository.OfertaLaboralRepository;
import com.acompanAR.backend.repository.PersonaRepository;
import com.acompanAR.backend.repository.PostulacionRepository;
import com.acompanAR.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
    @Autowired
    private OfertaLaboralRepository ofertaLaboralRepository;
    @Autowired
    private PostulacionRepository postulacionRepository;
    @PostMapping("/registro")
    @Transactional //evita que se guarde a medias si ahy un error
    public Empleador RegistrarEmpleador(@Valid @RequestBody EmpleadorRegistroDTO dto) {
        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nombre de usuario '" + dto.getNombreUsuario() + "' ya está en uso. Elegí otro.");
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

    // Listado de empleadores
    @GetMapping
    public List<Empleador> obtenerTodos() {
        return empleadorRepository.findByEstadoTrue(); // solo los empleadores activos
    }

    // Baja lógica de un empleador: lo puede hacer el propio empleador o un administrador.
    // No se borra nada: se marcan como inactivos el perfil, el usuario y sus ofertas.
    @PutMapping("/baja/{id}")
    @Transactional
    public Empleador darDeBaja(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Empleador empleador = empleadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleador no encontrado"));

        boolean esElMismo = Permisos.esElMismoUsuario(empleador.getUsuario().getNombreUsuario(), authentication);
        if (!esElMismo && !Permisos.esAdministrador(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No podés dar de baja a otra persona");
        }
        if (!empleador.isEstado()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El empleador ya está dado de baja");
        }

        // 1. Baja del perfil y del usuario
        empleador.setEstado(false);
        empleador.getUsuario().setEstado(false);
        usuarioRepository.save(empleador.getUsuario());

        // 2. Se dan de baja sus ofertas activas y se rechazan las postulaciones que seguían pendientes
        for (OfertaLaboral oferta : ofertaLaboralRepository.findByEmpleadorIdAndActivaTrue(id)) {
            oferta.setActiva(false);
            ofertaLaboralRepository.save(oferta);
            for (Postulacion postulacion : postulacionRepository.findByOfertaLaboralId(oferta.getId())) {
                if (postulacion.getEstado() == EstadoPostulacion.PENDIENTE) {
                    postulacion.setEstado(EstadoPostulacion.RECHAZADA);
                    postulacionRepository.save(postulacion);
                }
            }
        }
        Empleador guardado = empleadorRepository.save(empleador);

        // 3. Si se dio de baja a sí mismo, se cierra su sesión
        if (esElMismo) {
            HttpSession sesion = request.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }
        }
        return guardado;
    }

    // Ver el perfil de un empleador
    @GetMapping("/{id}")
    public Empleador obtenerPorId(@PathVariable Long id) {
        return empleadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleador no encontrado"));
    }

    @PutMapping("/editar/{id}")
    @Transactional
    public Empleador actualizarEmpleador(@PathVariable Long id,
                                         @Valid @RequestBody EmpleadorEdicionDTO dto,
                                         Authentication authentication) {

        // 1. Buscamos que el empleador exista
        Empleador empleador = empleadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleador no encontrado"));

        // 2. Solo el dueño del perfil puede modificarlo
        if (!empleador.getUsuario().getNombreUsuario().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No podés modificar el perfil de otra persona");
        }

        // 3. Actualizamos los datos personales a través de getPersona()
        empleador.getPersona().setNombre(dto.getNombre());
        empleador.getPersona().setApellido(dto.getApellido());
        empleador.getPersona().setTelefono(dto.getTelefono());
        empleador.getPersona().setLocalidad(dto.getLocalidad());

        // 4. Actualizamos los datos propios del empleador
        empleador.setNombreEmpresa(dto.getNombreEmpresa());
        empleador.setDescripcionPuesto(dto.getDescripcionPuesto());

        // 5. Guardamos los cambios
        return empleadorRepository.save(empleador);
    }
}
