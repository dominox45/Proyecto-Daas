package ar.edu.unju.fi.arquitecturas.tp2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapea las propiedades definidas en el application.yml bajo el prefijo 'app.comisiones'.
 * Permite inyectar los montos globales de mantenimiento en los servicios
 * de forma tipada y segura.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.comisiones")
public class ComisionesProperties {

    /** Monto fijo mensual a debitar en las Cuentas Corrientes. */
    private BigDecimal cuentaCorriente;

    /** Monto fijo mensual a debitar en las Cajas de Ahorro. */
    private BigDecimal cajaAhorro;
}