package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.GlobalExceptionHandler;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.exception.SaldoInsuficienteException;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.service.TransferenciaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la capa web para {@link TransferenciaController}.
 *
 * <p>
 * Verifica las respuestas HTTP del endpoint de transferencias,
 * la validación estructural del DTO y la traducción de errores
 * de negocio mediante {@link GlobalExceptionHandler}.
 * </p>
 *
 * <p>
 * Las pruebas incluyen la identificación del cliente que ejecuta
 * la transferencia, requerida para aplicar las reglas de autorización
 * del TP5.
 * </p>
 *
 * @author MaxDz
 * @version 1.1.0
 * @see TransferenciaController
 * @see TransferenciaService
 * @see GlobalExceptionHandler
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
     * Verifica que una transferencia válida responda HTTP 200
     * junto con los datos de la operación procesada.
     *
     * @throws Exception si ocurre un error durante la simulación HTTP
     */
    @Test
    void deberiaRealizarTransferenciaYResponder200()
            throws Exception {

        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        TransferenciaRequestDto request =
                new TransferenciaRequestDto(
                        operadorId,
                        origenId,
                        destinoId,
                        new BigDecimal("1500.00")
                );

        TransferenciaResponseDto response =
                new TransferenciaResponseDto(
                        origenId,
                        destinoId,
                        new BigDecimal("1500.00"),
                        EstadoTransaccion.COMPLETADA,
                        LocalDateTime.now()
                );

        when(transferenciaService.transferir(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/transacciones/transferir")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.cuentaOrigenId")
                                .value(origenId.toString())
                )
                .andExpect(
                        jsonPath("$.cuentaDestinoId")
                                .value(destinoId.toString())
                )
                .andExpect(
                        jsonPath("$.monto")
                                .value(1500.00)
                )
                .andExpect(
                        jsonPath("$.estado")
                                .value("COMPLETADA")
                );
    }

    /**
     * Verifica que Jakarta Bean Validation rechace una transferencia
     * cuando faltan datos obligatorios.
     *
     * @throws Exception si ocurre un error durante la simulación HTTP
     */
    @Test
    void deberiaRechazarRequestInvalido()
            throws Exception {

        TransferenciaRequestDto request =
                new TransferenciaRequestDto(
                        null,
                        null,
                        UUID.randomUUID(),
                        BigDecimal.ZERO
                );

        mockMvc.perform(
                        post("/api/v1/transacciones/transferir")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifica que una cuenta inexistente sea traducida
     * a una respuesta HTTP 404.
     *
     * @throws Exception si ocurre un error durante la simulación HTTP
     */
    @Test
    void deberiaResponder404CuandoNoExisteLaCuenta()
            throws Exception {

        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        TransferenciaRequestDto request =
                new TransferenciaRequestDto(
                        operadorId,
                        origenId,
                        destinoId,
                        new BigDecimal("100.00")
                );

        when(transferenciaService.transferir(any()))
                .thenThrow(
                        new RecursoNoEncontradoException(
                                "La cuenta no existe"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/transacciones/transferir")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                );
    }

    /**
     * Verifica que la falta de saldo sea traducida
     * a una respuesta HTTP 409.
     *
     * @throws Exception si ocurre un error durante la simulación HTTP
     */
    @Test
    void deberiaResponder409CuandoNoHaySaldo()
            throws Exception {

        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        TransferenciaRequestDto request =
                new TransferenciaRequestDto(
                        operadorId,
                        origenId,
                        destinoId,
                        new BigDecimal("100.00")
                );

        when(transferenciaService.transferir(any()))
                .thenThrow(
                        new SaldoInsuficienteException(
                                "Saldo insuficiente"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/transacciones/transferir")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                );
    }

    /**
     * Verifica que el intento de transferencia realizado por
     * un cliente adherente sea traducido a HTTP 400.
     *
     * @throws Exception si ocurre un error durante la simulación HTTP
     */
    @Test
    void deberiaResponder400CuandoAdherenteIntentaTransferir()
            throws Exception {

        UUID adherenteId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        TransferenciaRequestDto request =
                new TransferenciaRequestDto(
                        adherenteId,
                        origenId,
                        destinoId,
                        new BigDecimal("100.00")
                );

        when(transferenciaService.transferir(any()))
                .thenThrow(
                        new IllegalStateException(
                                "Los clientes adherentes solo pueden realizar extracciones"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/transacciones/transferir")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Los clientes adherentes solo pueden realizar extracciones"
                                )
                );
    }
}