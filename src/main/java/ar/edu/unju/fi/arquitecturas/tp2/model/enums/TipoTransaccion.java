package ar.edu.unju.fi.arquitecturas.tp2.model.enums;

/**
 * Define los tipos de transacciones que pueden registrarse
 * sobre una cuenta financiera.
 *
 * <p>
 * A partir del TP5 se incorpora {@link #DEBITO_COMISION}
 * para representar las comisiones debitadas automáticamente
 * por el sistema.
 * </p>
 */
public enum TipoTransaccion {

    /** Depósito de dinero en una cuenta. */
    DEPOSITO,

    /** Extracción de dinero desde una cuenta. */
    EXTRACCION,

    /** Movimiento generado en la cuenta de origen de una transferencia. */
    TRANSFERENCIA_ENVIADA,

    /** Movimiento generado en la cuenta de destino de una transferencia. */
    TRANSFERENCIA_RECIBIDA,

    /** Débito automático correspondiente a una comisión bancaria. */
    DEBITO_COMISION

}
