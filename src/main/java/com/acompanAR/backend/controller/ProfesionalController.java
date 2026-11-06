package com.acompanAR.backend.controller;


import com.acompanAR.backend.entity.TipoProfesional;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.dto.ProfesionalEdicionDTO;
import com.acompanAR.backend.dto.ProfesionalRegistroDTO;
import com.acompanAR.backend.entity.EstadoInscripcionCapacitacion;
import com.acompanAR.backend.entity.EstadoPostulacion;
import com.acompanAR.backend.entity.Inscripcion;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Postulacion;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.entity.Rol;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.security.Permisos;
import com.acompanAR.backend.repository.InscripcionRepository;
import com.acompanAR.backend.repository.PersonaRepository;
import com.acompanAR.backend.repository.PostulacionRepository;
import com.acompanAR.backend.repository.TipoProfesionalRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import com.acompanAR.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
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
    @Autowired
    private PostulacionRepository postulacionRepository;
    @Autowired
    private InscripcionRepository inscripcionRepository;
    @Autowired
    private TipoProfesionalRepository tipoProfesionalRepository;

    // Busca el tipo de profesional elegido y verifica que exista y no esté dado de baja
    private TipoProfesional buscarTipoProfesional(Long id) {
        TipoProfesional tipo = tipoProfesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de profesional elegido no existe"));
        if (tipo.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de profesional elegido ya no está disponible");
        }
        return tipo;
    }

    @PostMapping("/registro")
    @Transactional // Esto asegura que si algo falla, no se guarde nada por la mitad
    public Profesional registrarProfesional(@Valid @RequestBody ProfesionalRegistroDTO dto) {

        // El nombre de usuario no se puede repetir
        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nombre de usuario '" + dto.getNombreUsuario() + "' ya está en uso. Elegí otro.");
        }

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
        usuario.setEstado(Estado.ACTIVO); // Lo dejo activo por defecto
        usuario.setPersona(persona); //  hago la relación fk_persona
        usuario = usuarioRepository.save(usuario);

        //  Creo y guardo el Profesional (Lo vinculo con Persona y Usuario)
        Profesional profesional = new Profesional();
        profesional.setTipoProfesional(buscarTipoProfesional(dto.getTipoProfesionalId()));
        profesional.setAniosExperiencia(dto.getAniosExperiencia());
        profesional.setDescripcionExperiencia(dto.getDescripcionExperiencia());
        profesional.setDisponibilidad(dto.getDisponibilidad());
        profesional.setMatricula(dto.getMatricula());
        profesional.setEstado(Estado.ACTIVO);
        profesional.setPersona(persona); //  relación fk_persona
        profesional.setUsuario(usuario); //  la relación fk_usuario

        return profesionalRepository.save(profesional);


    }
    @GetMapping
    public List<Profesional> obtenerTodos() {
        return profesionalRepository.findByEstado(Estado.ACTIVO); // solo los profesionales activos
    }

    // Baja lógica de un profesional: lo puede hacer el propio profesional o un administrador.
    // No se borra nada: se marcan como inactivos el perfil y el usuario (ya no podrá ingresar).
    @PutMapping("/baja/{id}")
    @Transactional
    public Profesional darDeBaja(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));

        boolean esElMismo = Permisos.esElMismoUsuario(profesional.getUsuario().getNombreUsuario(), authentication);
        if (!esElMismo && !Permisos.esAdministrador(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No podés dar de baja a otra persona");
        }
        if (profesional.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El profesional ya está dado de baja");
        }

        // 1. Baja del perfil y del usuario
        profesional.setEstado(Estado.INACTIVO);
        profesional.getUsuario().setEstado(Estado.INACTIVO);
        usuarioRepository.save(profesional.getUsuario());

        // 2. Se cancelan sus postulaciones pendientes y sus inscripciones vigentes
        for (Postulacion postulacion : postulacionRepository.findByProfesionalId(id)) {
            if (postulacion.getEstado() == EstadoPostulacion.PENDIENTE) {
                postulacion.setEstado(EstadoPostulacion.CANCELADA);
                postulacionRepository.save(postulacion);
            }
        }
        for (Inscripcion inscripcion : inscripcionRepository.findByProfesionalId(id)) {
            if (inscripcion.getEstado() == EstadoInscripcionCapacitacion.CONFIRMADA) {
                inscripcion.setEstado(EstadoInscripcionCapacitacion.CANCELADA);
                inscripcionRepository.save(inscripcion);
            }
        }
        Profesional guardado = profesionalRepository.save(profesional);

        // 3. Si se dio de baja a sí mismo, se cierra su sesión
        if (esElMismo) {
            HttpSession sesion = request.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }
        }
        return guardado;
    }

    // Ver el perfil de un profesional
    @GetMapping("/{id}")
    public Profesional obtenerPorId(@PathVariable Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
    }

    @PutMapping("/editar/{id}")
    @Transactional
    public Profesional actualizarProfesional(@PathVariable Long id,
                                             @Valid @RequestBody ProfesionalEdicionDTO dto,
                                             Authentication authentication) {

        // 1. Buscamos que el profesional exista
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));

        // 2. Solo el dueño del perfil puede modificarlo
        if (!profesional.getUsuario().getNombreUsuario().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No podés modificar el perfil de otra persona");
        }

        // 3. Actualizamos los datos personales (no tocamos el usuario ni la contraseña acá)
        profesional.getPersona().setNombre(dto.getNombre());
        profesional.getPersona().setApellido(dto.getApellido());
        profesional.getPersona().setTelefono(dto.getTelefono());
        profesional.getPersona().setLocalidad(dto.getLocalidad());

        // 4. Actualizamos los datos propios del profesional
        profesional.setTipoProfesional(buscarTipoProfesional(dto.getTipoProfesionalId()));
        profesional.setAniosExperiencia(dto.getAniosExperiencia());
        profesional.setDescripcionExperiencia(dto.getDescripcionExperiencia());
        profesional.setDisponibilidad(dto.getDisponibilidad());
        profesional.setMatricula(dto.getMatricula());

        // 5. Guardamos los cambios
        return profesionalRepository.save(profesional);
    }
}