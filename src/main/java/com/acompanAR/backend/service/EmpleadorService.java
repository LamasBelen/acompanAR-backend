package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.EmpleadorEdicionDTO;
import com.acompanAR.backend.dto.EmpleadorRegistroDTO;
import com.acompanAR.backend.entity.Empleador;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoPostulacion;
import com.acompanAR.backend.entity.OfertaLaboral;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Postulacion;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.EmpleadorRepository;
import com.acompanAR.backend.repository.OfertaLaboralRepository;
import com.acompanAR.backend.repository.PersonaRepository;
import com.acompanAR.backend.repository.PostulacionRepository;
import com.acompanAR.backend.repository.UsuarioRepository;
import com.acompanAR.backend.security.Permisos;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Lógica de negocio de los empleadores: registro, edición, listado y baja lógica.
 */
@Service
public class EmpleadorService {

    private final PasswordEncoder passwordEncoder;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadorRepository empleadorRepository;
    private final OfertaLaboralRepository ofertaLaboralRepository;
    private final PostulacionRepository postulacionRepository;

    public EmpleadorService(PasswordEncoder passwordEncoder,
                            PersonaRepository personaRepository,
                            UsuarioRepository usuarioRepository,
                            EmpleadorRepository empleadorRepository,
                            OfertaLaboralRepository ofertaLaboralRepository,
                            PostulacionRepository postulacionRepository) {
        this.passwordEncoder = passwordEncoder;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.empleadorRepository = empleadorRepository;
        this.ofertaLaboralRepository = ofertaLaboralRepository;
        this.postulacionRepository = postulacionRepository;
    }

    @Transactional // evita que se guarde a medias si hay un error
    public Empleador registrar(EmpleadorRegistroDTO dto) {
        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nombre de usuario '" + dto.getNombreUsuario() + "' ya está en uso. Elegí otro.");
        }

        // 1. Persona
        Persona persona = new Persona();
        persona.setNombre(dto.getNombre());
        persona.setApellido(dto.getApellido());
        persona.setTelefono(dto.getTelefono());
        persona.setLocalidad(dto.getLocalidad());
        persona = personaRepository.save(persona);

        // 2. Usuario con el rol del perfil (polimorfismo: rolDelPerfil()) y contraseña hasheada
        Empleador empleador = new Empleador();
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(dto.getNombreUsuario());
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setRol(empleador.rolDelPerfil());
        usuario.setEstado(Estado.ACTIVO);
        usuario.setPersona(persona);
        usuario = usuarioRepository.save(usuario);

        // 3. Empleador
        empleador.setNombreEmpresa(dto.getNombreEmpresa());
        empleador.setDescripcionPuesto(dto.getDescripcionPuesto());
        empleador.setPersona(persona);
        empleador.setUsuario(usuario);
        return empleadorRepository.save(empleador);
    }

    public List<Empleador> listarActivos() {
        return empleadorRepository.findByEstado(Estado.ACTIVO);
    }

    public Empleador obtenerPorId(Long id) {
        return empleadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleador no encontrado"));
    }

    @Transactional
    public Empleador editar(Long id, EmpleadorEdicionDTO dto, Authentication authentication) {
        Empleador empleador = obtenerPorId(id);

        // Solo el dueño del perfil puede modificarlo
        if (!Permisos.esElMismoUsuario(empleador.getUsuario().getNombreUsuario(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No podés modificar el perfil de otra persona");
        }

        empleador.getPersona().setNombre(dto.getNombre());
        empleador.getPersona().setApellido(dto.getApellido());
        empleador.getPersona().setTelefono(dto.getTelefono());
        empleador.getPersona().setLocalidad(dto.getLocalidad());

        empleador.setNombreEmpresa(dto.getNombreEmpresa());
        empleador.setDescripcionPuesto(dto.getDescripcionPuesto());

        return empleadorRepository.save(empleador);
    }

    // Baja lógica: la puede hacer el propio empleador o un administrador.
    // No se borra nada: se marcan como inactivos el perfil, el usuario y sus ofertas.
    // Si el empleador se dio de baja a sí mismo, el controller se encarga de cerrar la sesión.
    @Transactional
    public Empleador darDeBaja(Long id, Authentication authentication) {
        Empleador empleador = obtenerPorId(id);

        boolean esElMismo = Permisos.esElMismoUsuario(empleador.getUsuario().getNombreUsuario(), authentication);
        if (!esElMismo && !Permisos.esAdministrador(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No podés dar de baja a otra persona");
        }
        if (empleador.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El empleador ya está dado de baja");
        }

        // 1. Baja del perfil y del usuario
        empleador.darDeBaja(); // método heredado de Perfil: baja del perfil y de su usuario
        usuarioRepository.save(empleador.getUsuario());

        // 2. Se dan de baja sus ofertas activas y se rechazan las postulaciones que seguían pendientes
        for (OfertaLaboral oferta : ofertaLaboralRepository.findByEmpleadorIdAndEstado(id, Estado.ACTIVO)) {
            oferta.setEstado(Estado.INACTIVO);
            ofertaLaboralRepository.save(oferta);
            for (Postulacion postulacion : postulacionRepository.findByOfertaLaboralId(oferta.getId())) {
                if (postulacion.getEstado() == EstadoPostulacion.PENDIENTE) {
                    postulacion.setEstado(EstadoPostulacion.RECHAZADA);
                    postulacionRepository.save(postulacion);
                }
            }
        }
        return empleadorRepository.save(empleador);
    }
}
