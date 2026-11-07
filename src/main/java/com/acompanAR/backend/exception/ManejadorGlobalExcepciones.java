package com.acompanAR.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Atrapa las excepciones que salen de los controllers y las convierte en respuestas JSON claras.
 * Así el front recibe siempre el mismo formato:
 *
 *   { "estado": 400, "mensaje": "El nombre es obligatorio", "errores": { "nombre": "El nombre es obligatorio" } }
 *
 * y nunca se ve una página de error de Spring ni un detalle interno del servidor.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    // Falló alguna validación de un DTO (@NotBlank, @Size, @NotNull, ...) -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

        // Como mensaje principal usamos el primer error encontrado
        String mensaje = errores.values().stream().findFirst().orElse("Los datos enviados no son válidos");
        Map<String, Object> cuerpo = armarCuerpo(HttpStatus.BAD_REQUEST.value(), mensaje);
        cuerpo.put("errores", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }

    // El JSON está mal armado o un dato tiene el tipo incorrecto (ej: texto donde va un número) -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        // Si el problema es un valor que no existe en un enum (ej: localidad "XYZ"), lo avisamos con claridad.
        // Jackson incluye en su mensaje: ... not one of the values accepted for Enum class: [A, B, C]
        // Leemos ese mensaje (sin usar clases de Jackson, porque cambian según la versión de Spring Boot).
        Throwable causa = ex;
        while (causa != null) {
            String texto = causa.getMessage();
            if (texto != null && texto.contains("values accepted for Enum class")) {
                int inicio = texto.indexOf('[', texto.indexOf("values accepted for Enum class"));
                int fin = texto.indexOf(']', inicio);
                if (inicio >= 0 && fin > inicio) {
                    String permitidos = texto.substring(inicio + 1, fin);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(armarCuerpo(400, "Valor no permitido. Valores válidos: " + permitidos));
                }
            }
            causa = causa.getCause() == causa ? null : causa.getCause();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(armarCuerpo(400, "Los datos enviados están mal formados o tienen un tipo incorrecto"));
    }

    // Un parámetro de la URL tiene un tipo incorrecto (ej: /api/ofertas/abc en lugar de un número) -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(armarCuerpo(400, "El valor del parámetro '" + ex.getName() + "' no es válido"));
    }

    // Errores que lanzamos a propósito en los controllers (404, 403, 409, ...)
    // Usamos el código y el mensaje que se definieron al lanzarlos
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> manejarEstado(ResponseStatusException ex) {
        int codigo = ex.getStatusCode().value();
        String mensaje = ex.getReason() != null ? ex.getReason() : "No se pudo completar la operación";
        return ResponseEntity.status(ex.getStatusCode()).body(armarCuerpo(codigo, mensaje));
    }

    // La base de datos rechazó el dato (ej: valor duplicado o demasiado largo) -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(armarCuerpo(409, "Los datos no se pudieron guardar porque entran en conflicto con datos existentes"));
    }

    // Cualquier otro error inesperado -> 500.
    // El detalle se guarda en el log del servidor y al usuario solo se le muestra un mensaje general.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> manejarInesperado(RuntimeException ex) {
        log.error("Error inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(armarCuerpo(500, "Ocurrió un error inesperado. Intentá de nuevo más tarde"));
    }

    private Map<String, Object> armarCuerpo(int estado, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", estado);
        cuerpo.put("mensaje", mensaje);
        return cuerpo;
    }
}
