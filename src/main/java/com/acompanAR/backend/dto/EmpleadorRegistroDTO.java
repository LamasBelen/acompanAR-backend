package com.acompanAR.backend.dto;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;


public class EmpleadorRegistroDTO {
    //pongo los datos de la persona
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String apellido;
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;
    @NotBlank(message = "La localidad es obligatoria")
    @Size(max = 100, message = "La localidad no puede superar los 100 caracteres")
    private String localidad;

    //Datos del  usuario

    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 4, max = 30, message = "El usuario debe tener entre 4 y 30 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "El usuario solo puede tener letras, números, punto, guion y guion bajo")
    private String nombreUsuario;
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    private String contrasena;

    //datos del empleador

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 255, message = "El nombre de la empresa es demasiado largo")
    private String nombreEmpresa;
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcionPuesto;

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

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getDescripcionPuesto() {
        return descripcionPuesto;
    }

    public void setDescripcionPuesto(String descripcionPuesto) {
        this.descripcionPuesto = descripcionPuesto;
    }
}
