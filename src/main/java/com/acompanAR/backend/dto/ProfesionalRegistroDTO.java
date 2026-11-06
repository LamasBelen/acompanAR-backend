package com.acompanAR.backend.dto;

import com.acompanAR.backend.entity.Localidad;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public class ProfesionalRegistroDTO {
    //aca van los datos de las personas
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String apellido;
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;
    @NotNull(message = "La localidad es obligatoria")
    private Localidad localidad;

    //sus datos de usuario
    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 4, max = 30, message = "El usuario debe tener entre 4 y 30 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "El usuario solo puede tener letras, números, punto, guion y guion bajo")
    private String nombreUsuario;
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    private String contrasena;

    //datos de profesional
    @NotNull(message = "El tipo de profesional es obligatorio")
    private Long tipoProfesionalId; // id de un registro de la tabla tipos_profesional
    @NotNull(message = "Los años de experiencia son obligatorios (si no tenés, poné 0)")
    @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
    @Max(value = 60, message = "Los años de experiencia no pueden superar 60")
    private Integer aniosExperiencia;
    @Size(max = 1500, message = "La descripción de la experiencia no puede superar los 1500 caracteres")
    private String descripcionExperiencia; // opcional
    @Size(max = 255, message = "La disponibilidad no puede superar los 255 caracteres")
    private String disponibilidad;
    @Size(max = 20, message = "La matrícula no puede superar los 20 caracteres")
    private String matricula;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Localidad getLocalidad() {
        return localidad;
    }

    public void setLocalidad(Localidad localidad) {
        this.localidad = localidad;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Long getTipoProfesionalId() {
        return tipoProfesionalId;
    }

    public void setTipoProfesionalId(Long tipoProfesionalId) {
        this.tipoProfesionalId = tipoProfesionalId;
    }

    public Integer getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(Integer aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }

    public String getDescripcionExperiencia() {
        return descripcionExperiencia;
    }

    public void setDescripcionExperiencia(String descripcionExperiencia) {
        this.descripcionExperiencia = descripcionExperiencia;
    }

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
}
