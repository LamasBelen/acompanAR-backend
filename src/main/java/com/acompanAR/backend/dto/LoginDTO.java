package com.acompanAR.backend.dto;
import jakarta.validation.constraints.NotBlank;

public class LoginDTO {
    @NotBlank(message = "El usuario no puede estar vacio")
    private String nombreUsuario;
    @NotBlank(message = "La contraseña no puede estar vacia")
    private String contrasena;

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

}
