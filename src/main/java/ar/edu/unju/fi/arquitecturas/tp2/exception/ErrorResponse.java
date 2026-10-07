package ar.edu.unju.fi.arquitecturas.tp2.exception;

import java.time.LocalDateTime;

/**
 * DTO inmutable (Record) para estructurar las respuestas de error de la API REST.
 * Garantiza que todos los errores devueltos por el sistema mantengan un formato JSON consistente.
 *
 * @param timestamp Fecha y hora exacta en que ocurrió el error.
 * @param status    Código de estado HTTP (ej. 400, 404).
 * @param error     Descripción corta del tipo de error HTTP (ej. "Not Found").
 * @param message   Mensaje detallado y específico sobre el problema de negocio.
 * @param path      Ruta de la API donde se originó la petición que falló.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {
}