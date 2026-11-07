package com.acompanAR.backend.config;

import com.acompanAR.backend.entity.Estado;
import com.acompanAR.backend.entity.Institucion;
import com.acompanAR.backend.entity.Localidad;
import com.acompanAR.backend.entity.TipoInstitucion;
import com.acompanAR.backend.entity.TipoProfesional;
import com.acompanAR.backend.repository.InstitucionRepository;
import com.acompanAR.backend.repository.TipoProfesionalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Al arrancar, si las tablas de tipos de profesional e instituciones están vacías,
 * las carga con datos básicos para que el registro y las capacitaciones funcionen.
 * Después el administrador puede agregar más desde la aplicación.
 */
@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarTiposProfesional(TipoProfesionalRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            for (String nombre : new String[]{
                    "Enfermero/a", "Auxiliar de enfermería", "Acompañante terapéutico", "Cuidador/a domiciliario/a"}) {
                TipoProfesional tipo = new TipoProfesional();
                tipo.setNombre(nombre);
                tipo.setEstado(Estado.ACTIVO);
                repository.save(tipo);
            }
            System.out.println(">>> Tipos de profesional iniciales cargados");
        };
    }

    @Bean
    CommandLineRunner cargarInstituciones(InstitucionRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            guardar(repository, "Ministerio de Salud de Tierra del Fuego", TipoInstitucion.PUBLICA, Localidad.USHUAIA);
            guardar(repository, "Hospital Regional Río Grande", TipoInstitucion.PUBLICA, Localidad.RIO_GRANDE);
            guardar(repository, "Cruz Roja Argentina", TipoInstitucion.PRIVADA, Localidad.RIO_GRANDE);
            System.out.println(">>> Instituciones iniciales cargadas");
        };
    }

    private void guardar(InstitucionRepository repository, String nombre, TipoInstitucion tipo, Localidad localidad) {
        Institucion institucion = new Institucion();
        institucion.setNombre(nombre);
        institucion.setTipo(tipo);
        institucion.setLocalidad(localidad);
        institucion.setEstado(Estado.ACTIVO);
        repository.save(institucion);
    }
}
