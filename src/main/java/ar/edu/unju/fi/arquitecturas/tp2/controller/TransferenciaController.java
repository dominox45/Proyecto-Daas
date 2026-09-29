package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.service.TransferenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST encargado de gestionar las peticiones HTTP
 * relacionadas con las transacciones financieras.
 */
@RestController
@RequestMapping("/api/v1/transacciones")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    /**
     * Constructor para la inyección de dependencias.
     *
     * @param transferenciaService Servicio que contiene la lógica de negocio de las transferencias.
     */
    public TransferenciaController(
            TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    /**
     * Endpoint para realizar una transferencia de fondos entre dos cuentas.
     *
     * @param request Objeto DTO con los datos de la transferencia (validado estructuralmente).
     * @return ResponseEntity con el TransferenciaResponseDto y código de estado HTTP 200 (OK).
     */
    @PostMapping("/transferir")
    public ResponseEntity<TransferenciaResponseDto> transferir(
            @Valid @RequestBody TransferenciaRequestDto request) {

        TransferenciaResponseDto response =
                transferenciaService.transferir(request);

        return ResponseEntity.ok(response);
    }
}