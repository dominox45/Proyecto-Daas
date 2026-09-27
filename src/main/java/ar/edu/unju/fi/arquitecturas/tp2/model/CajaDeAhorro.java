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
 * Representa una caja de ahorro del sistema bancario.
 *
 * <p>Hereda los atributos comunes y las relaciones de
 * {@link CuentaFinanciera}. Sus atributos específicos se almacenan
 * en la tabla cajas_de_ahorro mediante la estrategia JOINED.</p>
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cajas_de_ahorro")

public class CajaDeAhorro extends CuentaFinanciera {

    /** Tasa de interés anual expresada como valor decimal. */
    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal tasaInteresAnual;

    /** Cantidad de extracciones mensuales permitidas sin costo. */
    @Column(nullable = false)
    private int limiteExtraccionesMensualesSinCosto;
}