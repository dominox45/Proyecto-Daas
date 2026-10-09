package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST encargado de gestionar las peticiones HTTP
 * relacionadas con los clientes del sistema bancario.
 *
 * <p>
 * Su responsabilidad consiste en recibir las solicitudes de la API,
 * validar estructuralmente los datos de entrada y delegar la lógica
 * de negocio a la capa de servicios.
 * </p>
 *
 * <p>
 * El controlador trabaja exclusivamente con DTOs y no expone
 * directamente entidades JPA.
 * </p>
 *
 * @author MaxDz
 * @version 1.2.0
 * @see ClienteService
 * @see ClienteRequestDto
 * @see ClienteResponseDto
 */
@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    /**
     * Servicio encargado de gestionar la lógica de negocio
     * relacionada con los clientes.
     */
    private final ClienteService clienteService;

    /**
     * Constructor utilizado para la inyección de dependencias.
     *
     * @param clienteService servicio encargado de la gestión de clientes
     */
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Registra un nuevo cliente en el sistema.
     *
     * <p>
     * Los datos de entrada son validados mediante Jakarta Bean Validation
     * antes de ser procesados por la capa de servicios.
     * </p>
     *
     * @param request DTO con los datos necesarios para registrar al cliente
     * @return {@link ResponseEntity} con el cliente registrado y
     *         código HTTP 201 (Created)
     */
    @PostMapping
    public ResponseEntity<ClienteResponseDto> crear(
            @Valid @RequestBody ClienteRequestDto request) {

        ClienteResponseDto response = clienteService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Asocia un cliente existente como adherente de otro cliente titular.
     *
     * <p>
     * La validación de la relación familiar pertenece a la capa Service.
     * Si la asociación se completa correctamente, la operación responde
     * sin contenido.
     * </p>
     *
     * @param titularId identificador del cliente titular
     * @param adherenteId identificador del cliente que será adherente
     * @return respuesta HTTP 204 (No Content)
     */
    @PostMapping("/{titularId}/adherentes/{adherenteId}")
    public ResponseEntity<Void> asociarAdherente(
            @PathVariable UUID titularId,
            @PathVariable UUID adherenteId) {

        clienteService.asociarAdherente(
                titularId,
                adherenteId
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Autoriza a un cliente adherente a realizar extracciones sobre
     * una cuenta específica perteneciente a su titular.
     *
     * @param titularId identificador del cliente titular
     * @param adherenteId identificador del cliente adherente
     * @param cuentaId identificador de la cuenta que será autorizada
     * @return respuesta HTTP 204 cuando la autorización se registra correctamente
     */
    @PostMapping(
            "/{titularId}/adherentes/{adherenteId}/cuentas/{cuentaId}"
    )
    public ResponseEntity<Void> autorizarCuentaAdherente(
            @PathVariable UUID titularId,
            @PathVariable UUID adherenteId,
            @PathVariable UUID cuentaId) {

        clienteService.autorizarCuentaAdherente(
                titularId,
                adherenteId,
                cuentaId
        );

        return ResponseEntity.noContent().build();
    }
    /**
     * Confirma la activación de un cliente mediante su token.
     *
     * @param token token enviado al correo del cliente
     * @return respuesta HTTP 204 cuando la activación es exitosa
     */
    @GetMapping("/activar")
    public ResponseEntity<Void> activar(
            @RequestParam String token) {

        clienteService.activar(token);

        return ResponseEntity.noContent().build();
    }
}