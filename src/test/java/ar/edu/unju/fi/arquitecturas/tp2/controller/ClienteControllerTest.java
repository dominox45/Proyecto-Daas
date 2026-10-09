package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.GlobalExceptionHandler;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Pruebas de la capa web para {@link ClienteController}.
 *
 * <p>
 * Verifica el comportamiento de los endpoints relacionados con
 * el registro de clientes y la gestión del grupo familiar.
 * </p>
 *
 * <p>
 * Se comprueban respuestas exitosas, validaciones estructurales
 * mediante Jakarta Bean Validation y el tratamiento de errores
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
 * @version 1.2.0
 * @see ClienteController
 * @see ClienteService
 * @see GlobalExceptionHandler
 */
@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
class ClienteControllerTest {

    /**
     * Componente utilizado para simular peticiones HTTP sobre
     * el controlador bajo prueba.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Conversor utilizado para serializar los DTOs a formato JSON.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Servicio simulado para aislar la capa Controller de la lógica
     * de negocio y de persistencia.
     */
    @MockitoBean
    private ClienteService clienteService;

    /**
     * Contexto JPA simulado necesario para cargar correctamente
     * el contexto reducido utilizado por {@link WebMvcTest}.
     */
    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    /**
     * Verifica que una solicitud válida permita registrar un cliente
     * y produzca una respuesta HTTP 201 (Created) junto con los datos
     * definidos en {@link ClienteResponseDto}.
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaCrearClienteYResponder201()
            throws Exception {

        UUID id = UUID.randomUUID();

        ClienteRequestDto request = new ClienteRequestDto(
                "Ana López",
                "27304050608",
                "ana.lopez@email.com",
                "3884000000",
                "San Salvador de Jujuy"
        );

        ClienteResponseDto response = new ClienteResponseDto(
                id,
                request.getNombre(),
                request.getCuil(),
                request.getEmail(),
                request.getTelefono(),
                request.getDireccion()
        );

        when(clienteService.crear(any(ClienteRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/clientes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nombre").value("Ana López"))
                .andExpect(jsonPath("$.cuil").value("27304050608"))
                .andExpect(jsonPath("$.email")
                        .value("ana.lopez@email.com"))
                .andExpect(jsonPath("$.telefono")
                        .value("3884000000"))
                .andExpect(jsonPath("$.direccion")
                        .value("San Salvador de Jujuy"));
    }

    /**
     * Verifica que Jakarta Bean Validation rechace una solicitud
     * estructuralmente inválida antes de delegarla al servicio.
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaRechazarRequestInvalido()
            throws Exception {

        ClienteRequestDto request = new ClienteRequestDto(
                "",
                "123",
                "correo-invalido",
                "3884000000",
                "San Salvador de Jujuy"
        );

        mockMvc.perform(
                        post("/api/v1/clientes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifica que una regla de negocio rechazada por la capa Service
     * sea transformada por el manejador global en una respuesta
     * HTTP 400 (Bad Request).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaResponder400CuandoExisteCuilDuplicado()
            throws Exception {

        ClienteRequestDto request = new ClienteRequestDto(
                "Ana López",
                "27304050608",
                "ana.lopez@email.com",
                "3884000000",
                "San Salvador de Jujuy"
        );

        when(clienteService.crear(any(ClienteRequestDto.class)))
                .thenThrow(
                        new IllegalArgumentException(
                                "Ya existe un cliente con el CUIL indicado"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/clientes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Ya existe un cliente con el CUIL indicado"
                                )
                );
    }

    /**
     * Verifica que una asociación válida entre titular y adherente
     * responda HTTP 204 (No Content).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaAsociarAdherenteYResponder204()
            throws Exception {

        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/clientes/{titularId}/adherentes/{adherenteId}",
                                titularId,
                                adherenteId
                        )
                )
                .andExpect(status().isNoContent());

        verify(clienteService).asociarAdherente(
                titularId,
                adherenteId
        );
    }

    /**
     * Verifica que la ausencia del titular sea traducida
     * a una respuesta HTTP 404 (Not Found).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaResponder404CuandoTitularNoExiste()
            throws Exception {

        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        doThrow(
                new RecursoNoEncontradoException(
                        "El cliente titular no existe"
                )
        ).when(clienteService)
                .asociarAdherente(
                        titularId,
                        adherenteId
                );

        mockMvc.perform(
                        post(
                                "/api/v1/clientes/{titularId}/adherentes/{adherenteId}",
                                titularId,
                                adherenteId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(
                        jsonPath("$.message")
                                .value("El cliente titular no existe")
                );
    }

    /**
     * Verifica que una relación familiar inválida sea traducida
     * a una respuesta HTTP 400 (Bad Request).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaResponder400CuandoRelacionFamiliarEsInvalida()
            throws Exception {

        UUID clienteId = UUID.randomUUID();

        doThrow(
                new IllegalArgumentException(
                        "Un cliente no puede ser adherente de sí mismo"
                )
        ).when(clienteService)
                .asociarAdherente(
                        clienteId,
                        clienteId
                );

        mockMvc.perform(
                        post(
                                "/api/v1/clientes/{titularId}/adherentes/{adherenteId}",
                                clienteId,
                                clienteId
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Un cliente no puede ser adherente de sí mismo"
                                )
                );
    }

    /**
     * Verifica que pueda autorizarse correctamente una cuenta
     * específica a un cliente adherente.
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaAutorizarCuentaAAdherenteYResponder204()
            throws Exception {

        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/clientes/{titularId}"
                                        + "/adherentes/{adherenteId}"
                                        + "/cuentas/{cuentaId}",
                                titularId,
                                adherenteId,
                                cuentaId
                        )
                )
                .andExpect(status().isNoContent());

        verify(clienteService)
                .autorizarCuentaAdherente(
                        titularId,
                        adherenteId,
                        cuentaId
                );
    }

    /**
     * Verifica que una relación inválida entre titular y adherente
     * sea respondida como HTTP 400 (Bad Request).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaResponder400CuandoAutorizacionEsInvalida()
            throws Exception {

        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        doThrow(
                new IllegalArgumentException(
                        "El cliente no es adherente del titular indicado"
                )
        ).when(clienteService)
                .autorizarCuentaAdherente(
                        titularId,
                        adherenteId,
                        cuentaId
                );

        mockMvc.perform(
                        post(
                                "/api/v1/clientes/{titularId}"
                                        + "/adherentes/{adherenteId}"
                                        + "/cuentas/{cuentaId}",
                                titularId,
                                adherenteId,
                                cuentaId
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(
                                "El cliente no es adherente del titular indicado"
                        ));
    }

    /**
     * Verifica que la autorización responda HTTP 404 (Not Found)
     * cuando la cuenta indicada no existe.
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaResponder404CuandoCuentaAAutorizarNoExiste()
            throws Exception {

        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        doThrow(
                new RecursoNoEncontradoException(
                        "La cuenta no existe"
                )
        ).when(clienteService)
                .autorizarCuentaAdherente(
                        titularId,
                        adherenteId,
                        cuentaId
                );

        mockMvc.perform(
                        post(
                                "/api/v1/clientes/{titularId}"
                                        + "/adherentes/{adherenteId}"
                                        + "/cuentas/{cuentaId}",
                                titularId,
                                adherenteId,
                                cuentaId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("La cuenta no existe"));
    }

    /**
     * Verifica que un token válido permita activar al cliente
     * y produzca una respuesta HTTP 204 (No Content).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaActivarClienteYResponder204()
            throws Exception {

        String token =
                UUID.randomUUID().toString();

        mockMvc.perform(
                        get("/api/v1/clientes/activar")
                                .param("token", token)
                )
                .andExpect(status().isNoContent());

        verify(clienteService)
                .activar(token);
    }

    /**
     * Verifica que un token inexistente sea traducido
     * a una respuesta HTTP 400 (Bad Request).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaResponder400CuandoTokenNoExiste()
            throws Exception {

        String token =
                UUID.randomUUID().toString();

        doThrow(
                new IllegalArgumentException(
                        "El token de activación no existe"
                )
        ).when(clienteService)
                .activar(token);

        mockMvc.perform(
                        get("/api/v1/clientes/activar")
                                .param("token", token)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "El token de activación no existe"
                                )
                );

        verify(clienteService)
                .activar(token);
    }

    /**
     * Verifica que un token vencido sea traducido
     * a una respuesta HTTP 400 (Bad Request).
     *
     * @throws Exception si ocurre un error durante la simulación
     *                   de la petición HTTP
     */
    @Test
    void deberiaResponder400CuandoTokenEstaVencido()
            throws Exception {

        String token =
                UUID.randomUUID().toString();

        doThrow(
                new IllegalArgumentException(
                        "El token de activación está vencido"
                )
        ).when(clienteService)
                .activar(token);

        mockMvc.perform(
                        get("/api/v1/clientes/activar")
                                .param("token", token)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "El token de activación está vencido"
                                )
                );

        verify(clienteService)
                .activar(token);
    }
}