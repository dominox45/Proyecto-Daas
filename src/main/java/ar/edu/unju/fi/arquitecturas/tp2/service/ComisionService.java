package ar.edu.unju.fi.arquitecturas.tp2.service;

/**
 * Contrato para el proceso automatizado de liquidación de comisiones.
 */
public interface ComisionService {

    /**
     * Ejecuta el proceso masivo de cobro de mantenimiento mensual
     * sobre todas las cuentas activas del sistema.
     */
    void liquidarComisionesMensuales();
}