package com.acompanAR.backend.dto;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

// Datos que un profesional puede modificar de su perfil.
// No incluye usuario ni contraseña: eso no se cambia desde acá.
public class ProfesionalEdicionDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String apellido;
    @NotBlank(message = "El telefono es obligatorio")
    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;
    @NotBlank(message = "La localidad es obligatoria")
    @Size(max = 100, message = "La localidad no puede superar los 100 caracteres")
    private String localidad;

    @NotBlank(message = "El tipo de profesional es obligatorio")
    @Size(max = 255, message = "El tipo de profesional es demasiado largo")
    private String tipoProfesional;
    @Size(max = 1500, message = "La experiencia no puede superar los 1500 caracteres")
    private String experiencia;     // opcional
    @Size(max = 255, message = "La disponibilidad no puede superar los 255 caracteres")
    private String disponibilidad;  // opcional
    @Size(max = 255, message = "La matrícula no puede superar los 255 caracteres")
    private String matricula;       // opcional

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

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getTipoProfesional() {
        return tipoProfesional;
    }

    public void setTipoProfesional(String tipoProfesional) {
        this.tipoProfesional = tipoProfesional;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
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
