package ar.edu.unju.fi.arquitecturas.tp2.model.enums;

/**
 * Representa el estado de activación de un cliente
 * dentro del sistema bancario.
 *
 * <p>
 * Los clientes creados mediante el flujo de alta del TP5
 * comenzarán en estado {@link #PENDIENTE_ACTIVACION} y,
 * una vez validado correctamente su token de activación,
 * podrán pasar a {@link #ACTIVO}.
 * </p>
 */
public enum EstadoCliente {

    /**
     * El cliente fue registrado pero todavía no confirmó
     * su activación mediante el token correspondiente.
     */
    PENDIENTE_ACTIVACION,

    /**
     * El cliente completó correctamente el proceso de activación.
     */
    ACTIVO
}