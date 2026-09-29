package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaResponseDto;

/**
 * Servicio encargado de gestionar las operaciones y reglas de negocio
 * relacionadas con las transferencias de la aplicación.
 */
public interface TransferenciaService {

    /**
     * Ejecuta una transferencia de fondos entre dos cuentas financieras.
     *
     * @param request Objeto DTO que contiene los datos de la operación (cuenta origen, destino y monto).
     * @return TransferenciaResponseDto Objeto DTO con el resumen de la operación procesada,
     *         incluyendo el estado final y la fecha/hora de la transacción.
     */
    TransferenciaResponseDto transferir(TransferenciaRequestDto request);
}