package com.acompanAR.backend.entity;

// Estado de un registro para la baja lógica: nunca se borra una fila, solo pasa de ACTIVO a INACTIVO.
public enum Estado {
    ACTIVO,
    INACTIVO
}
