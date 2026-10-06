package ar.edu.unju.fi.arquitecturas.tp2.service;

import java.math.BigDecimal;

/**
 * Servicio encargado de proveer acceso a los parámetros de configuración general.
 * Oculta la complejidad de parseo y recuperación desde la base de datos.
 */
public interface ConfiguracionService {
    String obtenerValorTexto(String clave);
    BigDecimal obtenerValorDecimal(String clave);
    int obtenerValorEntero(String clave);
}