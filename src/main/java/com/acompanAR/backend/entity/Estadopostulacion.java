package com.acompanAR.backend.entity;

/**
 * Estados por los que pasa una postulación.
 *
 * PENDIENTE  -> el profesional se postuló y el empleador todavía no respondió
 * ACEPTADA   -> el empleador aceptó al profesional (se habilita el contacto, el progreso y la mensajería)
 * FINALIZADA -> el trabajo terminó (lo marca cualquiera de las dos partes); recién ahí se puede calificar
 * RECHAZADA  -> el empleador rechazó la postulación
 * CANCELADA  -> el profesional retiró su postulación (baja lógica: el registro no se borra)
 */
public enum EstadoPostulacion {
    PENDIENTE,
    ACEPTADA,
    FINALIZADA,
    RECHAZADA,
    CANCELADA
}
