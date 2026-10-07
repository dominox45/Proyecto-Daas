package ar.edu.unju.fi.arquitecturas.tp2.controller;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ConfiguracionResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ConfiguracionUpdateRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.service.ConfiguracionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador administrativo (Backoffice) para gestionar parámetros globales.
 * Permite listar y modificar en caliente valores críticos del sistema.
 */
@RestController
@RequestMapping("/api/v1/admin/configuraciones")
public class ConfiguracionAdminController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionAdminController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public ResponseEntity<List<ConfiguracionResponseDto>> obtenerTodas() {
        return ResponseEntity.ok(configuracionService.obtenerTodas());
    }

    @GetMapping("/{clave}")
    public ResponseEntity<ConfiguracionResponseDto> obtenerPorClave(@PathVariable String clave) {
        return ResponseEntity.ok(configuracionService.obtenerPorClave(clave));
    }

    @PutMapping("/{clave}")
    public ResponseEntity<ConfiguracionResponseDto> actualizar(
            @PathVariable String clave,
            @Valid @RequestBody ConfiguracionUpdateRequestDto request) {

        ConfiguracionResponseDto response = configuracionService.actualizar(clave, request);
        return ResponseEntity.ok(response);
    }
}