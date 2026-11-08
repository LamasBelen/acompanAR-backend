package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.InstitucionAdminDTO;
import com.acompanAR.backend.dto.InstitucionCuentaRegistroDTO;
import com.acompanAR.backend.dto.InstitucionRegistroDTO;
import com.acompanAR.backend.entity.Capacitacion;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoVerificacion;
import com.acompanAR.backend.entity.Institucion;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.CapacitacionRepository;
import com.acompanAR.backend.repository.InstitucionRepository;
import com.acompanAR.backend.repository.PersonaRepository;
import com.acompanAR.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Lógica de negocio de las instituciones que publican capacitaciones.
 * Una institución puede registrarse sola (queda PENDIENTE hasta que el administrador la verifique)
 * o ser cargada por el administrador (queda VERIFICADA directamente).
 */
@Service
public class InstitucionService {

    private final InstitucionRepository institucionRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public InstitucionService(InstitucionRepository institucionRepository,
                              CapacitacionRepository capacitacionRepository,
                              PersonaRepository personaRepository,
                              UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder) {
        this.institucionRepository = institucionRepository;
        this.capacitacionRepository = capacitacionRepository;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Las instituciones que se pueden elegir: activas y verificadas
    public List<Institucion> listarVerificadas() {
        return institucionRepository.findByEstadoAndEstadoVerificacion(Estado.ACTIVO, EstadoVerificacion.VERIFICADA);
    }

    // Para el administrador: todas las activas (pendientes, verificadas o rechazadas), con datos del responsable
    public List<InstitucionAdminDTO> listarParaAdmin() {
        return institucionRepository.findByEstado(Estado.ACTIVO).stream().map(this::aAdminDTO).toList();
    }

    public Institucion obtenerPorId(Long id) {
        return institucionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Institución no encontrada"));
    }

    // La institución del usuario logueado (para su pantalla de inicio)
    public Institucion obtenerDelUsuario(Authentication authentication) {
        return institucionRepository.findByUsuarioNombreUsuario(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tu usuario no es una institución"));
    }

    // La institución se registra por su cuenta: queda PENDIENTE de verificación
    @Transactional
    public Institucion registrar(InstitucionCuentaRegistroDTO dto) {
        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nombre de usuario '" + dto.getNombreUsuario() + "' ya está en uso. Elegí otro.");
        }
        String nombre = dto.getNombreInstitucion().trim();
        if (institucionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una institución llamada '" + nombre + "'");
        }

        // 1. Persona responsable
        Persona persona = new Persona();
        persona.setNombre(dto.getNombre());
        persona.setApellido(dto.getApellido());
        persona.setTelefono(dto.getTelefono());
        persona.setLocalidad(dto.getLocalidad());
        persona = personaRepository.save(persona);

        // 2. Usuario con el rol del perfil (polimorfismo: rolDelPerfil()) y contraseña hasheada
        Institucion institucion = new Institucion();
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(dto.getNombreUsuario());
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setRol(institucion.rolDelPerfil());
        usuario.setEstado(Estado.ACTIVO);
        usuario.setPersona(persona);
        usuario = usuarioRepository.save(usuario);

        // 3. Institución, pendiente hasta que el administrador la verifique
        institucion.setNombre(nombre);
        institucion.setTipo(dto.getTipo());
        institucion.setLocalidad(dto.getLocalidad());
        institucion.setEstado(Estado.ACTIVO);
        institucion.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);
        institucion.setPersona(persona);
        institucion.setUsuario(usuario);
        return institucionRepository.save(institucion);
    }

    // El administrador carga una institución a mano (sin cuenta): queda VERIFICADA
    @Transactional
    public Institucion crear(InstitucionRegistroDTO dto) {
        String nombre = dto.getNombre().trim();
        if (institucionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una institución llamada '" + nombre + "'");
        }
        Institucion institucion = new Institucion();
        institucion.setNombre(nombre);
        institucion.setTipo(dto.getTipo());
        institucion.setLocalidad(dto.getLocalidad());
        institucion.setEstado(Estado.ACTIVO);
        institucion.setEstadoVerificacion(EstadoVerificacion.VERIFICADA);
        return institucionRepository.save(institucion);
    }

    @Transactional
    public Institucion editar(Long id, InstitucionRegistroDTO dto) {
        Institucion institucion = obtenerPorId(id);
        String nombre = dto.getNombre().trim();
        if (!institucion.getNombre().equalsIgnoreCase(nombre) && institucionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una institución llamada '" + nombre + "'");
        }
        institucion.setNombre(nombre);
        institucion.setTipo(dto.getTipo());
        institucion.setLocalidad(dto.getLocalidad());
        return institucionRepository.save(institucion);
    }

    // El administrador aprueba la institución: desde ahora puede publicar capacitaciones
    @Transactional
    public Institucion verificar(Long id) {
        Institucion institucion = obtenerPorId(id);
        if (!institucion.estaActivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La institución está dada de baja");
        }
        if (institucion.getEstadoVerificacion() == EstadoVerificacion.VERIFICADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La institución ya está verificada");
        }
        institucion.setEstadoVerificacion(EstadoVerificacion.VERIFICADA);
        return institucionRepository.save(institucion);
    }

    // El administrador rechaza la solicitud: la institución no puede publicar.
    // Solo se rechaza una solicitud pendiente; a una ya verificada se le da de baja.
    @Transactional
    public Institucion rechazar(Long id) {
        Institucion institucion = obtenerPorId(id);
        if (institucion.getEstadoVerificacion() != EstadoVerificacion.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede rechazar una solicitud pendiente. Si ya fue verificada, dala de baja.");
        }
        institucion.setEstadoVerificacion(EstadoVerificacion.RECHAZADA);
        return institucionRepository.save(institucion);
    }

    // Baja lógica: la institución, su usuario (ya no puede ingresar) y sus capacitaciones activas quedan INACTIVOS
    @Transactional
    public Institucion darDeBaja(Long id) {
        Institucion institucion = obtenerPorId(id);
        if (!institucion.estaActivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La institución ya está dada de baja");
        }
        institucion.darDeBaja(); // método heredado de Perfil
        if (institucion.getUsuario() != null) {
            usuarioRepository.save(institucion.getUsuario());
        }
        for (Capacitacion capacitacion : capacitacionRepository.findByInstitucionIdAndEstado(id, Estado.ACTIVO)) {
            capacitacion.setEstado(Estado.INACTIVO);
            capacitacionRepository.save(capacitacion);
        }
        return institucionRepository.save(institucion);
    }

    private InstitucionAdminDTO aAdminDTO(Institucion institucion) {
        InstitucionAdminDTO dto = new InstitucionAdminDTO();
        dto.setId(institucion.getId());
        dto.setNombre(institucion.getNombre());
        dto.setTipo(institucion.getTipo());
        dto.setLocalidad(institucion.getLocalidad());
        dto.setEstado(institucion.getEstado());
        dto.setEstadoVerificacion(institucion.getEstadoVerificacion());
        dto.setTieneCuenta(institucion.getUsuario() != null);
        if (institucion.getUsuario() != null) {
            dto.setNombreUsuario(institucion.getUsuario().getNombreUsuario());
        }
        if (institucion.getPersona() != null) {
            dto.setResponsable(institucion.nombreCompleto());
            dto.setTelefono(institucion.getPersona().getTelefono());
        }
        return dto;
    }
}
