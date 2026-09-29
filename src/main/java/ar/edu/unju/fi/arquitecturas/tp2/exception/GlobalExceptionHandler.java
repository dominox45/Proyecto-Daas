package ar.edu.unju.fi.arquitecturas.tp2.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Interceptor global de excepciones para los controladores REST.
 * Captura las excepciones lanzadas por la lógica de negocio y las traduce
 * en respuestas HTTP estructuradas utilizando el DTO ErrorResponse.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja las excepciones cuando no se encuentra un recurso solicitado.
     *
     * @param ex      Excepción capturada.
     * @param request Petición HTTP original.
     * @return ResponseEntity con estado 404 (Not Found) y el detalle del error.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(
            RecursoNoEncontradoException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja las excepciones originadas por la falta de fondos en una cuenta.
     *
     * @param ex      Excepción capturada.
     * @param request Petición HTTP original.
     * @return ResponseEntity con estado 409 (CONFLICT) y el detalle del saldo.
     */
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleSaldoInsuficiente(
            SaldoInsuficienteException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Regla de Negocio - Saldo Insuficiente",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja las excepciones de validación estructural y argumentos inválidos.
     *
     * @param ex      Excepción capturada (ej. IllegalArgumentException o IllegalStateException).
     * @param request Petición HTTP original.
     * @return ResponseEntity con estado 400 (Bad Request) y el mensaje de validación.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleArgumentosInvalidos(
            RuntimeException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja las excepciones generadas por la etiqueta @Valid en los controladores.
     *
     * @param ex      Excepción de validación de Spring capturada.
     * @param request Petición HTTP original.
     * @return ResponseEntity con estado 400 (Bad Request) detallando el campo inválido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacionDatos(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación en la estructura de los datos");

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Fallo de Validación (@Valid)",
                mensaje,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}