package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.GlobalExceptionHandler;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la capa web para {@link ClienteController}.
 *
 * <p>
 * Verifica el comportamiento del endpoint encargado de registrar clientes,
 * incluyendo la respuesta HTTP para solicitudes válidas, la validación
 * estructural de los DTOs y el tratamiento de errores de negocio mediante
 * el manejador global de excepciones.
 * </p>
 *
 * <p>
 * La capa de servicios se simula mediante Mockito para aislar el
 * comportamiento del controlador y evitar dependencias con la base de datos.
 * </p>
 *
 * @author MaxDz
 * @version 1.0.0
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
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nombre").value("Ana López"))
                .andExpect(jsonPath("$.cuil").value("27304050608"))
                .andExpect(jsonPath("$.email").value("ana.lopez@email.com"))
                .andExpect(jsonPath("$.telefono").value("3884000000"))
                .andExpect(jsonPath("$.direccion")
                        .value("San Salvador de Jujuy"));
    }

    /**
     * Verifica que Jakarta Bean Validation rechace una solicitud
     * estructuralmente inválida antes de delegarla al servicio.
     *
     * <p>
     * En este escenario se utiliza un nombre vacío, un CUIL con formato
     * incorrecto y una dirección de correo electrónico inválida.
     * </p>
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
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifica que una regla de negocio rechazada por la capa Service
     * sea transformada por el manejador global en una respuesta
     * HTTP 400 (Bad Request).
     *
     * <p>
     * Este escenario representa, por ejemplo, el intento de registrar
     * un cliente cuyo CUIL ya se encuentra almacenado.
     * </p>
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
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Ya existe un cliente con el CUIL indicado"));
    }
}