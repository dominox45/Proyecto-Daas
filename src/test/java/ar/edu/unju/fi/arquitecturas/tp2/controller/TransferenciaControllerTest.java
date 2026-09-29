package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.GlobalExceptionHandler;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.exception.SaldoInsuficienteException;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.service.TransferenciaService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Clase de pruebas para TransferenciaController.
 * Verifica el comportamiento de los endpoints HTTP, la validación de peticiones
 * y la correcta integración con el manejador global de excepciones.
 */
@WebMvcTest(TransferenciaController.class)
@Import(GlobalExceptionHandler.class)
class TransferenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransferenciaService transferenciaService;
    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;
    /**
     * Verifica que ante una petición válida se devuelva el código HTTP 200 (OK)
     * junto con el JSON del TransferenciaResponseDto estructurado correctamente.
     *
     * @throws Exception Si ocurre un error en la simulación de la petición MVC.
     */
    @Test
    void deberiaRealizarTransferenciaYResponder200()
            throws Exception {

        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        TransferenciaRequestDto request =
                new TransferenciaRequestDto(
                        origenId,
                        destinoId,
                        new BigDecimal("1500.00"));

        TransferenciaResponseDto response =
                new TransferenciaResponseDto(
                        origenId,
                        destinoId,
                        new BigDecimal("1500.00"),
                        EstadoTransaccion.COMPLETADA,
                        LocalDateTime.now());

        when(transferenciaService.transferir(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/transacciones/transferir")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(

                        "$.cuentaOrigenId")
                        .value(origenId.toString()))
                .andExpect(jsonPath(
                        "$.cuentaDestinoId")
                        .value(destinoId.toString()))
                .andExpect(jsonPath(
                        "$.monto")
                        .value(1500.00))
                .andExpect(jsonPath(
                        "$.estado")
                        .value("COMPLETADA"));
    }

    /**
     * Verifica que ante una petición estructuralmente inválida (datos faltantes o nulos),
     * la etiqueta @Valid la intercepte y devuelva un código HTTP 400 (Bad Request).
     *
     * @throws Exception Si ocurre un error en la simulación de la petición MVC.
     */
    @Test
    void deberiaRechazarRequestInvalido()
            throws Exception {

        TransferenciaRequestDto request =
                new TransferenciaRequestDto(
                        null,
                        UUID.randomUUID(),
                        BigDecimal.ZERO);

        mockMvc.perform(
                        post("/api/v1/transacciones/transferir")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifica que si el servicio lanza una RecursoNoEncontradoException, el controlador
     * y el manejador global devuelvan una respuesta HTTP 404 (Not Found).
     *
     * @throws Exception Si ocurre un error en la simulación de la petición MVC.
     */
    @Test
    void deberiaResponder404CuandoNoExisteLaCuenta()
            throws Exception {

        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        TransferenciaRequestDto request =
                new TransferenciaRequestDto(
                        origenId,
                        destinoId,
                        new BigDecimal("100.00"));

        when(transferenciaService.transferir(any()))
                .thenThrow(
                        new RecursoNoEncontradoException(
                                "La cuenta no existe"));

        mockMvc.perform(
                        post("/api/v1/transacciones/transferir")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(
                        "$.status").value(404));
    }

    /**
     * Verifica que si el servicio lanza una SaldoInsuficienteException, el controlador
     * y el manejador global devuelvan una respuesta HTTP 409 (Conflict).
     *
     * @throws Exception Si ocurre un error en la simulación de la petición MVC.
     */
    @Test
    void deberiaResponder409CuandoNoHaySaldo()
            throws Exception {

        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        TransferenciaRequestDto request =
                new TransferenciaRequestDto(
                        origenId,
                        destinoId,
                        new BigDecimal("100.00"));

        when(transferenciaService.transferir(any()))
                .thenThrow(
                        new SaldoInsuficienteException(
                                "Saldo insuficiente"));

        mockMvc.perform(
                        post("/api/v1/transacciones/transferir")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath(
                        "$.status").value(409));
    }
}