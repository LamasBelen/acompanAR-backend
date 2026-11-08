package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.CapacitacionRegistroDTO;
import com.acompanAR.backend.entity.Capacitacion;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoVerificacion;
import com.acompanAR.backend.entity.Institucion;
import com.acompanAR.backend.repository.CapacitacionRepository;
import com.acompanAR.backend.repository.InstitucionRepository;
import com.acompanAR.backend.security.Permisos;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio de las capacitaciones (ABM con baja lógica).
 * Las publica una institución verificada (solo las suyas) o el administrador (de cualquier institución).
 */
@Service
public class CapacitacionService {

    private final CapacitacionRepository capacitacionRepository;
    private final InstitucionRepository institucionRepository;

    public CapacitacionService(CapacitacionRepository capacitacionRepository,
                               InstitucionRepository institucionRepository) {
        this.capacitacionRepository = capacitacionRepository;
        this.institucionRepository = institucionRepository;
    }

    // Busca la institución elegida por el administrador y verifica que exista, esté activa y verificada
    private Institucion buscarInstitucion(Long id) {
        Institucion institucion = institucionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La institución elegida no existe"));
        if (!institucion.estaActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La institución elegida está dada de baja");
        }
        if (institucion.getEstadoVerificacion() != EstadoVerificacion.VERIFICADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La institución elegida todavía no está verificada");
        }
        return institucion;
    }

    // La institución del usuario logueado
    private Institucion institucionLogueada(Authentication authentication) {
        return institucionRepository.findByUsuarioNombreUsuario(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu usuario no es una institución"));
    }

    // Una institución solo puede publicar si fue verificada por el administrador
    private void verificarQuePuedePublicar(Institucion institucion) {
        if (institucion.puedePublicar()) {
            return;
        }
        if (institucion.getEstadoVerificacion() == EstadoVerificacion.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tu institución está pendiente de verificación. Cuando el administrador la apruebe vas a poder publicar capacitaciones.");
        }
        if (institucion.getEstadoVerificacion() == EstadoVerificacion.RECHAZADA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La solicitud de tu institución fue rechazada.");
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu institución está dada de baja.");
    }

    // El administrador puede con cualquiera; una institución, solo con las suyas
    private void verificarDuenio(Capacitacion capacitacion, Authentication authentication) {
        if (Permisos.esAdministrador(authentication)) {
            return;
        }
        Institucion duenia = capacitacion.getInstitucion();
        boolean esSuya = duenia.getUsuario() != null
                && Permisos.esElMismoUsuario(duenia.getUsuario().getNombreUsuario(), authentication);
        if (!esSuya) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta capacitación no es de tu institución");
        }
    }

    @Transactional
    public Capacitacion crear(CapacitacionRegistroDTO dto, Authentication authentication) {
        Institucion institucion;
        if (Permisos.esAdministrador(authentication)) {
            if (dto.getInstitucionId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La institución es obligatoria");
            }
            institucion = buscarInstitucion(dto.getInstitucionId());
        } else {
            // Una institución publica siempre a su nombre, aunque mande otro id
            institucion = institucionLogueada(authentication);
            verificarQuePuedePublicar(institucion);
        }

        Capacitacion capacitacion = new Capacitacion();
        capacitacion.setTitulo(dto.getTitulo());
        capacitacion.setDescripcion(dto.getDescripcion());
        capacitacion.setInstitucion(institucion);
        capacitacion.setCargaHoraria(dto.getCargaHoraria());
        capacitacion.setLinkVideo(dto.getLinkVideo());
        // Si no mandan fecha de inicio, se usa hoy + 15 días
        capacitacion.setFechaInicio(dto.getFechaInicio() != null ? dto.getFechaInicio() : LocalDate.now().plusDays(15));
        capacitacion.setEstado(Estado.ACTIVO);
        return capacitacionRepository.save(capacitacion);
    }

    // Solo las capacitaciones activas (las dadas de baja no se muestran)
    public List<Capacitacion> listarActivas() {
        return capacitacionRepository.findByEstado(Estado.ACTIVO);
    }

    // Las capacitaciones activas de la institución logueada
    public List<Capacitacion> listarDeLaInstitucion(Authentication authentication) {
        Institucion institucion = institucionLogueada(authentication);
        return capacitacionRepository.findByInstitucionIdAndEstado(institucion.getId(), Estado.ACTIVO);
    }

    public Capacitacion obtenerPorId(Long id) {
        return capacitacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capacitación no encontrada"));
    }

    @Transactional
    public Capacitacion editar(Long id, CapacitacionRegistroDTO dto, Authentication authentication) {
        Capacitacion capacitacion = obtenerPorId(id);
        verificarDuenio(capacitacion, authentication);

        capacitacion.setTitulo(dto.getTitulo());
        capacitacion.setDescripcion(dto.getDescripcion());
        // Solo el administrador puede cambiar la institución; una institución no puede "pasarle" la capacitación a otra
        if (Permisos.esAdministrador(authentication) && dto.getInstitucionId() != null) {
            capacitacion.setInstitucion(buscarInstitucion(dto.getInstitucionId()));
        }
        capacitacion.setCargaHoraria(dto.getCargaHoraria());
        capacitacion.setLinkVideo(dto.getLinkVideo());
        if (dto.getFechaInicio() != null) {
            capacitacion.setFechaInicio(dto.getFechaInicio());
        }
        return capacitacionRepository.save(capacitacion);
    }

    // Baja lógica: no se borra el registro, solo se marca como INACTIVO
    @Transactional
    public Capacitacion darDeBaja(Long id, Authentication authentication) {
        Capacitacion capacitacion = obtenerPorId(id);
        verificarDuenio(capacitacion, authentication);
        capacitacion.setEstado(Estado.INACTIVO);
        return capacitacionRepository.save(capacitacion);
    }
}
