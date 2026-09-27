package ar.edu.unju.fi.arquitecturas.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Representa una cuenta corriente del sistema bancario.
 *
 * <p>Hereda los atributos comunes y las relaciones de
 * {@link CuentaFinanciera}. Sus atributos específicos se almacenan
 * en la tabla cuentas_corrientes mediante la estrategia JOINED.</p>
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cuentas_corrientes")

public class CuentaCorriente extends CuentaFinanciera {

    /** Importe de descubierto autorizado para la cuenta. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal descubiertoAutorizado;

    /** Importe de la comisión mensual por mantenimiento de la cuenta. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal costoComisionMantenimientoMensual;
}