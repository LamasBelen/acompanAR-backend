package com.acompanAR.backend.service;

import com.acompanAR.backend.dto.DenunciaRegistroDTO;
import com.acompanAR.backend.dto.DenunciaResolucionDTO;
import com.acompanAR.backend.dto.DenunciaRespuestaDTO;
import com.acompanAR.backend.entity.Denuncia;
import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.EstadoDenuncia;
import com.acompanAR.backend.entity.ObjetoDenunciado;
import com.acompanAR.backend.entity.Persona;
import com.acompanAR.backend.entity.Usuario;
import com.acompanAR.backend.repository.CapacitacionRepository;
import com.acompanAR.backend.repository.DenunciaRepository;
import com.acompanAR.backend.repository.EmpleadorRepository;
import com.acompanAR.backend.repository.InstitucionRepository;
import com.acompanAR.backend.repository.OfertaLaboralRepository;
import com.acompanAR.backend.repository.ProfesionalRepository;
import com.acompanAR.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Denuncias de la comunidad y moderación del administrador.
 * Cualquier usuario logueado puede denunciar un perfil, una oferta o una capacitación;
 * el administrador la resuelve dando de baja lo denunciado, o la descarta.
 */
@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionalRepository;
    private final EmpleadorRepository empleadorRepository;
    private final InstitucionRepository institucionRepository;
    private final OfertaLaboralRepository ofertaLaboralRepository;
    private final CapacitacionRepository capacitacionRepository;
    private final ProfesionalService profesionalService;
    private final EmpleadorService empleadorService;
    private final InstitucionService institucionService;
    private final OfertaLaboralService ofertaLaboralService;
    private final CapacitacionService capacitacionService;

    public DenunciaService(DenunciaRepository denunciaRepository,
                           UsuarioRepository usuarioRepository,
                           ProfesionalRepository profesionalRepository,
                           EmpleadorRepository empleadorRepository,
                           InstitucionRepository institucionRepository,
                           OfertaLaboralRepository ofertaLaboralRepository,
                           CapacitacionRepository capacitacionRepository,
                           ProfesionalService profesionalService,
                           EmpleadorService empleadorService,
                           InstitucionService institucionService,
                           OfertaLaboralService ofertaLaboralService,
                           CapacitacionService capacitacionService) {
        this.denunciaRepository = denunciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.profesionalRepository = profesionalRepository;
        this.empleadorRepository = empleadorRepository;
        this.institucionRepository = institucionRepository;
        this.ofertaLaboralRepository = ofertaLaboralRepository;
        this.capacitacionRepository = capacitacionRepository;
        this.profesionalService = profesionalService;
        this.empleadorService = empleadorService;
        this.institucionService = institucionService;
        this.ofertaLaboralService = ofertaLaboralService;
        this.capacitacionService = capacitacionService;
    }

    // Resumen de lo denunciado: cómo mostrarlo, de quién es (para que nadie se denuncie a sí mismo) y si sigue activo
    private record Objetivo(String descripcion, String nombreUsuarioDuenio, Estado estado) {
    }

    private Objetivo buscarObjetivo(ObjetoDenunciado objeto, Long id) {
        return switch (objeto) {
            case PROFESIONAL -> profesionalRepository.findById(id)
                    .map(p -> new Objetivo("Profesional: " + p.nombreCompleto(),
                            p.getUsuario().getNombreUsuario(), p.getEstado()))
                    .orElseThrow(() -> noExiste("El profesional denunciado"));
            case EMPLEADOR -> empleadorRepository.findById(id)
                    .map(e -> new Objetivo("Empleador: " + e.nombreCompleto() + " (" + e.getNombreEmpresa() + ")",
                            e.getUsuario().getNombreUsuario(), e.getEstado()))
                    .orElseThrow(() -> noExiste("El empleador denunciado"));
            case INSTITUCION -> institucionRepository.findById(id)
                    .map(i -> new Objetivo("Institución: " + i.getNombre(),
                            i.getUsuario() != null ? i.getUsuario().getNombreUsuario() : null, i.getEstado()))
                    .orElseThrow(() -> noExiste("La institución denunciada"));
            case OFERTA -> ofertaLaboralRepository.findById(id)
                    .map(o -> new Objetivo("Oferta: " + o.getTitulo(),
                            o.getEmpleador().getUsuario().getNombreUsuario(), o.getEstado()))
                    .orElseThrow(() -> noExiste("La oferta denunciada"));
            case CAPACITACION -> capacitacionRepository.findById(id)
                    .map(c -> new Objetivo("Capacitación: " + c.getTitulo(),
                            c.getInstitucion().getUsuario() != null
                                    ? c.getInstitucion().getUsuario().getNombreUsuario() : null,
                            c.getEstado()))
                    .orElseThrow(() -> noExiste("La capacitación denunciada"));
        };
    }

    private ResponseStatusException noExiste(String que) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, que + " no existe");
    }

    @Transactional
    public DenunciaRespuestaDTO denunciar(DenunciaRegistroDTO dto, Authentication authentication) {
        Usuario denunciante = usuarioRepository.findByNombreUsuario(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Objetivo objetivo = buscarObjetivo(dto.getObjeto(), dto.getIdObjeto());

        if (authentication.getName().equals(objetivo.nombreUsuarioDuenio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No podés denunciar tus propios datos");
        }
        if (objetivo.estado() != Estado.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Eso ya fue dado de baja");
        }
        if (denunciaRepository.existsByDenuncianteIdAndObjetoAndIdObjetoAndEstado(
                denunciante.getId(), dto.getObjeto(), dto.getIdObjeto(), EstadoDenuncia.PENDIENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya hiciste esta denuncia y está en revisión");
        }

        Denuncia denuncia = new Denuncia();
        denuncia.setDenunciante(denunciante);
        denuncia.setObjeto(dto.getObjeto());
        denuncia.setIdObjeto(dto.getIdObjeto());
        denuncia.setMotivo(dto.getMotivo());
        denuncia.setDescripcion(dto.getDescripcion().trim());
        denuncia.setFecha(LocalDate.now());
        denuncia.setEstado(EstadoDenuncia.PENDIENTE);
        return aDTO(denunciaRepository.save(denuncia));
    }

    // Para el administrador: todas las denuncias, o solo las de un estado. Las más nuevas primero.
    public List<DenunciaRespuestaDTO> listar(EstadoDenuncia estado) {
        List<Denuncia> denuncias = estado == null ? denunciaRepository.findAll() : denunciaRepository.findByEstado(estado);
        return denuncias.stream()
                .sorted(Comparator.comparing(Denuncia::getFecha).reversed().thenComparing(Denuncia::getId, Comparator.reverseOrder()))
                .map(this::aDTO)
                .toList();
    }

    // El administrador resuelve: da de baja lo denunciado o descarta la denuncia
    @Transactional
    public DenunciaRespuestaDTO resolver(Long id, DenunciaResolucionDTO dto, Authentication authentication) {
        Denuncia denuncia = denunciaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Denuncia no encontrada"));
        if (denuncia.getEstado() != EstadoDenuncia.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta denuncia ya fue resuelta");
        }

        String nota = dto.getNota() == null || dto.getNota().isBlank() ? null : dto.getNota().trim();

        switch (dto.getAccion()) {
            case DAR_DE_BAJA -> {
                darDeBajaLoDenunciado(denuncia, authentication);
                denuncia.setEstado(EstadoDenuncia.RESUELTA);
                // Las demás denuncias pendientes sobre lo mismo también quedan resueltas
                for (Denuncia otra : denunciaRepository.findByObjetoAndIdObjetoAndEstado(
                        denuncia.getObjeto(), denuncia.getIdObjeto(), EstadoDenuncia.PENDIENTE)) {
                    if (!otra.getId().equals(denuncia.getId())) {
                        cerrar(otra, EstadoDenuncia.RESUELTA, "Se dio de baja lo denunciado");
                        denunciaRepository.save(otra);
                    }
                }
                cerrar(denuncia, EstadoDenuncia.RESUELTA, nota != null ? nota : "Se dio de baja lo denunciado");
            }
            case DESCARTAR -> cerrar(denuncia, EstadoDenuncia.DESCARTADA,
                    nota != null ? nota : "No se encontró una infracción");
        }
        return aDTO(denunciaRepository.save(denuncia));
    }

    private void cerrar(Denuncia denuncia, EstadoDenuncia estado, String resolucion) {
        denuncia.setEstado(estado);
        denuncia.setResolucion(resolucion);
        denuncia.setFechaResolucion(LocalDate.now());
    }

    // Reutiliza la baja lógica de cada service (con sus cascadas). Si ya estaba dado de baja, no hace nada.
    private void darDeBajaLoDenunciado(Denuncia denuncia, Authentication authentication) {
        Objetivo objetivo = buscarObjetivo(denuncia.getObjeto(), denuncia.getIdObjeto());
        if (objetivo.estado() != Estado.ACTIVO) {
            return;
        }
        Long id = denuncia.getIdObjeto();
        switch (denuncia.getObjeto()) {
            case PROFESIONAL -> profesionalService.darDeBaja(id, authentication);
            case EMPLEADOR -> empleadorService.darDeBaja(id, authentication);
            case INSTITUCION -> institucionService.darDeBaja(id);
            case OFERTA -> ofertaLaboralService.darDeBaja(id, authentication);
            case CAPACITACION -> capacitacionService.darDeBaja(id, authentication);
        }
    }

    private DenunciaRespuestaDTO aDTO(Denuncia denuncia) {
        Objetivo objetivo = buscarObjetivo(denuncia.getObjeto(), denuncia.getIdObjeto());
        DenunciaRespuestaDTO dto = new DenunciaRespuestaDTO();
        dto.setId(denuncia.getId());
        dto.setObjeto(denuncia.getObjeto());
        dto.setIdObjeto(denuncia.getIdObjeto());
        dto.setDescripcionObjeto(objetivo.descripcion());
        dto.setObjetoActivo(objetivo.estado() == Estado.ACTIVO);
        dto.setMotivo(denuncia.getMotivo());
        dto.setDescripcion(denuncia.getDescripcion());
        dto.setFecha(denuncia.getFecha());
        dto.setEstado(denuncia.getEstado());
        dto.setResolucion(denuncia.getResolucion());
        Persona persona = denuncia.getDenunciante().getPersona();
        dto.setNombreDenunciante(persona != null
                ? persona.getNombre() + " " + persona.getApellido()
                : denuncia.getDenunciante().getNombreUsuario());
        return dto;
    }
}
