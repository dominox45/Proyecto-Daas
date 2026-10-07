package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.CuentaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.CuentaResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.service.CuentaFinancieraService;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ExtraccionRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST encargado de gestionar las peticiones HTTP
 * relacionadas con las cuentas financieras del sistema bancario.
 *
 * <p>
 * Su responsabilidad consiste en recibir solicitudes de la API,
 * validar estructuralmente los datos de entrada y delegar la lógica
 * de negocio a la capa de servicios.
 * </p>
 *
 * <p>
 * El controlador utiliza exclusivamente DTOs y no expone
 * directamente entidades JPA.
 * </p>
 *
 * @author MaxDz
 * @version 1.1.0
 * @see CuentaFinancieraService
 * @see CuentaRequestDto
 * @see CuentaResponseDto
 */
@RestController
@RequestMapping("/api/v1/cuentas")
public class CuentaController {

    /**
     * Servicio encargado de gestionar la lógica de negocio
     * relacionada con las cuentas financieras.
     */
    private final CuentaFinancieraService cuentaService;

    /**
     * Construye el controlador utilizando inyección de dependencias
     * mediante constructor.
     *
     * @param cuentaService servicio encargado de la gestión
     *                      de cuentas financieras
     */
    public CuentaController(
            CuentaFinancieraService cuentaService) {

        this.cuentaService = cuentaService;
    }

    /**
     * Registra una nueva cuenta financiera en el sistema.
     *
     * <p>
     * Los datos recibidos son validados mediante Jakarta Bean Validation
     * antes de ser procesados por la capa de servicios.
     * </p>
     *
     * @param request DTO con los datos necesarios para crear la cuenta
     * @return {@link ResponseEntity} con la cuenta registrada y
     *         código HTTP 201 (Created)
     */
    @PostMapping
    public ResponseEntity<CuentaResponseDto> crear(
            @Valid @RequestBody CuentaRequestDto request) {

        CuentaResponseDto response =
                cuentaService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Recupera una cuenta financiera mediante su CBU.
     *
     * @param cbu Clave Bancaria Uniforme de la cuenta
     * @return {@link ResponseEntity} con los datos de la cuenta y
     *         código HTTP 200 (OK)
     */
    @GetMapping("/{cbu}")
    public ResponseEntity<CuentaResponseDto> buscarPorCbu(
            @PathVariable String cbu) {

        CuentaResponseDto response =
                cuentaService.buscarPorCbu(cbu);

        return ResponseEntity.ok(response);
    }
    /**
     * Realiza una extracción sobre una cuenta financiera.
     *
     * <p>
     * El cliente que ejecuta la operación se identifica mediante
     * {@link ExtraccionRequestDto#getOperadorId()}. La capa Service
     * determina si se trata de un titular o de un adherente autorizado
     * explícitamente para operar sobre la cuenta.
     * </p>
     *
     * @param cuentaId identificador de la cuenta sobre la que se realizará
     *                 la extracción
     * @param request datos de la extracción solicitada
     * @return respuesta HTTP 204 (No Content) cuando la operación
     *         se completa correctamente
     */
    @PostMapping("/{cuentaId}/extracciones")
    public ResponseEntity<Void> extraer(
            @PathVariable UUID cuentaId,
            @Valid @RequestBody ExtraccionRequestDto request) {

        cuentaService.extraer(
                cuentaId,
                request.getOperadorId(),
                request.getMonto()
        );

        return ResponseEntity.noContent().build();
    }
}