package com.acompanAR.backend.entity;

// Estado de verificación de una institución: nace PENDIENTE y el administrador la aprueba o la rechaza.
// Hasta ser VERIFICADA no puede publicar capacitaciones (cuida a la comunidad de estafas).
public enum EstadoVerificacion {
    PENDIENTE,
    VERIFICADA,
    RECHAZADA
}
