package ar.edu.unju.fi.arquitecturas.tp2.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Clase de pruebas unitarias para el manejador global de excepciones (GlobalExceptionHandler).
 * Verifica que cada excepción de negocio se traduzca correctamente a una estructura
 * ErrorResponse con su respectivo código de estado HTTP.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/transacciones/transferir");

    /**
     * Verifica que cuando se lanza una RecursoNoEncontradoException, el manejador
     * construya una respuesta con código HTTP 404 (Not Found).
     */
    @Test
    void deberiaManejarRecursoNoEncontradoYRetornar404() {

        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("Cuenta no existe");

        ResponseEntity<ErrorResponse> response = handler.handleRecursoNoEncontrado(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cuenta no existe", response.getBody().message());
        assertEquals(404, response.getBody().status());
    }

    /**
     * Verifica que cuando se lanza una SaldoInsuficienteException, el manejador
     * construya una respuesta con código HTTP 409 (Conflict).
     */
    @Test
    void deberiaManejarSaldoInsuficienteYRetornar409() {

        SaldoInsuficienteException ex = new SaldoInsuficienteException("Sin fondos");

        ResponseEntity<ErrorResponse> response = handler.handleSaldoInsuficiente(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Sin fondos", response.getBody().message());
        assertEquals(409, response.getBody().status());
    }

    /**
     * Verifica que cuando se lanza una IllegalArgumentException (ej. monto negativo o nulo),
     * el manejador construya una respuesta con código HTTP 400 (Bad Request).
     */
    @Test
    void deberiaManejarArgumentoInvalidoYRetornar400() {

        IllegalArgumentException ex = new IllegalArgumentException("El monto debe ser positivo");

        ResponseEntity<ErrorResponse> response = handler.handleArgumentosInvalidos(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("El monto debe ser positivo", response.getBody().message());
        assertEquals(400, response.getBody().status());
    }
}