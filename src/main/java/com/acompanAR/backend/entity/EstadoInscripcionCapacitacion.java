package com.acompanAR.backend.entity;

/**
 * Estados de la inscripción de un profesional a una capacitación.
 *
 * CONFIRMADA -> el profesional está inscripto y cursando
 * FINALIZADA -> el profesional terminó la capacitación
 * CANCELADA  -> el profesional se dio de baja (baja lógica: el registro no se borra)
 */
public enum EstadoInscripcionCapacitacion {
    CONFIRMADA,
    FINALIZADA,
    CANCELADA
}
