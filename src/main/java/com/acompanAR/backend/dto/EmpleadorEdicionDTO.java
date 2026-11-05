package com.acompanAR.backend.dto;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

// Datos que un empleador puede modificar de su perfil.
// No incluye usuario ni contraseña: eso no se cambia desde acá.
public class EmpleadorEdicionDTO {
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

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 255, message = "El nombre de la empresa es demasiado largo")
    private String nombreEmpresa;
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcionPuesto; // opcional

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
