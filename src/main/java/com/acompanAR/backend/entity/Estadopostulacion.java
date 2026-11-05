package com.acompanAR.backend.entity;

/**
 * Estados por los que pasa una postulación.
 *
 * PENDIENTE  -> el profesional se postuló y el empleador todavía no respondió
 * ACEPTADA   -> el empleador aceptó al profesional (se habilita el contacto y, más adelante, la calificación)
 * RECHAZADA  -> el empleador rechazó la postulación
 * CANCELADA  -> el profesional retiró su postulación (baja lógica: el registro no se borra)
 */
public enum EstadoPostulacion {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    CANCELADA
}
