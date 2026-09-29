package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.CuentaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.CuentaResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.service.CuentaFinancieraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
 * @version 1.0.0
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
}