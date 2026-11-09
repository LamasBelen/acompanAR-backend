package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.ProfesionalEdicionDTO;
import com.acompanAR.backend.dto.ProfesionalRegistroDTO;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoInscripcionCapacitacion;
import com.acompanAR.backend.entity.EstadoPostulacion;
import com.acompanAR.backend.entity.Inscripcion;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Postulacion;
import com.acompanAR.backend.entity.Profesional;
import com.acompanAR.backend.entity.TipoProfesional;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.InscripcionRepository;
import com.acompanAR.backend.repository.PersonaRepository;
import com.acompanAR.backend.repository.PostulacionRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import com.acompanAR.backend.repository.TipoProfesionalRepository;
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
 * Lógica de negocio de los profesionales: registro, edición, listado y baja lógica.
 * El controller solo recibe la petición HTTP y llama a esta clase.
 */
@Service
public class ProfesionalService {

    private final PasswordEncoder passwordEncoder;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionalRepository;
    private final PostulacionRepository postulacionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final TipoProfesionalRepository tipoProfesionalRepository;

    public ProfesionalService(PasswordEncoder passwordEncoder,
                              PersonaRepository personaRepository,
                              UsuarioRepository usuarioRepository,
                              ProfesionalRepository profesionalRepository,
                              PostulacionRepository postulacionRepository,
                              InscripcionRepository inscripcionRepository,
                              TipoProfesionalRepository tipoProfesionalRepository) {
        this.passwordEncoder = passwordEncoder;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.profesionalRepository = profesionalRepository;
        this.postulacionRepository = postulacionRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.tipoProfesionalRepository = tipoProfesionalRepository;
    }

    // Saca los espacios de la matrícula y devuelve null si quedó vacía.
    // (Varias matrículas vacías "" chocarían entre sí; con null no hay problema.)
    private String normalizarMatricula(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            return null;
        }
        return matricula.trim();
    }

    // Busca el tipo de profesional elegido y verifica que exista y no esté dado de baja
    private TipoProfesional buscarTipoProfesional(Long id) {
        TipoProfesional tipo = tipoProfesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de profesional elegido no existe"));
        if (tipo.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de profesional elegido ya no está disponible");
        }
        return tipo;
    }

    @Transactional // si algo falla, no se guarda nada por la mitad
    public Profesional registrar(ProfesionalRegistroDTO dto) {
        // El nombre de usuario no se puede repetir
        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nombre de usuario '" + dto.getNombreUsuario() + "' ya está en uso. Elegí otro.");
        }

        // La matrícula (si la cargó) no puede estar en uso por otro profesional
        String matricula = normalizarMatricula(dto.getMatricula());
        if (matricula != null && profesionalRepository.existsByMatriculaIgnoreCase(matricula)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La matrícula '" + matricula + "' ya está registrada por otro profesional.");
        }

        if (personaRepository.existsByEmailIgnoreCase(dto.getEmail().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El correo '" + dto.getEmail().trim() + "' ya está registrado. Usá otro.");
        }

        // 1. Persona
        Persona persona = new Persona();
        persona.setNombre(dto.getNombre());
        persona.setApellido(dto.getApellido());
        persona.setTelefono(dto.getTelefono());
        persona.setEmail(dto.getEmail().trim());
        persona.setLocalidad(dto.getLocalidad());
        persona = personaRepository.save(persona);

        // 2. Usuario (vinculado a la persona, con la contraseña hasheada).
        // El rol lo define el propio perfil (polimorfismo: rolDelPerfil()).
        Profesional profesional = new Profesional();
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(dto.getNombreUsuario());
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setRol(profesional.rolDelPerfil());
        usuario.setEstado(Estado.ACTIVO);
        usuario.setPersona(persona);
        usuario = usuarioRepository.save(usuario);

        // 3. Profesional (vinculado a la persona y al usuario)
        profesional.setTipoProfesional(buscarTipoProfesional(dto.getTipoProfesionalId()));
        profesional.setAniosExperiencia(dto.getAniosExperiencia());
        profesional.setDescripcionExperiencia(dto.getDescripcionExperiencia());
        profesional.setDisponibilidad(dto.getDisponibilidad());
        profesional.setMatricula(matricula);
        profesional.setEstado(Estado.ACTIVO);
        profesional.setPersona(persona);
        profesional.setUsuario(usuario);
        return profesionalRepository.save(profesional);
    }

    public List<Profesional> listarActivos() {
        return profesionalRepository.findByEstado(Estado.ACTIVO);
    }

    public Profesional obtenerPorId(Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesional no encontrado"));
    }

    @Transactional
    public Profesional editar(Long id, ProfesionalEdicionDTO dto, Authentication authentication) {
        Profesional profesional = obtenerPorId(id);

        // Solo el dueño del perfil puede modificarlo
        if (!Permisos.esElMismoUsuario(profesional.getUsuario().getNombreUsuario(), authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No podés modificar el perfil de otra persona");
        }

        // La matrícula no puede ser la de otro profesional (la propia sí, si no la cambió)
        String matricula = normalizarMatricula(dto.getMatricula());
        if (matricula != null && profesionalRepository.existsByMatriculaIgnoreCaseAndIdNot(matricula, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La matrícula '" + matricula + "' ya está registrada por otro profesional.");
        }

        if (personaRepository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail().trim(), profesional.getPersona().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El correo '" + dto.getEmail().trim() + "' ya está registrado. Usá otro.");
        }

        // Datos personales (no se toca el usuario ni la contraseña)
        profesional.getPersona().setNombre(dto.getNombre());
        profesional.getPersona().setApellido(dto.getApellido());
        profesional.getPersona().setTelefono(dto.getTelefono());
        profesional.getPersona().setEmail(dto.getEmail().trim());
        profesional.getPersona().setLocalidad(dto.getLocalidad());

        // Datos propios del profesional
        profesional.setTipoProfesional(buscarTipoProfesional(dto.getTipoProfesionalId()));
        profesional.setAniosExperiencia(dto.getAniosExperiencia());
        profesional.setDescripcionExperiencia(dto.getDescripcionExperiencia());
        profesional.setDisponibilidad(dto.getDisponibilidad());
        profesional.setMatricula(matricula);

        return profesionalRepository.save(profesional);
    }

    // Baja lógica: la puede hacer el propio profesional o un administrador.
    // No se borra nada: se marcan como inactivos el perfil y el usuario (ya no podrá ingresar).
    // Si el profesional se dio de baja a sí mismo, el controller se encarga de cerrar la sesión.
    @Transactional
    public Profesional darDeBaja(Long id, Authentication authentication) {
        Profesional profesional = obtenerPorId(id);

        boolean esElMismo = Permisos.esElMismoUsuario(profesional.getUsuario().getNombreUsuario(), authentication);
        if (!esElMismo && !Permisos.esAdministrador(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No podés dar de baja a otra persona");
        }
        if (profesional.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El profesional ya está dado de baja");
        }

        // 1. Baja del perfil y del usuario
        profesional.darDeBaja(); // método heredado de Perfil: baja del perfil y de su usuario
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
        return profesionalRepository.save(profesional);
    }
}
