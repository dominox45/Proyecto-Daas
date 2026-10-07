package ar.edu.unju.fi.arquitecturas.tp2.model.enums;

/**
 * Representa los diferentes tipos de datos que puede contener
 * un parámetro de configuración en el sistema.
 * Esto facilita el parseo del valor almacenado (que es siempre un String)
 * al tipo de dato nativo requerido por la lógica de negocio.
 */
public enum TipoDatoConfiguracion {
    TEXTO,
    ENTERO,
    DECIMAL,
    BOOLEANO
}