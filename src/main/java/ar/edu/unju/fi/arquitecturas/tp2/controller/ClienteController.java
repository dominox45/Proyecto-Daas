package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
 * @version 1.0.0
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
}