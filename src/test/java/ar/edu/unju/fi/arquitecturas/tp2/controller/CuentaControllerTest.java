package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.CuentaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.CuentaResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.GlobalExceptionHandler;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.service.CuentaFinancieraService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la capa web para {@link CuentaController}.
 *
 * <p>
 * Verifica el comportamiento de los endpoints REST relacionados
 * con la creación y consulta de cuentas financieras.
 * </p>
 *
 * <p>
 * Se comprueban respuestas exitosas, validaciones estructurales
 * mediante Jakarta Bean Validation y el tratamiento de excepciones
 * de negocio a través de {@link GlobalExceptionHandler}.
 * </p>
 *
 * <p>
 * La capa de servicios se simula mediante Mockito para aislar
 * el comportamiento del controlador y evitar dependencias
 * con la base de datos.
 * </p>
 *
 * @author MaxDz
 * @version 1.0.0
 * @see CuentaController
 * @see CuentaFinancieraService
 * @see GlobalExceptionHandler
 */
@WebMvcTest(CuentaController.class)
@Import(GlobalExceptionHandler.class)
class CuentaControllerTest {

    /**
     * Componente utilizado para simular peticiones HTTP
     * sobre el controlador bajo prueba.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Conversor utilizado para serializar los DTOs
     * al formato JSON enviado en las peticiones.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Servicio simulado para aislar la capa Controller
     * de la lógica de negocio y persistencia.
     */
    @MockitoBean
    private CuentaFinancieraService cuentaService;

    /**
     * Contexto JPA simulado necesario para cargar correctamente
     * el contexto reducido utilizado por {@link WebMvcTest}.
     */
    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    /**
     * Verifica que una solicitud válida permita registrar
     * una caja de ahorro y produzca una respuesta HTTP
     * 201 (Created).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaCrearCuentaYResponder201()
            throws Exception {

        UUID clienteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        CuentaRequestDto request = new CuentaRequestDto(
                clienteId,
                TipoCuenta.CAJA_DE_AHORRO,
                "2850590940090418135201",
                "ana.ahorro",
                new BigDecimal("0.050000"),
                5,
                null,
                null
        );

        CuentaResponseDto response = new CuentaResponseDto(
                cuentaId,
                TipoCuenta.CAJA_DE_AHORRO,
                request.getCbu(),
                request.getAlias(),
                BigDecimal.ZERO,
                EstadoCuenta.ACTIVA
        );

        when(cuentaService.crear(any(CuentaRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/cuentas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(cuentaId.toString())
                )
                .andExpect(
                        jsonPath("$.tipoCuenta")
                                .value("CAJA_DE_AHORRO")
                )
                .andExpect(
                        jsonPath("$.cbu")
                                .value("2850590940090418135201")
                )
                .andExpect(
                        jsonPath("$.alias")
                                .value("ana.ahorro")
                )
                .andExpect(
                        jsonPath("$.saldoOperativo")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.estado")
                                .value("ACTIVA")
                );
    }

    /**
     * Verifica que Jakarta Bean Validation rechace una solicitud
     * estructuralmente inválida antes de delegarla a la capa Service.
     *
     * <p>
     * En este escenario faltan el identificador del cliente y el tipo
     * de cuenta, además de utilizarse un CBU con longitud incorrecta
     * y un alias vacío.
     * </p>
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaRechazarRequestInvalido()
            throws Exception {

        CuentaRequestDto request = new CuentaRequestDto(
                null,
                null,
                "123",
                "",
                null,
                null,
                null,
                null
        );

        mockMvc.perform(
                        post("/api/v1/cuentas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifica que cuando el cliente indicado para la creación
     * de la cuenta no existe, el manejador global traduzca la
     * excepción en una respuesta HTTP 404 (Not Found).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaResponder404CuandoClienteNoExiste()
            throws Exception {

        UUID clienteId = UUID.randomUUID();

        CuentaRequestDto request = new CuentaRequestDto(
                clienteId,
                TipoCuenta.CAJA_DE_AHORRO,
                "2850590940090418135202",
                "cliente.inexistente",
                new BigDecimal("0.050000"),
                5,
                null,
                null
        );

        when(cuentaService.crear(any(CuentaRequestDto.class)))
                .thenThrow(
                        new RecursoNoEncontradoException(
                                "El cliente no existe"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/cuentas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("El cliente no existe")
                );
    }

    /**
     * Verifica que una cuenta existente pueda recuperarse mediante
     * su CBU y produzca una respuesta HTTP 200 (OK).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaBuscarCuentaPorCbuYResponder200()
            throws Exception {

        UUID cuentaId = UUID.randomUUID();
        String cbu = "2850590940090418135203";

        CuentaResponseDto response = new CuentaResponseDto(
                cuentaId,
                TipoCuenta.CUENTA_CORRIENTE,
                cbu,
                "juan.corriente",
                new BigDecimal("2500.00"),
                EstadoCuenta.ACTIVA
        );

        when(cuentaService.buscarPorCbu(cbu))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/cuentas/{cbu}", cbu)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(cuentaId.toString())
                )
                .andExpect(
                        jsonPath("$.tipoCuenta")
                                .value("CUENTA_CORRIENTE")
                )
                .andExpect(
                        jsonPath("$.cbu")
                                .value(cbu)
                )
                .andExpect(
                        jsonPath("$.alias")
                                .value("juan.corriente")
                )
                .andExpect(
                        jsonPath("$.saldoOperativo")
                                .value(2500.00)
                )
                .andExpect(
                        jsonPath("$.estado")
                                .value("ACTIVA")
                );
    }

    /**
     * Verifica que la consulta de un CBU inexistente sea traducida
     * por el manejador global en una respuesta HTTP 404 (Not Found).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaResponder404CuandoCbuNoExiste()
            throws Exception {

        String cbu = "2850590940090418135204";

        when(cuentaService.buscarPorCbu(cbu))
                .thenThrow(
                        new RecursoNoEncontradoException(
                                "La cuenta no existe"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/cuentas/{cbu}", cbu)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("La cuenta no existe")
                );
    }
}