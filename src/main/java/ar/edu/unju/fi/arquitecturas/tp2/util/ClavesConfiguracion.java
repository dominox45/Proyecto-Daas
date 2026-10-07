package ar.edu.unju.fi.arquitecturas.tp2.util;

/**
 * Catálogo centralizado de las claves utilizadas en la entidad
 * ConfiguracionGeneral.
 *
 * Centralizar estos valores previene errores tipográficos ("strings mágicos")
 * y facilita el seguimiento de qué parámetros dinámicos utiliza el sistema.
 */
public final class ClavesConfiguracion {

    private ClavesConfiguracion() {
        // Constructor privado para ocultar el constructor público implícito
    }

    /** Límite diario de extracción para clientes titulares. */
    public static final String LIMITE_EXTRACCION_TITULAR = "LIMITE_EXTRACCION_TITULAR";

    /** Límite diario de extracción para clientes adherentes. */
    public static final String LIMITE_EXTRACCION_ADHERENTE = "LIMITE_EXTRACCION_ADHERENTE";

    /** Expresión CRON que define cuándo se ejecuta la liquidación automática de comisiones. */
    public static final String CRON_LIQUIDACION_COMISIONES = "CRON_LIQUIDACION_COMISIONES";
}